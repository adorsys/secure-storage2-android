package de.adorsys.android.securestorage2sampleapp

import android.app.KeyguardManager
import android.content.Context
import android.view.WindowManager
import androidx.test.rule.ActivityTestRule
import de.adorsys.android.securestorage2.SecureStorage
import de.adorsys.android.securestorage2.SecureStorageException
import org.junit.Before
import org.junit.Rule

open class SecureStorage2BaseTest {
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

        activityRule.activity.runOnUiThread {
            val keyguardManager = activityRule.activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            @Suppress("DEPRECATION") val keyguardLock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE)
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
}