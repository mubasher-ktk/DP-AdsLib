package com.dp.ads.lib.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkCheck {
    companion object{
        fun isNetworkAvailable(context: Context?): Boolean {
            val connMgr = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val network = connMgr.activeNetwork ?: return false
            val capabilities = connMgr.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
    }
}