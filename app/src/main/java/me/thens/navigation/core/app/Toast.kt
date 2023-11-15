package me.thens.navigation.core.app

import android.content.Context
import android.util.Log
import android.widget.Toast

fun Context.showToast(text: String, longDuration: Boolean = false) {
    Log.d("TOAST", text)
    val length = if (longDuration) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, text, length).show()
}