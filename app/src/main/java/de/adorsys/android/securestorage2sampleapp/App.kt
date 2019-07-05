package de.adorsys.android.securestorage2sampleapp

import android.app.Application
import de.adorsys.android.securestorage2.SecureStorage
import de.adorsys.android.securestorage2.SecureStorageConfig

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SecureStorageConfig.INSTANCE.initializeSecureStorageConfig(
            context = applicationContext,
            encryptionKeyAlias = "SecureStorage2Key",
            x500Principal = "CN=SecureStorage2 Sample App, O=Adorsys GmbH & Co. KG., C=Germany",
            useOnlyWithHardwareSupport = false,
            workWithDataAsynchronously = true
        )

        SecureStorage.initSecureStorageKeys(applicationContext)
    }
}