package de.adorsys.android.securestorage2

import android.content.Context

enum class SecureStorageConfig {
    INSTANCE;

    internal lateinit var SHARED_PREFERENCES_NAME: String
    internal lateinit var ENCRYPTION_KEY_ALIAS: String

    fun initializeSecureStorageConfig(context: Context, ecnryptionKeyAlias: String) {
        SHARED_PREFERENCES_NAME = context.packageName + ".SecureStorage2"
        ENCRYPTION_KEY_ALIAS = ecnryptionKeyAlias
    }
}
