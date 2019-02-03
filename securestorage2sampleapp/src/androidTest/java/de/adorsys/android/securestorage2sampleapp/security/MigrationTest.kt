package de.adorsys.android.securestorage2sampleapp.security

import android.text.TextUtils
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import de.adorsys.android.securestorage2library.SecureStorage
import de.adorsys.android.securestorage2sampleapp.MainActivity
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @Rule
    @JvmField
    var mainActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun migrateValueFromSecureStorage1ToSecureStorage2Test() {
        // Store a value in SecureStorage1
        SecurePreferences.setValue(KEY_OLD_SOLUTION_STRING, VALUE_ORIGINAL_STRING)
        // Assert the value was correctly stored in SecureStorage1
        Assert.assertTrue(SecurePreferences.contains(KEY_OLD_SOLUTION_STRING))
        // Get the decrypted value from SecureStorage1
        val oldSolutionSavedValue = SecurePreferences.getStringValue(KEY_OLD_SOLUTION_STRING, "")
        // Assert decrypted value from SecureStorage1 is not null or blank
        Assert.assertFalse(TextUtils.isEmpty(oldSolutionSavedValue))
        // Store the decrypted value from SecureStorage1 in SecureStorage2
        SecureStorage.setValue(KEY_NEW_SOLUTION_STRING, oldSolutionSavedValue!!)
        // Assert the value was correctly stored in SecureStorage2
        Assert.assertTrue(SecureStorage.contains(KEY_NEW_SOLUTION_STRING))
        // Get the decrypted value from SecureStorage2
        val newSolutionSavedValue = SecureStorage.getStringValue(KEY_NEW_SOLUTION_STRING, "")
        // Assert decrypted value from SecureStorage2 is not null or blank
        Assert.assertFalse(TextUtils.isEmpty(newSolutionSavedValue))
        // Assert the values stored using SecureStorage1 and SecureStorage2 are equal
        Assert.assertEquals(oldSolutionSavedValue, newSolutionSavedValue)
        // Assert the value stored using SecureStorage2 equals original value
        Assert.assertEquals(newSolutionSavedValue, VALUE_ORIGINAL_STRING)
        // Remove value from SecureStorage1
        SecurePreferences.removeValue(KEY_OLD_SOLUTION_STRING)
        // Assert value was removed from SecureStorage1
        Assert.assertFalse(SecurePreferences.contains(KEY_OLD_SOLUTION_STRING))
        // Clear all values from SecureStorage1 and delete the Asymmetric keys
        SecurePreferences.clearAllValues()
        // Assert all values where removed from SecureStorage1
        Assert.assertFalse(SecurePreferences.contains(KEY_OLD_SOLUTION_STRING))
        // Clear all values from SecureStorage and delete the Symmetric and Asymmetric keys
        SecureStorage.clearAllValues()
    }

    companion object {
        private const val KEY_OLD_SOLUTION_STRING = "KEY_OLD_SOLUTION_STRING_TEST"
        private const val KEY_NEW_SOLUTION_STRING = "KEY_NEW_SOLUTION_STRING_TEST"
        private const val VALUE_ORIGINAL_STRING = "LOREM IPSUM DOLOR SIT AMET"
    }
}