package de.adorsys.android.securestorage2sampleapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import de.adorsys.android.securestorage2.SecureStorageConfig

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SecureStorageConfig.INSTANCE.initializeSecureStorageConfig(this@MainActivity, "KeyAlias")
    }
}