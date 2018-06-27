package de.adorsys.android.securestorage2sampleapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import de.adorsys.android.securestorage2library.SecureStorage
import de.adorsys.android.securestoragelibrary.SecurePreferences

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startMessage = "TEST"

        Log.d("LOGTAG1", " " + startMessage)

        SecurePreferences.setValue("TAG", startMessage)

        Log.d("LOGTAG", " ENCRYPTED 1")

        val try1 = SecurePreferences.getStringValue("TAG", "WRONG")
        if (try1 != null) {
            SecureStorage.setValue("TAG", try1)
        }

        Log.d("LOGTAG", " ENCRYPTED 2")

        Log.d("LOGTAG2", " " + SecureStorage.getStringValue("TAG", "WRONG"))
    }
}
