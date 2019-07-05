package de.adorsys.android.securestorage2.internal

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import de.adorsys.android.securestorage2.*
import de.adorsys.android.securestorage2.execute
import de.adorsys.android.securestorage2.internal.AesCbcWithIntegrity.SecretKeys
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

@SuppressLint("CommitPrefEdits")
internal object KeyStoreToolApi21 {

    //================================================================================
    // SecureStorage KeyStoreTool API >= 21 && API < 23 Logic
    //================================================================================

    private const val KEY_PAIR_GENERATOR_PROVIDER = "AndroidKeyStore"
    private const val RSA_ALGORITHM = "RSA"
    private const val AES_ALGORITHM = "RSA"
    private const val KEY_AES_CONFIDENTIALITY_KEY = "AesConfidentialityKey"
    private const val KEY_AES_INTEGRITY_KEY = "AesIntegrityKey"

    @Throws(SecureStorageException::class)
    internal fun keyExists(context: Context, keyStoreInstance: KeyStore): Boolean {
        return rsaKeyPairExists(keyStoreInstance) && aesKeyExists(context)
    }

    @Throws(SecureStorageException::class)
    internal fun deleteKey(context: Context, keyStoreInstance: KeyStore) {
        // Delete Symmetric Key from SecureStorage
        SecureStorage.getSharedPreferencesInstance(context).edit().clear()
            .execute(SecureStorageConfig.INSTANCE.ASYNC_OPERATION)

        // Delete Asymmetric KeyPair from Keystore
        if (rsaKeyPairExists(keyStoreInstance)) {
            try {
                keyStoreInstance.deleteEntry(SecureStorageConfig.INSTANCE.ENCRYPTION_KEY_ALIAS)
            } catch (e: KeyStoreException) {
                throw SecureStorageException(e.message!!, e, SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION)
            }
        } else if (BuildConfig.DEBUG) {
            Log.e(
                KeyStoreTool::class.java.name,
                context.getString(R.string.message_keypair_does_not_exist)
            )
        }
    }

    @Throws(SecureStorageException::class)
    private fun rsaKeyPairExists(keyStoreInstance: KeyStore): Boolean {
        try {
            return keyStoreInstance.getKey(SecureStorageConfig.INSTANCE.ENCRYPTION_KEY_ALIAS, null) != null
        } catch (e: Exception) {
            throw SecureStorageException(e.message!!, e, SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION)
        }
    }

    @Throws(SecureStorageException::class)
    private fun aesKeyExists(context: Context): Boolean {
        return SecureStorage.getSharedPreferencesInstance(context).contains(KEY_AES_CONFIDENTIALITY_KEY)
                && SecureStorage.getSharedPreferencesInstance(context).contains(KEY_AES_INTEGRITY_KEY)
    }

    private fun getKeyPairGenerator(): KeyPairGenerator {
        return KeyPairGenerator.getInstance(RSA_ALGORITHM, KEY_PAIR_GENERATOR_PROVIDER)
    }

    fun generateKey(context: Context) {
        generateRsaKey(context)
    }

    @Suppress("DEPRECATION")
    private fun generateRsaKey(context: Context): KeyPair? {
        val keyPairGenerator = getKeyPairGenerator()

        val keyStartDate = Calendar.getInstance()
        keyStartDate.add(Calendar.DAY_OF_MONTH, -1)
        val keyEndDate = Calendar.getInstance()
        keyEndDate.add(Calendar.YEAR, 99)

        val keyPairGeneratorSpecBuilder = KeyPairGeneratorSpec.Builder(context)
            .setAlias(SecureStorageConfig.INSTANCE.ENCRYPTION_KEY_ALIAS)
            .setSubject(X500Principal(SecureStorageConfig.INSTANCE.X500PRINCIPAL))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(keyStartDate.time)
            .setEndDate(keyEndDate.time)

        keyPairGenerator.initialize(keyPairGeneratorSpecBuilder.build())
        return keyPairGenerator.generateKeyPair()
    }

    private fun generateAesKey(
        context: Context,
        keyStoreInstance: KeyStore,
        cipher: Cipher,
        keyPrefix: String
    ): SecretKeys {
        val secretKeys = AesCbcWithIntegrity.generateKey()

        storeAesKeyPart(
            context,
            keyStoreInstance,
            cipher,
            keyPrefix + KEY_AES_CONFIDENTIALITY_KEY,
            encodeKeyToString(secretKeys.confidentialityKey)
        )

        storeAesKeyPart(
            context,
            keyStoreInstance,
            cipher,
            keyPrefix + KEY_AES_INTEGRITY_KEY,
            encodeKeyToString(secretKeys.integrityKey)
        )

        return secretKeys
    }

    private fun storeAesKeyPart(
        context: Context,
        keyStoreInstance: KeyStore,
        cipher: Cipher,
        keyValueKey: String,
        aesKey: String
    ) {
        val publicKey = getPublicKey(context, keyStoreInstance)

        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val bytes = cipher.doFinal(aesKey.toByteArray())

        SecureStorage.getSharedPreferencesInstance(context).edit()
            .putString(keyValueKey, Base64.encodeToString(bytes, Base64.DEFAULT))
            .execute(SecureStorageConfig.INSTANCE.ASYNC_OPERATION)
    }

    private fun getAesKeyPart(
        context: Context,
        keyStoreInstance: KeyStore,
        cipher: Cipher,
        keyValueKey: String
    ): String {
        val privateKey = getPrivateKey(context, keyStoreInstance)

        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        val encodedKey = SecureStorage.getSharedPreferencesInstance(context).getString(keyValueKey, null)

        if (encodedKey == null) {
            throw SecureStorageException(
                context.getString(R.string.message_key_does_not_exist),
                null,
                SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION
            )
        } else {
            val decodedKey = Base64.decode(encodedKey, Base64.DEFAULT)

            val decryptedKey = cipher.doFinal(decodedKey)
            return String(decryptedKey)
        }
    }

    private fun encodeKeyToString(key: SecretKey): String {
        return Base64.encodeToString(key.encoded, Base64.DEFAULT)
    }

    private fun decodeStringToKey(encodedString: String): SecretKey {
        val encodedKey = Base64.decode(encodedString, Base64.DEFAULT)
        return SecretKeySpec(encodedKey, 0, encodedKey.size, AES_ALGORITHM)
    }

    private fun getAesKey(context: Context, keyStoreInstance: KeyStore, cipher: Cipher, keyPrefix: String): SecretKeys {
        val encodedIntegrityKey = getAesKeyPart(context, keyStoreInstance, cipher, keyPrefix + KEY_AES_INTEGRITY_KEY)
        val encodedConfidentialityKey =
            getAesKeyPart(context, keyStoreInstance, cipher, keyPrefix + KEY_AES_CONFIDENTIALITY_KEY)

        val reconstructedIntegrityKey = decodeStringToKey(encodedIntegrityKey)
        val reconstructedConfidentialityKey = decodeStringToKey(encodedConfidentialityKey)

        return SecretKeys(reconstructedConfidentialityKey, reconstructedIntegrityKey)
    }

    @Throws(SecureStorageException::class)
    private fun getPrivateKey(context: Context, keyStoreInstance: KeyStore): PrivateKey {

        if (rsaKeyPairExists(keyStoreInstance)) {
            try {
                val privateKeyEntry = keyStoreInstance
                    .getEntry(SecureStorageConfig.INSTANCE.ENCRYPTION_KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
                return privateKeyEntry.privateKey
            } catch (e: Exception) {
                throw SecureStorageException(e.message!!, e, SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION)
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.e(
                    KeyStoreTool::class.java.name,
                    context.getString(R.string.message_keypair_does_not_exist)
                )
            }
            throw SecureStorageException(
                context.getString(R.string.message_keypair_does_not_exist), null,
                SecureStorageException.ExceptionType.INTERNAL_LIBRARY_EXCEPTION
            )
        }
    }

    @Throws(SecureStorageException::class)
    private fun getPublicKey(context: Context, keyStoreInstance: KeyStore): PublicKey {
        if (rsaKeyPairExists(keyStoreInstance)) {
            try {
                val privateKeyEntry = keyStoreInstance
                    .getEntry(SecureStorageConfig.INSTANCE.ENCRYPTION_KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
                return privateKeyEntry.certificate.publicKey
            } catch (e: Exception) {
                throw SecureStorageException(e.message!!, e, SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION)
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.e(
                    KeyStoreTool::class.java.name,
                    context.getString(R.string.message_keypair_does_not_exist)
                )
            }
            throw SecureStorageException(
                context.getString(R.string.message_keypair_does_not_exist), null,
                SecureStorageException.ExceptionType.INTERNAL_LIBRARY_EXCEPTION
            )
        }
    }

    fun encryptValue(context: Context, keyStoreInstance: KeyStore, cipher: Cipher, key: String, value: String): String {
        generateAesKey(context, keyStoreInstance, cipher, key)

        val aesKey = getAesKey(context, keyStoreInstance, cipher, key)
        return AesCbcWithIntegrity.encrypt(value, aesKey).toString()
    }

    fun decryptValue(context: Context, keyStoreInstance: KeyStore, cipher: Cipher, key: String, value: String): String {
        val aesKey = getAesKey(context, keyStoreInstance, cipher, key)
        return AesCbcWithIntegrity.decryptString(
            AesCbcWithIntegrity.CipherTextIvMac(value),
            aesKey
        )
    }
}