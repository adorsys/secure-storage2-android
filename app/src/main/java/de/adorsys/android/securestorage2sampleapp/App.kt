package de.adorsys.android.securestorage2sampleapp

import android.app.Application
import de.adorsys.android.securestorage2.SecureStorage

class App : Application() {

    private val isRunningTest: Boolean by lazy {
        try {
            Class.forName("androidx.test.espresso.Espresso")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    override fun onCreate() {
        super.onCreate()
        SecureStorage.init(
            context = applicationContext,
            encryptionKeyAlias = "SecureStorage2Key",
            x500Principal = "CN=SecureStorage2 , O=Adorsys GmbH & Co. KG., C=Germany",
            useOnlyWithHardwareSupport = false
        )

        // In Espresso tests we initialize the SecureStorageKeys in the test class
        when {
            !isRunningTest -> SecureStorage.initSecureStorageKeys(applicationContext)
        }
    }
}