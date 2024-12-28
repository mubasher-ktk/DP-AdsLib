package com.dp.ads.lib.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class NetworkCheck {
    companion object{
        fun isNetworkAvailable(context: Context?):Boolean {
            val connMgr: ConnectivityManager =
                context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo: NetworkInfo? = connMgr.activeNetworkInfo
            if (activeNetworkInfo != null) {
                return if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                    true
                } else activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE
            }
            return false
        }
    }
}