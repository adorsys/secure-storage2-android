package de.adorsys.android.securestorage2sampleapp.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import de.adorsys.android.securestorage2library.SecureStorage
import de.adorsys.android.securestorage2sampleapp.MainActivity
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class SecureStorage2Test {

    @Rule
    @JvmField
    var mainActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun stringEncryptionDecryptionTest() {
        // Store String in SecureStorage
        SecureStorage.setValue(KEY_STRING, VALUE_STRING)
        // Assert value was stored in SecureStorage
        Assert.assertTrue(SecureStorage.contains(KEY_STRING))
        // Assert decrypted value equals the original value
        Assert.assertEquals(VALUE_STRING, SecureStorage.getStringValue(key = KEY_STRING, defValue = ""))
        // Remove value from SecureStorage
        SecureStorage.removeValue(KEY_STRING)
        // Assert value was removed from SecureStorage
        Assert.assertFalse(SecureStorage.contains(KEY_STRING))
    }

    @Test
    fun booleanEncryptionDecryptionTest() {
        // Store Boolean in SecureStorage
        SecureStorage.setValue(KEY_BOOLEAN, VALUE_BOOLEAN)
        // Assert value was stored in SecureStorage
        Assert.assertTrue(SecureStorage.contains(KEY_BOOLEAN))
        // Assert decrypted value equals the original value
        Assert.assertEquals(VALUE_BOOLEAN, SecureStorage.getBooleanValue(key = KEY_BOOLEAN, defValue = false))
        // Remove value from SecureStorage
        SecureStorage.removeValue(KEY_BOOLEAN)
        // Assert value was removed from SecureStorage
        Assert.assertFalse(SecureStorage.contains(KEY_BOOLEAN))
    }

    @Test
    fun integerEncryptionDecryptionTest() {
        // Store Integer in SecureStorage
        SecureStorage.setValue(KEY_INTEGER, VALUE_INTEGER)
        // Assert value was stored in SecureStorage
        Assert.assertTrue(SecureStorage.contains(KEY_INTEGER))
        // Assert decrypted value equals the original value
        Assert.assertEquals(VALUE_INTEGER, SecureStorage.getIntValue(key = KEY_INTEGER, defValue = 0))
        // Remove value from SecureStorage
        SecureStorage.removeValue(KEY_INTEGER)
        // Assert value was removed from SecureStorage
        Assert.assertFalse(SecureStorage.contains(KEY_INTEGER))
    }

    @Test
    fun longEncryptionDecryptionTest() {
        // Store Long in SecureStorage
        SecureStorage.setValue(KEY_LONG, VALUE_LONG)
        // Assert value was stored in SecureStorage
        Assert.assertTrue(SecureStorage.contains(KEY_LONG))
        // Assert decrypted value equals the original value
        Assert.assertEquals(VALUE_LONG, SecureStorage.getLongValue(key = KEY_LONG, defValue = 1))
        // Remove value from SecureStorage
        SecureStorage.removeValue(KEY_LONG)
        // Assert value was removed from SecureStorage
        Assert.assertFalse(SecureStorage.contains(KEY_LONG))
    }

    @Test
    fun floatEncryptionDecryptionTest() {
        // Store Float in SecureStorage
        SecureStorage.setValue(KEY_FLOAT, VALUE_FLOAT)
        // Assert value was stored in SecureStorage
        Assert.assertTrue(SecureStorage.contains(KEY_FLOAT))
        // Assert decrypted value equals the original value
        Assert.assertEquals(VALUE_FLOAT, SecureStorage.getFloatValue(key = KEY_FLOAT, defValue = 1.1.toFloat()))
        // Remove value from SecureStorage
        SecureStorage.removeValue(KEY_FLOAT)
        // Assert value was removed from SecureStorage
        Assert.assertFalse(SecureStorage.contains(KEY_FLOAT))
    }

    @Test
    fun clearAllValuesTest() {
        // Store a String, Integer and Long value in SecureStorage
        SecureStorage.setValue(KEY_STRING, VALUE_STRING)
        SecureStorage.setValue(KEY_INTEGER, VALUE_INTEGER)
        SecureStorage.setValue(KEY_LONG, VALUE_FLOAT)

        // Assert values exist in SecureStorage
        Assert.assertTrue(SecureStorage.contains(KEY_STRING))
        Assert.assertTrue(SecureStorage.contains(KEY_INTEGER))
        Assert.assertTrue(SecureStorage.contains(KEY_LONG))

        // Clear all values from SecureStorage and delete the Symmetric and Asymmetric keys
        SecureStorage.clearAllValues()

        // Assert all values where removed from SecureStorage
        Assert.assertFalse(SecureStorage.contains(KEY_STRING))
        Assert.assertFalse(SecureStorage.contains(KEY_INTEGER))
        Assert.assertFalse(SecureStorage.contains(KEY_LONG))
    }

    companion object {
        private const val KEY_STRING = "KEY_STRING_TEST"
        private const val VALUE_STRING = "VALUE_STRING_TEST"
        private const val KEY_BOOLEAN = "KEY_BOOLEAN_TEST"
        private const val VALUE_BOOLEAN = true
        private const val KEY_INTEGER = "KEY_INTEGER_TEST"
        private const val VALUE_INTEGER = 2147483647
        private const val KEY_LONG = "KEY_LONG_TEST"
        private const val VALUE_LONG = 9223372036854775807
        private const val KEY_FLOAT = "KEY_FLOAT_TEST"
        private const val VALUE_FLOAT = 999.99.toFloat()
    }
}