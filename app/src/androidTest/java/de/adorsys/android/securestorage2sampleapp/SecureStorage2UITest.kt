package de.adorsys.android.securestorage2sampleapp

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import de.adorsys.android.securestorage2.SecureStorage
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
open class SecureStorage2UITest : SecureStorage2BaseTest() {

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

        Log.d("SecureStorage2UITest Store Data Start Time", System.currentTimeMillis().toString())

        onView(withId(R.id.button_store))
            .perform(scrollTo())
            .perform(click())

        Log.d("SecureStorage2UITest Store Data End Time", System.currentTimeMillis().toString())

        // Get Data Section

        Espresso.onView(withId(R.id.edit_text_get_key))
            .perform(scrollTo())
            .perform(typeText(KEY))
            .perform(closeSoftKeyboard())

        Log.d("SecureStorage2UITest Get Data Start Time", System.currentTimeMillis().toString())

        onView(withId(R.id.button_get))
            .perform(scrollTo())
            .perform(click())

        Log.d("SecureStorage2UITest Get Data End Time", System.currentTimeMillis().toString())

        // Delete Data Section

        Espresso.onView(withId(R.id.edit_text_remove_key))
            .perform(scrollTo())
            .perform(typeText(KEY))
            .perform(closeSoftKeyboard())

        Log.d("SecureStorage2UITest Get Data Start Time", System.currentTimeMillis().toString())

        onView(withId(R.id.button_remove))
            .perform(scrollTo())
            .perform(click())

        Log.d("SecureStorage2UITest Get Data End Time", System.currentTimeMillis().toString())

        SecureStorage.clearAllValues(activityRule.activity.applicationContext)
    }
}