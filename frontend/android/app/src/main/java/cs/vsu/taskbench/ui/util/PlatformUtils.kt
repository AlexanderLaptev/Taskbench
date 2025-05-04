package cs.vsu.taskbench.ui.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import androidx.core.content.getSystemService

fun Context.hasInternetConnection(): Boolean {
    val cm = getSystemService<ConnectivityManager>()!!
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return (capabilities.hasCapability(NET_CAPABILITY_INTERNET)
            && capabilities.hasCapability(NET_CAPABILITY_VALIDATED))
}
