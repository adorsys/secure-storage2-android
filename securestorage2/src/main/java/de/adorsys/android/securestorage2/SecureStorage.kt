package de.adorsys.android.securestorage2

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import de.adorsys.android.securestorage2.internal.KeyStoreTool
import de.adorsys.android.securestorage2.internal.SecureStorageException
import java.lang.Boolean.*
import java.lang.Float.parseFloat
import java.lang.Integer.parseInt
import java.lang.Long.parseLong

object SecureStorage {

    @Throws(SecureStorageException::class)
    fun putString(context: Context, key: String, value: String) {
        if (!KeyStoreTool.keyExists()) {
            KeyStoreTool.generateKey()
        }

        val encryptedValue = KeyStoreTool.encryptValue(value)

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

    fun getString(context: Context, key: String, defaultValue: String): String {
        val encryptedValue = getSecureValue(context, key)

        val decryptedValue = KeyStoreTool.decryptValue(encryptedValue)

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

    fun contains(context: Context, key: String): Boolean {
        val preferences =
            context.getSharedPreferences(SecureStorageConfig.INSTANCE.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        return preferences.contains(key)
    }

    fun remove(context: Context, key: String) {
        removeSecureValue(context, key)
    }

    @Throws(SecureStorageException::class)
    fun clearAllValues(context: Context) {
        if (KeyStoreTool.keyExists()) {
            KeyStoreTool.deleteKey()
        }
        clearAllSecureValues(context)
    }

    fun registerOnSecureStorageChangeListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        val preferences = context
            .getSharedPreferences(SecureStorageConfig.INSTANCE.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSecureStorageChangeListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        val preferences = context
            .getSharedPreferences(SecureStorageConfig.INSTANCE.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun putSecureValue(context: Context, key: String, value: String) {
        val preferences = context
            .getSharedPreferences(SecureStorageConfig.INSTANCE.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        preferences.edit().putString(key, value).apply()
    }

    private fun getSecureValue(context: Context, key: String): String? {
        val preferences = context
            .getSharedPreferences(SecureStorageConfig.INSTANCE.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        return preferences.getString(key, null)
    }

    private fun removeSecureValue(context: Context, key: String) {
        val preferences = context
            .getSharedPreferences(SecureStorageConfig.INSTANCE.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        preferences.edit().remove(key).apply()
    }

    private fun clearAllSecureValues(context: Context) {
        val preferences =
            context.getSharedPreferences(SecureStorageConfig.INSTANCE.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        preferences.edit().clear().apply()
    }
}