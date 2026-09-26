package com.opencapture.openpocketcine.session

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.opencapture.openpocketcine.diagnostics.DiagnosticCenter

/**
 * Local VPN / ad-blocker filters (AdGuard, Blokada, RethinkDNS) capture the
 * camera UDP datalink. `bindProcessToNetwork` cannot bypass a [android.net.VpnService]
 * that did not call `allowBypass()`. Matches iOS `LocalVPNFilter` copy and
 * live-wait policy (#239).
 */
object LocalVPNFilter {
    const val LIVE_HINT_DELAY_SECONDS = 8.0
    const val LIVE_HINT_DELAY_MS = 8_000L

    const val WIZARD_BANNER =
        "暂停 VPN 和广告拦截器，或将本应用加入排除名单。它们可能阻断相机实时画面。"

    const val LIVE_HINT =
        "可能有 VPN 或广告拦截器在阻断实时画面。请暂停它，或将本应用加入排除名单，然后重试。"

    const val JOIN_WIFI_PHONE_STEP = "暂停 VPN 和广告拦截器，或将本应用加入排除名单"

    fun shouldHintOnLiveWait(
        vpnActive: Boolean,
        hadVideo: Boolean,
        secondsWithoutVideo: Double,
    ): Boolean = vpnActive && !hadVideo && secondsWithoutVideo >= LIVE_HINT_DELAY_SECONDS

    fun isActive(context: Context): Boolean {
        val connectivity =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
                as? ConnectivityManager ?: return false
        return isActive(connectivity)
    }

    @Suppress("DEPRECATION")
    fun isActive(connectivity: ConnectivityManager): Boolean =
        connectivity.allNetworks.any { network ->
            connectivity.getNetworkCapabilities(network)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        }

    @Volatile private var loggedActive = false

    fun noteIfActive(context: Context) {
        if (!isActive(context) || loggedActive) return
        loggedActive = true
        DiagnosticCenter.log(
            "notice",
            "session",
            "vpn",
            "vpn: local VPN or ad blocker active — can drop UDP live view",
        )
    }
}
