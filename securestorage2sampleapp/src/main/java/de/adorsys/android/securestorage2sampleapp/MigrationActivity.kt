/**
 *
 * ---STEPS FOR MIGRATING VALUES FROM SECURESTORAGE 1 TO SECURESTORAGE 2---
 *
 * 1. STORE A VALUE USING SECURE STORAGE 1
 *
 * 2. CHECK IF THE VALUE WAS SAVED CORRECTLY
 *
 * 3. GET THE VALUE FROM SECURE STORAGE 1
 *
 * 4. SAVE THE VALUE USING SECURE STORAGE 2
 *
 * 5. CHECK IF THE VALUE WAS SAVED CORRECTLY
 *
 * 6. COMPARE THE VALUES SAVED USING SECURE STORAGE 1 AND 2 AND SEE IF THEY ARE THE SAME (WHICH THEY SHOULD BE)
 *
 * 7. IF EVERYTHING IS OK, DELETE THE VALUE FROM THE SECURE STORAGE 1 SOLUTION
 *
 * 8. AFTER YOU HAVE DONE THIS FOR EVERY VALUE YOU WANT TO MIGRATE CLEAR ALL THE KEYS FROM SECURE STORAGE 1, AND KEEP ONLY USING SECURE STORAGE 2 FOR ALL FUTURE CREDENTIALS/VALUES
 */

package de.adorsys.android.securestorage2sampleapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import de.adorsys.android.securestorage2library.SecureStorage
import de.adorsys.android.securestoragelibrary.SecurePreferences
import kotlinx.android.synthetic.main.activity_migration.*

class MigrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_migration)

        var valueFromOldSolution = ""
        var valueFromNewSolution: String

        encrypt_value_button.setOnClickListener {
            if (TextUtils.isEmpty(plain_message_edit_text.text)) {
                showSnackbar(root_layout, "Field cannot be empty!", true)
            } else {
                // 1. STORE A VALUE USING SECURE STORAGE 1
                storeValueUsingOldSolution(plain_message_edit_text.text.toString())

                // 2. CHECK IF THE VALUE WAS SAVED CORRECTLY
                if (checkValueUsingOldSolution()) {
                    if (!TextUtils.isEmpty(getValueFromOldSolution())) {
                        // 3. GET THE VALUE FROM SECURE STORAGE 1
                        valueFromOldSolution = getValueFromOldSolution()!!

                        migrate_value_button.isEnabled = true

                        val processMessage = String.format(getString(R.string.message_encrypted_old_solution, plain_message_edit_text.text.toString(), valueFromOldSolution))
                        value_encryption_info_text_view.text = getSpannedText(processMessage)
                    } else {
                        showDefaultErrorSnackbar()
                    }
                } else {
                    showDefaultErrorSnackbar()
                }

                hideKeyboard(this, value_migration_text_view)
            }
        }

        migrate_value_button.setOnClickListener {
            if (TextUtils.isEmpty(valueFromOldSolution)) {
                showDefaultErrorSnackbar()
            } else {
                // 4. SAVE THE VALUE USING SECURE STORAGE 2
                storeValueUsingNewSolution(valueFromOldSolution)

                // 5. CHECK IF THE VALUE WAS SAVED CORRECTLY
                if (checkValueUsingNewSolution()) {
                    if (!TextUtils.isEmpty(getValueFromNewSolution())) {
                        valueFromNewSolution = getValueFromNewSolution()!!

                        // 6. COMPARE THE VALUES SAVED USING SECURE STORAGE 1 AND 2 AND SEE IF THEY ARE THE SAME (WHICH THEY SHOULD BE)
                        if (compareValuesBetweenSolutions(valueFromOldSolution, valueFromNewSolution)) {

                            // 7. IF EVERYTHING IS OK, DELETE THE VALUE FROM THE SECURE STORAGE 1 SOLUTION
                            removeValueFromOldSolution()

                            // 8. AFTER YOU HAVE DONE THIS FOR EVERY VALUE YOU WANT TO MIGRATE CLEAR ALL THE KEYS FROM SECURE STORAGE 1, AND KEEP ONLY USING SECURE STORAGE 2 FOR ALL FUTURE CREDENTIALS/VALUES
                            deleteAllKeysAndValuesFromOldSolution()

                            val processMessage = String.format(getString(R.string.message_encrypted_by_migration, valueFromOldSolution, valueFromNewSolution))
                            value_migration_text_view.text = getSpannedText(processMessage)
                        } else {
                            showSnackbar(root_layout, "Values are not the same, something went wrong during migration!", true)
                        }
                    } else {
                        showDefaultErrorSnackbar()
                    }
                } else {
                    showDefaultErrorSnackbar()
                }
                hideKeyboard(this, value_migration_text_view)
            }
        }
    }

    private fun storeValueUsingOldSolution(value: String) {
        SecurePreferences.setValue(KEY, value)
    }

    private fun checkValueUsingOldSolution(): Boolean {
        return SecurePreferences.contains(KEY)
    }

    private fun getValueFromOldSolution(): String? {
        return SecurePreferences.getStringValue(KEY, null)
    }

    private fun storeValueUsingNewSolution(value: String) {
        SecureStorage.setValue(KEY, value)
    }

    private fun checkValueUsingNewSolution(): Boolean {
        return SecureStorage.contains(KEY)
    }

    private fun getValueFromNewSolution(): String? {
        return SecureStorage.getStringValue(KEY, "")
    }

    private fun compareValuesBetweenSolutions(valueOldSolution: String, valueNewSolution: String): Boolean {
        return valueNewSolution == valueOldSolution
    }

    private fun removeValueFromOldSolution() {
        SecurePreferences.removeValue(KEY)
    }

    private fun deleteAllKeysAndValuesFromOldSolution() {
        SecurePreferences.clearAllValues()
    }

    private fun showDefaultErrorSnackbar() {
        showSnackbar(root_layout, "Something went wrong, value was not stored!", true)
    }

    companion object {
        private const val KEY = "TEMPORARY_KEY"
    }
}