package com.example.mockgpsprivacy.util

import android.webkit.JavascriptInterface

/**
 * Bridge for communication between WebView (JavaScript) and the app (Kotlin).
 * Allows receiving coordinates selected by the user on the map.
 */
class WebMapBridge(private val onLocationSelected: (Double, Double) -> Unit) {

    /**
     * Method called by the map's JavaScript.
     */
    @JavascriptInterface
    fun onMapClick(lat: Double, lon: Double) {
        onLocationSelected(lat, lon)
    }
}
