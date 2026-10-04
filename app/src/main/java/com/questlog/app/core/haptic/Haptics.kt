package com.questlog.app.core.haptic

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

object Haptics {

    fun success(view: View) {
        view.performHapticFeedback(constant(confirmed = true))
    }

    fun warning(view: View) {
        view.performHapticFeedback(constant(confirmed = false))
    }

    private fun constant(confirmed: Boolean): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (confirmed) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT
        } else {
            if (confirmed) HapticFeedbackConstants.LONG_PRESS else HapticFeedbackConstants.VIRTUAL_KEY
        }
}
