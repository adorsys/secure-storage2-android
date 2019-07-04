package de.adorsys.android.securestorage2.internal

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.view.View.LAYOUT_DIRECTION_RTL
import de.adorsys.android.securestorage2.SecureStorage
import de.adorsys.android.securestorage2.SecureStorageConfig
import de.adorsys.android.securestorage2.execute
import de.adorsys.android.securestorage2.internal.SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher

@SuppressLint("NewApi")
internal object KeyStoreTool {

    private const val KEY_INSTALLATION_API_VERSION_UNDER_M = "INSTALLATION_API_VERSION_UNDER_M"
    private const val KEY_KEYSTORE_NAME = "AndroidKeyStore"
    private const val KEY_ASYMMETRIC_TRANSFORMATION_ALGORITHM = "RSA/ECB/PKCS1Padding"
    private const val KEY_SYMMETRIC_TRANSFORMATION_ALGORITHM = "AES/CBC/PKCS7Padding"

    //================================================================================
    // SecureStorage KeyStoreTool Logic
    //================================================================================

    internal fun initKeys(context: Context) {
        generateKey(context)
    }

    private fun isRTL(context: Context): Boolean {
        return context.resources.configuration.layoutDirection == LAYOUT_DIRECTION_RTL
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

    @Throws(SecureStorageException::class)
    private fun getCipher(context: Context): Cipher {
        getKeyStoreInstance()

        return if (apiVersionMAndAbove(context)) {
            Cipher.getInstance(KEY_SYMMETRIC_TRANSFORMATION_ALGORITHM)
        } else {
            // https://stackoverflow.com/a/36394097/3392276
            Cipher.getInstance(KEY_ASYMMETRIC_TRANSFORMATION_ALGORITHM)
        }
    }

    internal fun keyExists(context: Context): Boolean {
        return if (apiVersionMAndAbove(context)) {
            KeyStoreToolApi23.keyExists(getKeyStoreInstance())
        } else {
            KeyStoreToolApi21.keyExists(context, getKeyStoreInstance())
        }
    }


    internal fun generateKey(context: Context) {
        if (!keyExists(context)) {
            if (isRTL(context)) {
                Locale.setDefault(Locale.US)
            }

            if (apiVersionMAndAbove(context)) {
                KeyStoreToolApi23.generateKey()
            } else {
                KeyStoreToolApi21.generateKey(context, getKeyStoreInstance(), getCipher(context))
            }
        }
    }

    internal fun encryptValue(context: Context, key: String, value: String): String {
        return if (apiVersionMAndAbove(context)) {
            KeyStoreToolApi23.encryptValue(context, getKeyStoreInstance(), getCipher(context), key, value)
        } else {
            KeyStoreToolApi21.encryptValue(context, getKeyStoreInstance(), getCipher(context), value)
        }
    }

    internal fun decryptValue(context: Context, key: String, value: String): String? {
        return if (apiVersionMAndAbove(context)) {
            KeyStoreToolApi23.decryptValue(context, getKeyStoreInstance(), getCipher(context), key, value)
        } else {
            KeyStoreToolApi21.decryptValue(context, getKeyStoreInstance(), getCipher(context), value)
        }
    }

    internal fun deleteKey(context: Context) {
        if (apiVersionMAndAbove(context)) {
            KeyStoreToolApi23.deleteKey(context, getKeyStoreInstance())
        } else {
            KeyStoreToolApi21.deleteKey(context, getKeyStoreInstance())
        }
    }

    internal fun deviceHasSecureHardwareSupport(context: Context): Boolean {
        // Android uses the Fingerprint Hardware Abstraction Layer (HAL) to connect to a vendor-specific
        // library and fingerprint hardware, e.g. a fingerprint sensor.
        // A vendor-specific HAL implementation must use the communication protocol required by a TEE
        // https://source.android.com/security/authentication/fingerprint-hal
        return FingerprintManagerCompat.from(context).isHardwareDetected
    }

    @RequiresApi(23)
    internal fun isKeyInsideSecureHardware(): Boolean {
        return KeyStoreToolApi23.isKeyInsideSecureHardware(getKeyStoreInstance())
    }

    private fun apiVersionMAndAbove(context: Context): Boolean {
        val installationApiVersionUnderM =
            SecureStorage.getSharedPreferencesInstance(context).contains(KEY_INSTALLATION_API_VERSION_UNDER_M)
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !installationApiVersionUnderM
    }

    @SuppressLint("CommitPrefEdits")
    fun setInstallApiVersionFlag(context: Context) {
        val preferences = SecureStorage.getSharedPreferencesInstance(context)

        val installationApiVersionUnderM = preferences.contains(KEY_INSTALLATION_API_VERSION_UNDER_M)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && !installationApiVersionUnderM) {
            SecureStorage.getSharedPreferencesInstance(context).edit()
                .putBoolean(KEY_INSTALLATION_API_VERSION_UNDER_M, true)
                .execute(SecureStorageConfig.INSTANCE.ASYNC_OPERATION)
        }
    }
}