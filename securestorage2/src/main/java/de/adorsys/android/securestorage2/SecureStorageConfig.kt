package de.adorsys.android.securestorage2

import android.content.Context
import de.adorsys.android.securestorage2.internal.KeyStoreTool

enum class SecureStorageConfig {
    INSTANCE;

    internal lateinit var SHARED_PREFERENCES_NAME: String
    internal lateinit var ENCRYPTION_KEY_ALIAS: String
    internal lateinit var X500PRINCIPAL: String
    internal var CAN_USE_LIBRARY = false
    internal var ASYNC_OPERATION = false

    fun initializeSecureStorageConfig(
        context: Context,
        encryptionKeyAlias: String,
        x500Principal: String,
        useOnlyWithHardwareSupport: Boolean,
        workWithDataAsynchronously: Boolean
    ) {
        SHARED_PREFERENCES_NAME = context.packageName + ".SecureStorage2"
        ENCRYPTION_KEY_ALIAS = encryptionKeyAlias
        X500PRINCIPAL = x500Principal
        ASYNC_OPERATION = workWithDataAsynchronously

        CAN_USE_LIBRARY = if (useOnlyWithHardwareSupport) {
            KeyStoreTool.deviceHasSecureHardwareSupport(context)
        } else {
            true
        }
    }

    // this is how a x500principal must look:
//    "CN=App Name, O=Company Name, C=Country"
}