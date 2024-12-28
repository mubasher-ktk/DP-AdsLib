package com.dp.ads.lib.utils

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager

object AdLoadingDialog {

    private var dialog: Dialog? = null

    fun setContentView(activity: Activity, view: View?, isCancelable: Boolean, widthPercent: Float = 0.85f): AdLoadingDialog {
        dialog = Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(view!!)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(isCancelable)
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        return this
    }

    fun showDialogInterstitial(): Dialog? {
        dialog?.let {
            if (!it.isShowing) {
                it.show()
            }
        }
        return dialog
    }

    fun dismissDialog(activity: Activity) {
        try {
            dialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}