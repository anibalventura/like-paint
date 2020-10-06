package com.anibalventura.likepaint.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.anibalventura.likepaint.App
import com.anibalventura.likepaint.utils.Constants.THEME
import kotlinx.coroutines.Dispatchers

val resources = App.resourses!!

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

/** =========================== Toast message. =========================== **/
fun toast(context: Context, message: Int) {
    Toast.makeText(context, resources.getString(message), Toast.LENGTH_SHORT).show()
}

/** ============================= Share text. ============================= **/
fun share(context: Context, message: String) {
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