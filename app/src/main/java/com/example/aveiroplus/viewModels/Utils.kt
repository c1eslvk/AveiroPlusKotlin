package com.example.aveiroplus.viewModels

import android.content.Context
import androidx.core.content.edit
import com.example.aveiroplus.R

internal object SharedPreferenceUtil {

    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"

    fun getLocationTrackingPref(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            .getBoolean(KEY_FOREGROUND_ENABLED, false)

    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE).edit {
            putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
        }
}
