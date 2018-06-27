package de.adorsys.android.securestorage2library

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.text.TextUtils
import de.adorsys.android.securestorage2library.SecureStorageException.ExceptionType.CRYPTO_EXCEPTION
import de.adorsys.android.securestorage2library.SecureStorageProvider.Companion.context
import java.io.UnsupportedEncodingException
import java.security.GeneralSecurityException

object SecureStorage {
    internal const val KEY_SHARED_PREFERENCES_NAME = "SecureStorage2"
    private const val KEY_SET_COUNT_POSTFIX = "_count"

    @Throws(SecureStorageException::class)
    fun setValue(key: String,
                 value: String) {
        if (!KeystoreTool.keysExist()) {
            KeystoreTool.generateKeys()
        }

        var transformedValue: String? = null
        try {
            transformedValue = KeystoreTool.encryptMessage(value)
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        if (TextUtils.isEmpty(transformedValue)) {
            throw SecureStorageException(
                    context.get()!!.getString(R.string.message_problem_encryption),
                    null, CRYPTO_EXCEPTION)
        } else {
            setSecureValue(key, transformedValue!!)
        }
    }

    @Throws(SecureStorageException::class)
    fun setValue(key: String,
                 value: Boolean) {
        setValue(key, value.toString())
    }

    @Throws(SecureStorageException::class)
    fun setValue(key: String,
                 value: Float) {
        setValue(key, value.toString())
    }

    @Throws(SecureStorageException::class)
    fun setValue(key: String,
                 value: Long) {
        setValue(key, value.toString())
    }

    @Throws(SecureStorageException::class)
    fun setValue(key: String,
                 value: Int) {
        setValue(key, value.toString())
    }

    @Throws(SecureStorageException::class)
    fun setValue(key: String,
                 value: Set<String>) {
        setValue(key + KEY_SET_COUNT_POSTFIX, value.size.toString())

        for ((i, s) in value.withIndex()) {
            setValue(key + "_" + i, s)
        }
    }

    fun getStringValue(key: String, defValue: String): String {
        val result = getSecureValue(key)
        return try {
            if (!TextUtils.isEmpty(result)) {
                KeystoreTool.decryptMessage(result)
            } else {
                defValue
            }
        } catch (e: SecureStorageException) {
            defValue
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            defValue
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
            defValue
        }

    }

    fun getBooleanValue(key: String,
                        defValue: Boolean): Boolean {
        return java.lang.Boolean.parseBoolean(getStringValue(key, defValue.toString()))
    }

    fun getFloatValue(key: String,
                      defValue: Float): Float {
        return java.lang.Float.parseFloat(getStringValue(key, defValue.toString()))
    }

    fun getLongValue(key: String,
                     defValue: Long): Long {
        return java.lang.Long.parseLong(getStringValue(key, defValue.toString()))
    }

    fun getIntValue(key: String,
                    defValue: Int): Int {
        return Integer.parseInt(getStringValue(key, defValue.toString()))
    }

    fun getStringSetValue(key: String,
                          defValue: Set<String>): Set<String> {
        val size = getIntValue(key + KEY_SET_COUNT_POSTFIX, -1)

        if (size == -1) {
            return defValue
        }

        val res = HashSet<String>(size)
        for (i in 0 until size) {
            res.add(getStringValue(key + "_" + i, ""))
        }

        return res
    }

    operator fun contains(key: String): Boolean {
        val preferences = context.get()
                ?.getSharedPreferences(KEY_SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        return preferences!!.contains(key)
    }

    fun removeValue(key: String) {
        removeSecureValue(key)
    }

    @Throws(SecureStorageException::class)
    fun clearAllValues() {
        if (KeystoreTool.keysExist()) {
            KeystoreTool.deleteKeys()
        }
        clearAllSecureValues()
    }

    fun registerOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        val preferences = context.get()
                ?.getSharedPreferences(KEY_SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        preferences!!.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        val preferences = context.get()
                ?.getSharedPreferences(KEY_SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        preferences!!.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun setSecureValue(key: String,
                       value: String) {
        val preferences = context.get()
                ?.getSharedPreferences(KEY_SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        preferences!!.edit().putString(key, value).apply()
    }

    private fun getSecureValue(key: String): String? {
        val preferences = context.get()
                ?.getSharedPreferences(KEY_SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        return preferences!!.getString(key, null)
    }

    private fun removeSecureValue(key: String) {
        val preferences = context.get()
                ?.getSharedPreferences(KEY_SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        preferences!!.edit().remove(key).apply()
    }

    private fun clearAllSecureValues() {
        val preferences = context.get()
                ?.getSharedPreferences(KEY_SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        preferences!!.edit().clear().apply()
    }
}