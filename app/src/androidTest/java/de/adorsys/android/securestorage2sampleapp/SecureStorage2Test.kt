package de.adorsys.android.securestorage2sampleapp

import android.util.Log
import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import de.adorsys.android.securestorage2.SecureStorage
import de.adorsys.android.securestorage2.SecureStorageException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import android.view.WindowManager
import android.content.Context.KEYGUARD_SERVICE
import android.app.KeyguardManager

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
open class SecureStorage2Test {
    @Rule
    @JvmField
    var activityRule = ActivityTestRule(
        MainActivity::class.java
    )

    @Before
    @Throws(SecureStorageException::class)
    fun setUp() {
        // Init library parameters
        SecureStorage.init(
            context = activityRule.activity.applicationContext,
            encryptionKeyAlias = "SecureStorage2Key",
            x500Principal = "CN=SecureStorage2 , O=Adorsys GmbH & Co. KG., C=Germany",
            useOnlyWithHardwareSupport = false
        )

        // Generate SecureStorage keys
        SecureStorage.initSecureStorageKeys(activityRule.activity.applicationContext)

        activityRule.activity.runOnUiThread {
            val keyguardManager = activityRule.activity.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            @Suppress("DEPRECATION") val keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE)
            keyguardLock.disableKeyguard()

            //turn the screen on
            activityRule.activity.window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
    }

    @Test
    fun testUI() {
        SecureStorage.clearAllValues(activityRule.activity.applicationContext)

        // Store Data Section

        Espresso.onView(withId(R.id.edit_text_store_key))
            .perform(scrollTo())
            .perform(typeText(KEY))
            .perform(closeSoftKeyboard())

        onView(withId(R.id.edit_text_store_value))
            .perform(scrollTo())
            .perform(typeText(VALUE))
            .perform(closeSoftKeyboard())

        Log.d("SecureStorage2Test Store Data Start Time", System.currentTimeMillis().toString())

        onView(withId(R.id.button_store))
            .perform(scrollTo())
            .perform(click())

        Log.d("SecureStorage2Test Store Data End Time", System.currentTimeMillis().toString())

        // Get Data Section

        Espresso.onView(withId(R.id.edit_text_get_key))
            .perform(scrollTo())
            .perform(typeText(KEY))
            .perform(closeSoftKeyboard())

        Log.d("SecureStorage2Test Get Data Start Time", System.currentTimeMillis().toString())

        onView(withId(R.id.button_get))
            .perform(scrollTo())
            .perform(click())

        Log.d("SecureStorage2Test Get Data End Time", System.currentTimeMillis().toString())

        // Delete Data Section

        Espresso.onView(withId(R.id.edit_text_remove_key))
            .perform(scrollTo())
            .perform(typeText(KEY))
            .perform(closeSoftKeyboard())

        Log.d("SecureStorage2Test Get Data Start Time", System.currentTimeMillis().toString())

        onView(withId(R.id.button_remove))
            .perform(scrollTo())
            .perform(click())

        Log.d("SecureStorage2Test Get Data End Time", System.currentTimeMillis().toString())

        SecureStorage.clearAllValues(activityRule.activity.applicationContext)
    }

    @Test
    fun testStoringData() {
        val context = activityRule.activity.applicationContext

        SecureStorage.clearAllValues(context)

        SecureStorage.putString(context, KEY, VALUE)

        assertTrue(SecureStorage.contains(context, KEY))

        SecureStorage.clearAllValues(activityRule.activity.applicationContext)
    }

    @Test
    fun testGettingData() {
        val context = activityRule.activity.applicationContext

        SecureStorage.clearAllValues(context)

        SecureStorage.putString(context, KEY, VALUE)

        assertEquals(VALUE, SecureStorage.getString(context, KEY, "FAILED"))

        SecureStorage.clearAllValues(activityRule.activity.applicationContext)
    }

    @Test
    fun testRemovingData() {
        val context = activityRule.activity.applicationContext

        SecureStorage.clearAllValues(context)

        SecureStorage.putString(context, KEY, VALUE)

        SecureStorage.remove(context, KEY)

        assertFalse(SecureStorage.contains(context, KEY))

        SecureStorage.clearAllValues(activityRule.activity.applicationContext)
    }

    companion object {
        const val KEY = "KEY_TEST"
        const val VALUE = "KEY_VALUE"
    }
}