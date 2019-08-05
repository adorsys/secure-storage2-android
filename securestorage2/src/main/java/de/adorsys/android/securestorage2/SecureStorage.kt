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

package de.adorsys.android.securestorage2

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import de.adorsys.android.securestorage2.internal.KeyStoreTool
import java.lang.Boolean.parseBoolean
import java.lang.Float.parseFloat
import java.lang.Integer.parseInt
import java.lang.Long.parseLong

@Suppress("unused")
object SecureStorage {
    internal const val KEY_INSTALLATION_API_VERSION_UNDER_M = "INSTALLATION_API_VERSION_UNDER_M"

    internal lateinit var SHARED_PREFERENCES_NAME: String
    internal lateinit var ENCRYPTION_KEY_ALIAS: String
    internal lateinit var X500PRINCIPAL: String
    internal var EXPLICITLY_USE_SECURE_HARDWARE = false

    private var CAN_USE_LIBRARY = true

    fun init(
        context: Context,
        encryptionKeyAlias: String? = null,
        x500Principal: String? = null,
        useOnlyWithHardwareSupport: Boolean = false
    ) {
        SHARED_PREFERENCES_NAME = context.packageName + ".SecureStorage2"
        ENCRYPTION_KEY_ALIAS = encryptionKeyAlias ?: "SecureStorage2Key"
        X500PRINCIPAL = x500Principal ?: "CN=SecureStorage2 , O=Adorsys GmbH & Co. KG., C=Germany"
        EXPLICITLY_USE_SECURE_HARDWARE = useOnlyWithHardwareSupport

        CAN_USE_LIBRARY = when {
            useOnlyWithHardwareSupport -> KeyStoreTool.deviceHasSecureHardwareSupport(context)
            else -> true
        }

        initSecureStorageKeys(context)
    }

    @Throws(SecureStorageException::class)
    fun deviceHasSecureHardwareSupport(context: Context): Boolean {
        val applicationContext = context.applicationContext

        checkAppCanUseLibrary()

        return KeyStoreTool.deviceHasSecureHardwareSupport(applicationContext)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(SecureStorageException::class)
    fun isKeyInsideSecureHardware() {
        checkAppCanUseLibrary()

        KeyStoreTool.isKeyInsideSecureHardware()
    }

    @Throws(SecureStorageException::class)
    fun putString(context: Context, key: String, value: String) {
        val applicationContext = context.applicationContext

        checkAppCanUseLibrary()

        if (!KeyStoreTool.keyExists(applicationContext)) {
            KeyStoreTool.generateKey(applicationContext)
        }

        val encryptedValue = KeyStoreTool.encryptValue(applicationContext, key, value)

        putSecureValue(applicationContext, key, encryptedValue)
    }

    @Throws(SecureStorageException::class)
    fun putBoolean(context: Context, key: String, value: Boolean) {
        val applicationContext = context.applicationContext

        putString(applicationContext, key, value.toString())
    }

    @Throws(SecureStorageException::class)
    fun putFloat(context: Context, key: String, value: Float) {
        val applicationContext = context.applicationContext

        putString(applicationContext, key, value.toString())
    }

    @Throws(SecureStorageException::class)
    fun putLong(context: Context, key: String, value: Long) {
        val applicationContext = context.applicationContext

        putString(applicationContext, key, value.toString())
    }

    @Throws(SecureStorageException::class)
    fun putInt(context: Context, key: String, value: Int) {
        val applicationContext = context.applicationContext

        putString(applicationContext, key, value.toString())
    }

    @Throws(SecureStorageException::class)
    fun getString(context: Context, key: String, defaultValue: String): String {
        val applicationContext = context.applicationContext

        checkAppCanUseLibrary()

        val encryptedValue = getSecureValue(applicationContext, key)

        if (encryptedValue.isNullOrBlank()) {
            return defaultValue
        }

        val decryptedValue = KeyStoreTool.decryptValue(applicationContext, key, encryptedValue)

        return when {
            decryptedValue.isNullOrBlank() -> defaultValue
            else -> decryptedValue
        }
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
        val applicationContext = context.applicationContext

        return parseBoolean(getString(applicationContext, key, defaultValue.toString()))
    }

    fun getFloat(context: Context, key: String, defaultValue: Float): Float {
        val applicationContext = context.applicationContext

        return parseFloat(getString(applicationContext, key, defaultValue.toString()))
    }

    fun getLong(context: Context, key: String, defaultValue: Long): Long {
        val applicationContext = context.applicationContext

        return parseLong(getString(applicationContext, key, defaultValue.toString()))
    }

    fun getInt(context: Context, key: String, defaultValue: Int): Int {
        val applicationContext = context.applicationContext

        return parseInt(getString(applicationContext, key, defaultValue.toString()))
    }

    @Throws(SecureStorageException::class)
    fun contains(context: Context, key: String): Boolean {
        val applicationContext = context.applicationContext

        checkAppCanUseLibrary()

        return getSharedPreferencesInstance(applicationContext).contains(key)
    }

    @Throws(SecureStorageException::class)
    fun remove(context: Context, key: String) {
        val applicationContext = context.applicationContext

        checkAppCanUseLibrary()

        removeSecureValue(applicationContext, key)
    }

    @Throws(SecureStorageException::class)
    fun clearAllValues(context: Context) {
        val applicationContext = context.applicationContext

        checkAppCanUseLibrary()

        val apiVersionUnderMExisted = contains(applicationContext, KEY_INSTALLATION_API_VERSION_UNDER_M)

        clearAllSecureValues(applicationContext)

        if (apiVersionUnderMExisted) {
            KeyStoreTool.setInstallApiVersionFlag(applicationContext, true)
        }
    }

    @Throws(SecureStorageException::class)
    fun clearAllValuesAndDeleteKeys(context: Context) {
        val applicationContext = context.applicationContext

        checkAppCanUseLibrary()

        if (KeyStoreTool.keyExists(applicationContext)) {
            KeyStoreTool.deleteKey(applicationContext)
        }
        clearAllSecureValues(applicationContext)
    }

    @Throws(SecureStorageException::class)
    fun registerOnSecureStorageChangeListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        val applicationContext = context.applicationContext

        checkAppCanUseLibrary()

        getSharedPreferencesInstance(applicationContext).registerOnSharedPreferenceChangeListener(listener)
    }

    @Throws(SecureStorageException::class)
    fun unregisterOnSecureStorageChangeListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        val applicationContext = context.applicationContext

        checkAppCanUseLibrary()

        getSharedPreferencesInstance(applicationContext).unregisterOnSharedPreferenceChangeListener(listener)
    }

    internal fun getSharedPreferencesInstance(context: Context): SharedPreferences {
        val applicationContext = context.applicationContext

        return applicationContext.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            MODE_PRIVATE
        )
    }

    @Throws(SecureStorageException::class)
    private fun initSecureStorageKeys(context: Context) {
        val applicationContext = context.applicationContext

        checkAppCanUseLibrary()

        KeyStoreTool.setInstallApiVersionFlag(applicationContext)

        if (!KeyStoreTool.keyExists(applicationContext)) {
            KeyStoreTool.initKeys(applicationContext)
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun putSecureValue(context: Context, key: String, value: String) {
        getSharedPreferencesInstance(context).edit().putString(key, value).execute()
    }

    private fun getSecureValue(context: Context, key: String): String? =
        getSharedPreferencesInstance(context).getString(key, null)

    @SuppressLint("CommitPrefEdits")
    private fun removeSecureValue(context: Context, key: String) {
        getSharedPreferencesInstance(context).edit().remove(key).execute()
    }

    private fun clearAllSecureValues(context: Context) = getSharedPreferencesInstance(context).edit().clear().execute()

    @Throws(SecureStorageException::class)
    private fun checkAppCanUseLibrary() {
        if (!CAN_USE_LIBRARY) {
            throw SecureStorageException(
                "Cannot use SecureStorage2 on this device because it does not have hardware support.",
                null,
                SecureStorageException.ExceptionType.KEYSTORE_NOT_SUPPORTED_EXCEPTION
            )
        }
    }
}

//================================================================================
// SecureStorage Extension Function
//================================================================================

internal fun SharedPreferences.Editor.execute() {
    apply()
}