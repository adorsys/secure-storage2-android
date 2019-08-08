package de.adorsys.android.securestorage2sampleapp

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import de.adorsys.android.securestorage2.SecureStorage
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
open class SecureStorage2LogicTest: SecureStorage2BaseTest() {

    @Test
    fun testStoringData() {
        val context = activityRule.activity.applicationContext

        SecureStorage.clearAllValues(context)

        SecureStorage.putString(context, KEY, VALUE)

        Assert.assertTrue(SecureStorage.contains(context, KEY))

        SecureStorage.clearAllValues(activityRule.activity.applicationContext)
    }

    @Test
    fun testGettingData() {
        val context = activityRule.activity.applicationContext

        SecureStorage.clearAllValues(context)

        SecureStorage.putString(context, KEY, VALUE)

        Assert.assertEquals(
            VALUE,
            SecureStorage.getString(context, KEY, "FAILED")
        )

        SecureStorage.clearAllValues(activityRule.activity.applicationContext)
    }

    @Test
    fun testRemovingData() {
        val context = activityRule.activity.applicationContext

        SecureStorage.clearAllValues(context)

        SecureStorage.putString(context, KEY, VALUE)

        SecureStorage.remove(context, KEY)

        Assert.assertFalse(SecureStorage.contains(context, KEY))

        SecureStorage.clearAllValues(activityRule.activity.applicationContext)
    }
}