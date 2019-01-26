package de.adorsys.android.securestorage2sampleapp

import android.app.Activity
import android.content.Context
import android.os.Build
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView

fun hideKeyboard(context: Context, view: View) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun getSpannedText(text: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(text)
    }
}

fun showSnackbar(rootView: View, message: String, durationShort: Boolean) {
    val duration = if (durationShort) {
        Snackbar.LENGTH_SHORT
    } else {
        Snackbar.LENGTH_LONG
    }

    val snackbar = Snackbar.make(rootView, message, duration)
    snackbar.view.setBackgroundColor(ContextCompat.getColor(rootView.context, R.color.colorPrimary))
    snackbar.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(rootView.context, android.R.color.white))
    snackbar.show()
}