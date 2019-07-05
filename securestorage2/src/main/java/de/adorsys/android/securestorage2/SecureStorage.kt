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
import android.support.annotation.RequiresApi
import de.adorsys.android.securestorage2.internal.KeyStoreTool
import de.adorsys.android.securestorage2.internal.SecureStorageException
import java.lang.Boolean.parseBoolean
import java.lang.Float.parseFloat
import java.lang.Integer.parseInt
import java.lang.Long.parseLong

@SuppressLint("CommitPrefEdits")
object SecureStorage {

    @Throws(SecureStorageException::class)
    fun initSecureStorageKeys(context: Context) {
        KeyStoreTool.setInstallApiVersionFlag(context)

        if (!KeyStoreTool.keyExists(context)) {
            KeyStoreTool.initKeys(context)
        }
    }

    @Throws(SecureStorageException::class)
    fun deviceHasSecureHardwareSupport(context: Context): Boolean {
        return KeyStoreTool.deviceHasSecureHardwareSupport(context)
    }

    @RequiresApi(23)
    @Throws(SecureStorageException::class)
    fun isKeyInsideSecureHardware() {
        KeyStoreTool.isKeyInsideSecureHardware()
    }

    @Throws(SecureStorageException::class)
    fun putString(context: Context, key: String, value: String) {
        checkAppCanUseLibrary()

        if (!KeyStoreTool.keyExists(context)) {
            KeyStoreTool.generateKey(context)
        }

        val encryptedValue = KeyStoreTool.encryptValue(context, key, value)

        putSecureValue(context, key, encryptedValue)
    }

    @Throws(SecureStorageException::class)
    fun putBoolean(context: Context, key: String, value: Boolean) {
        putString(context, key, value.toString())
    }

    @Throws(SecureStorageException::class)
    fun putFloat(context: Context, key: String, value: Float) {
        putString(context, key, value.toString())
    }

    @Throws(SecureStorageException::class)
    fun putLong(context: Context, key: String, value: Long) {
        putString(context, key, value.toString())
    }

    @Throws(SecureStorageException::class)
    fun putInt(context: Context, key: String, value: Int) {
        putString(context, key, value.toString())
    }

    @Throws(SecureStorageException::class)
    fun getString(context: Context, key: String, defaultValue: String): String {
        checkAppCanUseLibrary()

        val encryptedValue = getSecureValue(context, key)

        if (encryptedValue.isNullOrBlank()) {
            return defaultValue
        }

        val decryptedValue = KeyStoreTool.decryptValue(context, key, encryptedValue)

        return if (decryptedValue.isNullOrBlank()) {
            defaultValue
        } else {
            decryptedValue
        }
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
        return parseBoolean(getString(context, key, defaultValue.toString()))
    }

    fun getFloat(context: Context, key: String, defaultValue: Float): Float {
        return parseFloat(getString(context, key, defaultValue.toString()))
    }

    fun getLong(context: Context, key: String, defaultValue: Long): Long {
        return parseLong(getString(context, key, defaultValue.toString()))
    }

    fun getInt(context: Context, key: String, defaultValue: Int): Int {
        return parseInt(getString(context, key, defaultValue.toString()))
    }

    @Throws(SecureStorageException::class)
    fun contains(context: Context, key: String): Boolean {
        checkAppCanUseLibrary()

        return getSharedPreferencesInstance(context).contains(key)
    }

    @Throws(SecureStorageException::class)
    fun remove(context: Context, key: String) {
        checkAppCanUseLibrary()

        removeSecureValue(context, key)
    }

    @Throws(SecureStorageException::class)
    fun clearAllValues(context: Context) {
        checkAppCanUseLibrary()

        if (KeyStoreTool.keyExists(context)) {
            KeyStoreTool.deleteKey(context)
        }
        clearAllSecureValues(context)
    }

    @Throws(SecureStorageException::class)
    fun registerOnSecureStorageChangeListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        checkAppCanUseLibrary()

        getSharedPreferencesInstance(context).registerOnSharedPreferenceChangeListener(listener)
    }

    @Throws(SecureStorageException::class)
    fun unregisterOnSecureStorageChangeListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        checkAppCanUseLibrary()

        getSharedPreferencesInstance(context).unregisterOnSharedPreferenceChangeListener(listener)
    }

    internal fun getSharedPreferencesInstance(context: Context): SharedPreferences {
        return context.getSharedPreferences(SecureStorageConfig.INSTANCE.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
    }

    private fun putSecureValue(context: Context, key: String, value: String) {
        getSharedPreferencesInstance(context).edit().putString(key, value)
            .execute(SecureStorageConfig.INSTANCE.ASYNC_OPERATION)
    }

    private fun getSecureValue(context: Context, key: String): String? {
        return getSharedPreferencesInstance(context).getString(key, null)
    }

    private fun removeSecureValue(context: Context, key: String) {
        getSharedPreferencesInstance(context).edit().remove(key).execute(SecureStorageConfig.INSTANCE.ASYNC_OPERATION)
    }

    private fun clearAllSecureValues(context: Context) {
        getSharedPreferencesInstance(context).edit().clear().execute(SecureStorageConfig.INSTANCE.ASYNC_OPERATION)
    }

    @Throws(SecureStorageException::class)
    private fun checkAppCanUseLibrary() {
        if (!SecureStorageConfig.INSTANCE.CAN_USE_LIBRARY) {
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

internal fun SharedPreferences.Editor.execute(async: Boolean) {
    when {
        async -> apply()
        else -> commit()
    }
}