@file:Suppress("DEPRECATION")

package de.adorsys.android.securestorage2library.internal

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.provider.Settings.Secure.ANDROID_ID
import android.provider.Settings.Secure.getString
import android.security.KeyPairGeneratorSpec
import android.text.TextUtils
import android.util.Base64
import android.util.Base64.DEFAULT
import android.util.Base64.decode
import android.util.Log
import android.view.View.LAYOUT_DIRECTION_RTL
import de.adorsys.android.securestorage2library.BuildConfig
import de.adorsys.android.securestorage2library.R
import de.adorsys.android.securestorage2library.SecureStorage.KEY_SHARED_PREFERENCES_NAME
import de.adorsys.android.securestorage2library.SecureStorage.removeValue
import de.adorsys.android.securestorage2library.internal.SecureStorageException.ExceptionType.INTERNAL_LIBRARY_EXCEPTION
import de.adorsys.android.securestorage2library.internal.SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION
import de.adorsys.android.securestorage2library.internal.SecureStorageProvider.Companion.context
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.charset.Charset.forName
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.CertificateException
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.NoSuchPaddingException
import javax.security.auth.x500.X500Principal

internal object KeystoreTool {
    private const val KEY_CHARSET = "UTF-8"
    private const val KEY_X500PRINCIPAL = "CN=SecureStorage2, O=Adorsys, C=Germany"
    private const val KEY_ALIAS = "adorsysSecureStorage2Keypair"
    private const val KEY_KEYSTORE_NAME = "AndroidKeyStore"
    private const val KEY_ASYMMETRIC_ENCRYPTION_ALGORITHM = "RSA"
    private const val KEY_ASYMMETRIC_TRANSFORMATION_ALGORITHM = "RSA/ECB/PKCS1Padding"
    private const val KEY_RANDOM_SECURE_PASSPHRASE = "generated_secure_passphrase"
    private const val KEY_RANDOMIZED_DEVICE_ID = "randomized_device_id"

    fun keysExist(): Boolean {
        return asymmetricKeyPairExists() && symmetricKeyExists()
    }

    @Throws(SecureStorageException::class)
    private fun asymmetricKeyPairExists(): Boolean {
        try {
            return getKeyStoreInstance().getKey(KEY_ALIAS, null) != null
        } catch (e: Exception) {
            throw SecureStorageException(e.message!!, e, KEYSTORE_EXCEPTION)
        }
    }

    private fun symmetricKeyExists(): Boolean {
        val randomDeviceId = getAsymmetricValue(KEY_RANDOMIZED_DEVICE_ID, null)
        val randomSecurePassPhrase = getAsymmetricValue(KEY_RANDOM_SECURE_PASSPHRASE, null)
        return randomDeviceId != null || randomSecurePassPhrase != null
    }

    // https://stackoverflow.com/a/36394097/3392276
    @Throws(KeyStoreException::class, CertificateException::class,
            NoSuchAlgorithmException::class, IOException::class, NoSuchPaddingException::class)
    private fun getCipher(): Cipher {
        val ks = KeyStore.getInstance(KEY_KEYSTORE_NAME)
        ks.load(null)

        return Cipher.getInstance(KEY_ASYMMETRIC_TRANSFORMATION_ALGORITHM)
    }

    @Throws(SecureStorageException::class)
    private fun getKeyStoreInstance(): KeyStore {
        try {
            // Get the AndroidKeyStore instance
            val keyStore = KeyStore.getInstance(KEY_KEYSTORE_NAME)

            // Relict of the JCA API - you have to call load even
            // if you do not have an input stream you want to load or it'll crash
            keyStore.load(null)

            return keyStore
        } catch (e: Exception) {
            throw SecureStorageException(e.message!!, e, KEYSTORE_EXCEPTION)
        }
    }

    fun generateKeys() {
        if (!asymmetricKeyPairExists()) {
            generateAsymmetricKeypair()
        }
        if (!symmetricKeyExists()) {
            generateSymmetricKey()
        }
    }

    fun deleteKeys() {
        deleteSymmetricKey()
        deleteAsymmetricKeys()
    }

    fun encryptMessage(value: String): String? {
        val secretKeys =
                AesCbcWithIntegrity
                        .generateKeyFromPassword(
                                getAsymmetricValue(KEY_RANDOM_SECURE_PASSPHRASE, null),
                                getAsymmetricValue(KEY_RANDOMIZED_DEVICE_ID, null))
        return AesCbcWithIntegrity.encrypt(value, secretKeys).toString()
    }

    fun decryptMessage(result: String?): String {
        val secretKeys =
                AesCbcWithIntegrity.generateKeyFromPassword(
                        getAsymmetricValue(KEY_RANDOM_SECURE_PASSPHRASE, null),
                        getAsymmetricValue(KEY_RANDOMIZED_DEVICE_ID, null))
        return AesCbcWithIntegrity
                .decryptString(AesCbcWithIntegrity.CipherTextIvMac(result), secretKeys)
    }

    private fun isRTL(): Boolean {
        return context.get()!!.resources.configuration.layoutDirection == LAYOUT_DIRECTION_RTL
    }

    /** Asymmetric section */

    @Throws(SecureStorageException::class)
    private fun getAsymmetricPrivateKey(): PrivateKey {
        val privateKey: PrivateKey
        privateKey = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // only for P and newer versions
                getKeyStoreInstance().getKey(KEY_ALIAS, null) as PrivateKey
            } else {
                val privateKeyEntry =
                        getKeyStoreInstance().getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
                privateKeyEntry.privateKey
            }

        } catch (e: Exception) {
            throw SecureStorageException(e.message!!, e, KEYSTORE_EXCEPTION)
        }

        return privateKey
    }

    @Throws(SecureStorageException::class)
    private fun getAsymmetricPublicKey(): PublicKey {
        val publicKey: PublicKey
        publicKey = try {
            if (asymmetricKeyPairExists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // only for P and newer versions
                    getKeyStoreInstance().getCertificate(KEY_ALIAS).publicKey
                } else {
                    val privateKeyEntry = getKeyStoreInstance().getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
                    privateKeyEntry.certificate.publicKey
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e(KeystoreTool::class.java.name,
                            context.get()!!.getString(R.string.message_keypair_does_not_exist))
                }
                throw SecureStorageException(
                        context.get()!!.getString(R.string.message_keypair_does_not_exist), null,
                        INTERNAL_LIBRARY_EXCEPTION)
            }
        } catch (e: Exception) {
            throw SecureStorageException(e.message!!, e, KEYSTORE_EXCEPTION)
        }

        return publicKey
    }

    @Throws(SecureStorageException::class)
    private fun generateAsymmetricKeypair() {
        try {
            if (isRTL()) {
                Locale.setDefault(Locale.US)
            }

            val start = Calendar.getInstance()
            val end = Calendar.getInstance()
            end.add(Calendar.YEAR, 99)

            @Suppress("DEPRECATION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            val spec = KeyPairGeneratorSpec.Builder(context.get())
                    .setAlias(KEY_ALIAS)
                    .setSubject(X500Principal(KEY_X500PRINCIPAL))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .build()

            val generator = KeyPairGenerator.getInstance(KEY_ASYMMETRIC_ENCRYPTION_ALGORITHM, KEY_KEYSTORE_NAME)
            generator.initialize(spec)
            generator.generateKeyPair()
        } catch (e: Exception) {
            throw SecureStorageException(e.message!!, e, KEYSTORE_EXCEPTION)
        }
    }

    private fun setAsymmetricValue(key: String, plainMessage: String) {
        val encryptedValue: String
        try {
            val input = getCipher()
            input.init(Cipher.ENCRYPT_MODE, getAsymmetricPublicKey())

            val outputStream = ByteArrayOutputStream()
            val cipherOutputStream = CipherOutputStream(
                    outputStream, input)
            cipherOutputStream.write(plainMessage.toByteArray(forName(KEY_CHARSET)))
            cipherOutputStream.close()

            val values = outputStream.toByteArray()
            encryptedValue = Base64.encodeToString(values, DEFAULT)
        } catch (e: Exception) {
            throw SecureStorageException(e.message!!, e, KEYSTORE_EXCEPTION)
        }

        val preferences = context.get()!!.getSharedPreferences(KEY_SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        preferences.edit().putString(key, encryptedValue).apply()
    }

    private fun getAsymmetricValue(key: String, defValue: String?): String? {
        val preferences = context.get()!!.getSharedPreferences(KEY_SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val encryptedMessage = preferences.getString(key, null)

        try {
            val output = getCipher()
            output.init(Cipher.DECRYPT_MODE, getAsymmetricPrivateKey())

            val cipherInputStream = CipherInputStream(
                    ByteArrayInputStream(decode(encryptedMessage, DEFAULT)), output)
            val values = ArrayList<Byte>()

            var nextByte = 0
            while ({ nextByte = cipherInputStream.read(); nextByte }() != -1) {
                values.add(nextByte.toByte())
            }

            val bytes = ByteArray(values.size)
            for (i in bytes.indices) {
                bytes[i] = values[i]
            }

            return String(bytes, 0, bytes.size, forName(KEY_CHARSET))
        } catch (e: Exception) {
            return defValue
        }
    }

    @Throws(SecureStorageException::class)
    private fun deleteAsymmetricKeys() {
        // Delete Key from Keystore
        if (asymmetricKeyPairExists()) {
            try {
                getKeyStoreInstance().deleteEntry(KEY_ALIAS)
            } catch (e: KeyStoreException) {
                throw SecureStorageException(e.message!!, e, KEYSTORE_EXCEPTION)
            }
        } else if (BuildConfig.DEBUG) {
            Log.e(KeystoreTool::class.java.name,
                    context.get()!!.getString(R.string.message_keypair_does_not_exist))
        }
    }

    /** Symmetric section */

    @SuppressLint("HardwareIds")
    private fun getUniqueDeviceId(applicationContext: Context): String {
        return getString(applicationContext.contentResolver, ANDROID_ID)
    }

    private fun generateSymmetricKey() {
        val randomDeviceId = getAsymmetricValue(KEY_RANDOMIZED_DEVICE_ID, null)

        if (TextUtils.isEmpty(randomDeviceId)) {
            val uuid = UUID.randomUUID().toString()
            val randomizedId =
                    AesCbcWithIntegrity.saltString(
                            getUniqueDeviceId(context.get()!!).plus("-")
                                    .plus(uuid).toByteArray(forName(KEY_CHARSET)))
            try {
                setAsymmetricValue(KEY_RANDOMIZED_DEVICE_ID, randomizedId)
            } catch (e: Exception) {
                // setKeystoreCrashHappened();
            }
        }

        val randomSecurePassPhrase = getAsymmetricValue(KEY_RANDOM_SECURE_PASSPHRASE, null)
        if (TextUtils.isEmpty(randomSecurePassPhrase)) {
            try {
                setAsymmetricValue(KEY_RANDOM_SECURE_PASSPHRASE, SecurePassphraseUtil.generateRandomString())
            } catch (e: Exception) {
                // setKeystoreCrashHappened();
            }
        }
    }

    private fun deleteSymmetricKey() {
        removeValue(KEY_RANDOMIZED_DEVICE_ID)
        removeValue(KEY_RANDOM_SECURE_PASSPHRASE)
    }
}