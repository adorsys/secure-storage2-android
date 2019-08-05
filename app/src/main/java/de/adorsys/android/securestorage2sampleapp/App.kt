package de.adorsys.android.securestorage2sampleapp

import android.app.Application
import de.adorsys.android.securestorage2.SecureStorage

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SecureStorage.init(
            context = applicationContext,
            encryptionKeyAlias = "SecureStorage2Key",
            x500Principal = "CN=SecureStorage2 , O=Adorsys GmbH & Co. KG., C=Germany",
            useOnlyWithHardwareSupport = false
        )
    }
}