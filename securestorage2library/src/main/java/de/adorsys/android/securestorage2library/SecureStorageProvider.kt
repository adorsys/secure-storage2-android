package de.adorsys.android.securestorage2library

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import java.lang.ref.WeakReference

class SecureStorageProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        SecureStorageProvider.context = WeakReference(context)
        // Fixes for the output of the default PRNG having low entropy on API 18
        PRNGFixes.apply()
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, //NOPMD
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?,
                        selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun attachInfo(context: Context, providerInfo: ProviderInfo?) {
        if (providerInfo == null) {
            throw NullPointerException("SecureStorageInitProvider ProviderInfo cannot be null.")
        }
        // So if the authorities equal the library internal ones, the developer forgot to set his applicationId
        if ("<your-library-applicationid>.de.adorsys.android.securestorage2library" == providerInfo.authority) {
            throw IllegalStateException("Incorrect provider authority in manifest. Most likely due to a " + "missing applicationId variable in application\'s build.gradle.")
        }
        super.attachInfo(context, providerInfo)
    }

    companion object {
        lateinit var context: WeakReference<Context>
    }
}