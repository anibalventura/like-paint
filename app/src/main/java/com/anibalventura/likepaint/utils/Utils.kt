package com.anibalventura.likepaint.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.anibalventura.likepaint.utils.Constants.THEME
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers

/** ========================== SharedPreferences. ========================== **/
fun sharedPref(context: Context): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(context)
}

/** ============================= Setup theme. ============================= **/
fun setupTheme(context: Context) {
    // Set the theme from the sharedPref value.
    when (sharedPref(context).getString(THEME, "0")) {
        "1" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        "2" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        "0" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}

/** =========================== SnackBar message. =========================== **/
fun snackBarMsg(view: View, message: String) {
    val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    snackBar.show()
}

/** ============================= Share text. ============================= **/
fun shareText(context: Context, message: String) {
    // Create the intent.
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }

    // Send the intent.
    context.let {
        Intent(context, Dispatchers.Main::class.java)
        context.startActivity(Intent.createChooser(sendIntent, null))
    }
}