package com.example.wifiscanner

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.wifiscanner.ui.theme.WifiScannerTheme

class MainActivity : ComponentActivity() {

    private lateinit var wifiManager: WifiManager
    private val scanResults = mutableStateOf<List<ScanResult>>(emptyList())

    private val location1Results = mutableStateOf<Map<String, MutableList<Int>>>(mutableMapOf())
    private val location2Results = mutableStateOf<Map<String, MutableList<Int>>>(mutableMapOf())
    private val location3Results = mutableStateOf<Map<String, MutableList<Int>>>(mutableMapOf())

    private val isLocation1Scanned = mutableStateOf(false)
    private val isLocation2Scanned = mutableStateOf(false)
    private val isLocation3Scanned = mutableStateOf(false)

    private val selectedLocation = mutableStateOf(1)

    private var scanCount = 0
    private val totalScansRequired = 100
    private val scanProgress = mutableStateOf(0)
    private val isScanning = mutableStateOf(false)

    private var waitingForScanResults = false

    private val handler = Handler(Looper.getMainLooper())

    private val ssidMap = mutableMapOf<String, String>()

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!isScanning.value) return

            waitingForScanResults = false
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)

            processScanResults()

            scanCount++
            scanProgress.value = (scanCount * 100) / totalScansRequired

            if (scanCount < totalScansRequired) {
                handler.postDelayed({
                    performSingleScan()
                }, 500)
            } else {
                when (selectedLocation.value) {
                    1 -> isLocation1Scanned.value = true
                    2 -> isLocation2Scanned.value = true
                    3 -> isLocation3Scanned.value = true
                }
                isScanning.value = false
                scanProgress.value = 100
                Toast.makeText(context, "Completed 100 scans at Location ${selectedLocation.value}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val scanTimeoutRunnable = Runnable {
        if (waitingForScanResults && isScanning.value) {
            waitingForScanResults = false

            processScanResults()

            scanCount++
            scanProgress.value = (scanCount * 100) / totalScansRequired

            if (scanCount < totalScansRequired) {
                handler.postDelayed({ performSingleScan() }, 500)
            } else {
                when (selectedLocation.value) {
                    1 -> isLocation1Scanned.value = true
                    2 -> isLocation2Scanned.value = true
                    3 -> isLocation3Scanned.value = true
                }
                isScanning.value = false
                scanProgress.value = 100
                Toast.makeText(this, "Completed scanning (with timeouts) at Location ${selectedLocation.value}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions denied. Cannot scan WiFi networks.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        registerReceiver(
            wifiScanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )

        requestPermissions()

        setContent {
            WifiScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WifiScannerApp(
                        selectedLocation = selectedLocation.value,
                        onLocationSelected = {
                            if (!isScanning.value) selectedLocation.value = it
                        },
                        isLocation1Scanned = isLocation1Scanned.value,
                        isLocation2Scanned = isLocation2Scanned.value,
                        isLocation3Scanned = isLocation3Scanned.value,
                        onStartScanClicked = { startScanning() },
                        onCancelScanClicked = { cancelScanning() },
                        scanResults = scanResults.value,
                        location1Results = processResultsForDisplay(location1Results.value),
                        location2Results = processResultsForDisplay(location2Results.value),
                        location3Results = processResultsForDisplay(location3Results.value),
                        allLocationsScanned = isLocation1Scanned.value && isLocation2Scanned.value && isLocation3Scanned.value,
                        scanProgress = scanProgress.value,
                        isScanning = isScanning.value
                    )
                }
            }
        }
    }

    private fun processResultsForDisplay(results: Map<String, MutableList<Int>>): List<WifiData> {
        return results.map { (bssid, signalStrengths) ->
            val ssid = ssidMap[bssid] ?: "<Unknown>"
            val minSignal = signalStrengths.minOrNull() ?: 0
            val maxSignal = signalStrengths.maxOrNull() ?: 0

            WifiData(
                ssid = ssid,
                bssid = bssid,
                minSignalStrength = minSignal,
                maxSignalStrength = maxSignal,
                rangeDifference = maxSignal - minSignal,
                samplesCount = signalStrengths.size
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(wifiScanReceiver)
        } catch (e: Exception) {
        }
        handler.removeCallbacksAndMessages(null)
        isScanning.value = false
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun startScanning() {
        if (isScanning.value) return

        scanCount = 0
        scanProgress.value = 0
        isScanning.value = true
        waitingForScanResults = false

        performSingleScan()

        Toast.makeText(this, "Starting 100 scans at Location ${selectedLocation.value}", Toast.LENGTH_SHORT).show()
    }

    private fun cancelScanning() {
        if (isScanning.value) {
            handler.removeCallbacksAndMessages(null)
            isScanning.value = false
            waitingForScanResults = false
            scanProgress.value = 0
            Toast.makeText(this, "Scanning cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performSingleScan() {
        if (!isScanning.value) return

        try {
            waitingForScanResults = true

            handler.postDelayed(scanTimeoutRunnable, 3000)

            val success = wifiManager.startScan()

            if (!success) {
                handler.postDelayed({
                    if (waitingForScanResults) {
                        scanTimeoutRunnable.run()
                    }
                }, 500)
            }
        } catch (e: Exception) {
            waitingForScanResults = false
            handler.removeCallbacks(scanTimeoutRunnable)

            scanCount++
            scanProgress.value = (scanCount * 100) / totalScansRequired

            if (scanCount < totalScansRequired) {
                handler.postDelayed({ performSingleScan() }, 500)
            } else {
                when (selectedLocation.value) {
                    1 -> isLocation1Scanned.value = true
                    2 -> isLocation2Scanned.value = true
                    3 -> isLocation3Scanned.value = true
                }
                isScanning.value = false
                scanProgress.value = 100
            }
        }
    }

    private fun processScanResults() {
        handler.removeCallbacks(scanTimeoutRunnable)

        val results = wifiManager.scanResults
        scanResults.value = results

        val resultMap = when (selectedLocation.value) {
            1 -> location1Results.value.toMutableMap()
            2 -> location2Results.value.toMutableMap()
            3 -> location3Results.value.toMutableMap()
            else -> mutableMapOf()
        }

        for (scanResult in results) {
            val bssid = scanResult.BSSID
            val rss = scanResult.level

            ssidMap[bssid] = scanResult.SSID.ifEmpty { "<Hidden Network>" }

            resultMap.getOrPut(bssid) { mutableListOf() }.add(rss)
        }

        when (selectedLocation.value) {
            1 -> location1Results.value = resultMap
            2 -> location2Results.value = resultMap
            3 -> location3Results.value = resultMap
        }
    }
}

data class WifiData(
    val ssid: String,
    val bssid: String,
    val minSignalStrength: Int,
    val maxSignalStrength: Int,
    val rangeDifference: Int,
    val samplesCount: Int
)