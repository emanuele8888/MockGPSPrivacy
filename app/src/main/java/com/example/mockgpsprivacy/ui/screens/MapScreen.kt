package com.example.mockgpsprivacy.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.example.mockgpsprivacy.util.WebMapBridge
import com.example.mockgpsprivacy.viewmodel.LocationViewModel
import java.util.Locale

@Composable
fun MapScreen(viewModel: LocationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            tonalElevation = 4.dp, 
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(
                    text = "Secure Location Selector",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Lat: ${uiState.latitudeText}, Lon: ${uiState.longitudeText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            HardenedMapView(
                currentLat = uiState.latitudeText.replace(",", ".").toDoubleOrNull() ?: 45.4642,
                currentLon = uiState.longitudeText.replace(",", ".").toDoubleOrNull() ?: 9.1900,
                onCoordinatesSelected = { lat, lon ->
                    viewModel.updateLatitude(String.format(Locale.US, "%.6f", lat))
                    viewModel.updateLongitude(String.format(Locale.US, "%.6f", lon))
                }
            )
            
            Badge(
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Text("PRIVACY ENGINE ACTIVE", modifier = Modifier.padding(4.dp))
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HardenedMapView(
    currentLat: Double,
    currentLon: Double,
    onCoordinatesSelected: (Double, Double) -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                .build()

            WebView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        return assetLoader.shouldInterceptRequest(request.url)
                    }
                }
                
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                addJavascriptInterface(WebMapBridge(onCoordinatesSelected), "AndroidBridge")

                loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/",
                    getSecureMapHtml(currentLat, currentLon),
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { /* No update needed */ }
    )
}

private fun getSecureMapHtml(lat: Double, lon: Double): String {
    val latStr = String.format(Locale.US, "%.6f", lat)
    val lonStr = String.format(Locale.US, "%.6f", lon)
    
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://appassets.androidplatform.net/assets/leaflet/leaflet.css" />
            <script src="https://appassets.androidplatform.net/assets/leaflet/leaflet.js"></script>
            <style>
                body { margin: 0; padding: 0; background: #1a1a1a; overflow: hidden; }
                #map { height: 100vh; width: 100vw; background: #1a1a1a; }
                
                /* Filter for dark mode (inverted by default in current setup) */
                .dark-tiles { filter: invert(100%) hue-rotate(180deg) brightness(95%) contrast(90%); }
                
                /* Zoom controls styling */
                .leaflet-control-zoom { border: none !important; box-shadow: 0 2px 5px rgba(0,0,0,0.5) !important; }
                .leaflet-control-zoom-in, .leaflet-control-zoom-out { 
                    background-color: #333 !important; 
                    color: white !important; 
                    border-bottom: 1px solid #444 !important;
                }
                
                /* Layer toggle button styling */
                .layer-toggle {
                    position: absolute;
                    top: 10px;
                    right: 10px;
                    z-index: 1000;
                    background: #333;
                    color: white;
                    border: none;
                    padding: 8px 12px;
                    border-radius: 4px;
                    font-family: sans-serif;
                    font-size: 12px;
                    cursor: pointer;
                    box-shadow: 0 2px 5px rgba(0,0,0,0.5);
                }
            </style>
        </head>
        <body>
            <button id="layer-btn" class="layer-toggle">Switch to Satellite</button>
            <div id="map"></div>
            <script>
                window.onload = function() {
                    if (typeof L === 'undefined') {
                        document.body.innerHTML = "<div style='color:yellow; background:red; padding:20px; text-align:center;'>ERROR: Leaflet not loaded correctly.</div>";
                        return;
                    }
                    try {
                        var map = L.map('map', { 
                            zoomControl: true, 
                            attributionControl: false 
                        }).setView([$latStr, $lonStr], 13);
                        
                        var osmLayer = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png');
                        var satelliteLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}');
                        
                        osmLayer.addTo(map);
                        
                        // Default to dark mode for OSM as per current design
                        var mapDiv = document.querySelector('.leaflet-tile-container');
                        if (mapDiv) mapDiv.classList.add('dark-tiles');
                        
                        var currentLayer = 'osm';
                        var btn = document.getElementById('layer-btn');
                        
                        btn.onclick = function() {
                            if (currentLayer === 'osm') {
                                map.removeLayer(osmLayer);
                                satelliteLayer.addTo(map);
                                btn.innerHTML = 'Switch to Map';
                                currentLayer = 'sat';
                            } else {
                                map.removeLayer(satelliteLayer);
                                osmLayer.addTo(map);
                                btn.innerHTML = 'Switch to Satellite';
                                currentLayer = 'osm';
                            }
                        };

                        var marker = L.marker([$latStr, $lonStr], { draggable: true }).addTo(map);
                        
                        map.on('click', function(e) {
                            marker.setLatLng(e.latlng);
                            AndroidBridge.onMapClick(e.latlng.lat, e.latlng.lng);
                        });
                        marker.on('dragend', function(e) {
                            var pos = marker.getLatLng();
                            AndroidBridge.onMapClick(pos.lat, pos.lng);
                        });
                    } catch(e) {
                        document.body.innerHTML = "<div style='color:white;'>" + e.message + "</div>";
                    }
                };
            </script>
        </body>
        </html>
    """.trimIndent()
}
