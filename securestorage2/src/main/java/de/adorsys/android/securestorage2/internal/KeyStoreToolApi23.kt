/*
 * Copyright (C) 2019 adorsys GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.android.securestorage2.internal

import android.content.Context
import android.os.Build
import de.adorsys.android.securestorage2.R
import de.adorsys.android.securestorage2.SecureStorageConfig
import java.security.KeyStore
import java.security.KeyStoreException
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import androidx.annotation.RequiresApi
import android.util.Base64
import de.adorsys.android.securestorage2.SecureStorageException
import java.security.spec.InvalidKeySpecException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.SecretKeyFactory

internal object KeyStoreToolApi23 {

    //================================================================================
    // SecureStorage KeyStoreTool API >= 23 Logic
    //================================================================================

    private const val KEY_GENERATOR_PROVIDER = "AndroidKeyStore"
    private const val KEY_CIPHER_IV = "KeyCipherIV"

    @RequiresApi(23)
    @Throws(SecureStorageException::class)
    internal fun keyExists(keyStoreInstance: KeyStore): Boolean {
        try {
            return keyStoreInstance.getKey(SecureStorageConfig.INSTANCE.ENCRYPTION_KEY_ALIAS, null) != null
        } catch (e: Exception) {
            throw SecureStorageException(
                e.message!!,
                e,
                SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION
            )
        }
    }

    @RequiresApi(23)
    fun generateKey(): SecretKey {
        val keyGenerator = getKeyGenerator()

        val keyStartDate = Calendar.getInstance()
        keyStartDate.add(Calendar.DAY_OF_MONTH, -1)

        val keyGenParameterSpecBuilder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                KeyGenParameterSpec.Builder(
                    SecureStorageConfig.INSTANCE.ENCRYPTION_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setKeyValidityStart(keyStartDate.time)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setKeySize(256)
                    .setIsStrongBoxBacked(SecureStorageConfig.INSTANCE.EXPLICITLY_USE_SECURE_HARDWARE)
                    .setUserConfirmationRequired(SecureStorageConfig.INSTANCE.EXPLICITLY_USE_SECURE_HARDWARE)
            } else {
                KeyGenParameterSpec.Builder(
                    SecureStorageConfig.INSTANCE.ENCRYPTION_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setKeyValidityStart(keyStartDate.time)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setKeySize(256)
            }

        keyGenerator.init(keyGenParameterSpecBuilder.build())
        return keyGenerator.generateKey()
    }


    @RequiresApi(23)
    internal fun encryptValue(
        context: Context,
        keyStoreInstance: KeyStore,
        cipher: Cipher,
        key: String,
        value: String
    ): String {

        val secretKey = if (!keyExists(keyStoreInstance)) {
            generateKey()
        } else {
            getSecretKey(keyStoreInstance)
        }

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val bytes = cipher.doFinal(value.toByteArray())
        saveIVInSecureStorage(context, key, cipher.iv)
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    @RequiresApi(23)
    internal fun decryptValue(
        context: Context,
        keyStoreInstance: KeyStore,
        cipher: Cipher,
        key: String,
        value: String
    ): String {

        val secretKey = getSecretKey(keyStoreInstance)

        cipher.init(
            Cipher.DECRYPT_MODE, secretKey,
            IvParameterSpec(getIVFromSecureStorage(context, key))
        )
        val encryptedData = Base64.decode(value, Base64.DEFAULT)
        val decodedData = cipher.doFinal(encryptedData)
        return String(decodedData)
    }

    @RequiresApi(23)
    internal fun deleteKey(context: Context, keyStoreInstance: KeyStore) {
        // Delete Key from Keystore
        if (keyExists(keyStoreInstance)) {
            try {
                keyStoreInstance.deleteEntry(SecureStorageConfig.INSTANCE.ENCRYPTION_KEY_ALIAS)
            } catch (e: KeyStoreException) {
                throw SecureStorageException(
                    e.message!!,
                    e,
                    SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION
                )
            }
        } else {
            throw SecureStorageException(
                context.getString(R.string.message_key_does_not_exist),
                null,
                SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION
            )
        }
    }

    @RequiresApi(23)
    private fun saveIVInSecureStorage(context: Context, key: String, iv: ByteArray) {
        val preferences =
            context.getSharedPreferences(SecureStorageConfig.INSTANCE.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val encodedIv = Base64.encodeToString(iv, Base64.DEFAULT)
        preferences.edit().putString("$KEY_CIPHER_IV$key", encodedIv).apply()
    }

    @RequiresApi(23)
    private fun getIVFromSecureStorage(context: Context, key: String): ByteArray {
        val preferences =
            context.getSharedPreferences(SecureStorageConfig.INSTANCE.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val encodedIv = preferences.getString("$KEY_CIPHER_IV$key", null)
        return Base64.decode(encodedIv, Base64.DEFAULT)
    }

    @RequiresApi(23)
    internal fun isKeyInsideSecureHardware(keyStoreInstance: KeyStore): Boolean {
        return getKeyInfo(keyStoreInstance)?.isInsideSecureHardware ?: false
    }

    @RequiresApi(23)
    private fun getKeyGenerator(): KeyGenerator {
        return KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_GENERATOR_PROVIDER)
    }

    @RequiresApi(23)
    private fun getSecretKey(keyStoreInstance: KeyStore): SecretKey {
        val secretKeyEntry = keyStoreInstance
            .getEntry(SecureStorageConfig.INSTANCE.ENCRYPTION_KEY_ALIAS, null) as KeyStore.SecretKeyEntry

        return secretKeyEntry.secretKey
    }

    @RequiresApi(23)
    private fun getKeyInfo(keyStoreInstance: KeyStore): KeyInfo? {
        val secretKey = getSecretKey(keyStoreInstance)

        val factory = SecretKeyFactory.getInstance(secretKey.algorithm, KEY_GENERATOR_PROVIDER)
        var keyInfo: KeyInfo?
        try {
            keyInfo = factory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo
        } catch (e: InvalidKeySpecException) {
            // Not an Android KeyStore key.
            keyInfo = null
        }
        return keyInfo
    }
}