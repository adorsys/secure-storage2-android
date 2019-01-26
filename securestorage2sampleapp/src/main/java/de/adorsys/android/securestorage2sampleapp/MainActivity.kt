package de.adorsys.android.securestorage2sampleapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import de.adorsys.android.securestorage2library.SecureStorage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        encrypt_value_button.setOnClickListener { handleOnGenerateKeyButtonClick() }

        clear_field_button.setOnClickListener { handleOnClearFieldButtonClick() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_migrate -> {
                startActivity(Intent(this@MainActivity, MigrationActivity::class.java))
            }
            R.id.action_clear_all -> {
                try {
                    SecureStorage.clearAllValues()
                    showSnackbar(root_layout, "SecureStorage cleared, Asymmetric and symmetric Keys deleted", true)
                    plain_message_edit_text.setText("")
                    value_encryption_info_text_view.text = ""
                    clear_field_button.isEnabled = false
                    shield_image.setImageResource(R.drawable.shield_unlocked)
                } catch (e: Exception) {
                    showSnackbar(root_layout, e.message!!, false)
                }
                return true
            }
            R.id.action_info -> {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/adorsys/secure-storage2-android/blob/master/README.md")))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun handleOnGenerateKeyButtonClick() {
        if (!TextUtils.isEmpty(plain_message_edit_text.text)) {
            if (encrypt_value_button.text.toString() == getString(R.string.button_generate_encrypt)) {
                encrypt_value_button.setText(R.string.button_encrypt)
            }
            try {
                SecureStorage.setValue(KEY, plain_message_edit_text.text.toString())
                val decryptedMessage = SecureStorage.getStringValue(KEY, "")

                val fadeIn = AlphaAnimation(0f, 1f)
                fadeIn.duration = 500
                val fadeOut = AlphaAnimation(1f, 0f)
                fadeOut.duration = 500

                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        shield_image.setImageResource(R.drawable.shield_locked)
                        shield_image.startAnimation(fadeIn)
                        clear_field_button.isEnabled = true

                        val finalMessage = String.format(getString(R.string.message_encrypted_decrypted, plain_message_edit_text.text.toString(), decryptedMessage))
                        value_encryption_info_text_view.text = getSpannedText(finalMessage)
                    }
                })
                shield_image.startAnimation(fadeOut)
            } catch (e: de.adorsys.android.securestorage2library.internal.SecureStorageException) {
                handleException(e)
            }
        } else {
            showSnackbar(root_layout, "Field cannot be empty", true)
        }
        hideKeyboard(this, value_encryption_info_text_view)
    }

    private fun handleOnClearFieldButtonClick() {
        SecureStorage.removeValue(KEY)
        plain_message_edit_text.setText("")
        value_encryption_info_text_view.text = ""
        clear_field_button.isEnabled = false
        shield_image.setImageResource(R.drawable.shield_unlocked)
    }

    private fun handleException(e: de.adorsys.android.securestorage2library.internal.SecureStorageException) {
        Log.e(TAG, e.message)
        when (e.type) {
            de.adorsys.android.securestorage2library.internal.SecureStorageException.ExceptionType.KEYSTORE_NOT_SUPPORTED_EXCEPTION
            -> showSnackbar(root_layout, getString(R.string.error_not_supported), false)
            de.adorsys.android.securestorage2library.internal.SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION
            -> showSnackbar(root_layout, getString(R.string.error_fatal), false)
            de.adorsys.android.securestorage2library.internal.SecureStorageException.ExceptionType.CRYPTO_EXCEPTION
            -> showSnackbar(root_layout, getString(R.string.error_encryption), false)
            de.adorsys.android.securestorage2library.internal.SecureStorageException.ExceptionType.INTERNAL_LIBRARY_EXCEPTION
            -> showSnackbar(root_layout, getString(R.string.error_library), false)
            else -> return
        }
    }

    companion object {
        private const val KEY = "TEMPORARY_KEY"
        private const val TAG = "LOG_TAG"
    }
}