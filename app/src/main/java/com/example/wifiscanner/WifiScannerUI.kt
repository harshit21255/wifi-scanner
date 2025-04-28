package com.example.wifiscanner

import android.net.wifi.ScanResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wifiscanner.ui.theme.*

@Composable
fun WifiScannerApp(
    selectedLocation: Int,
    onLocationSelected: (Int) -> Unit,
    isLocation1Scanned: Boolean,
    isLocation2Scanned: Boolean,
    isLocation3Scanned: Boolean,
    onStartScanClicked: () -> Unit,
    onCancelScanClicked: () -> Unit = {},
    scanResults: List<ScanResult>,
    location1Results: List<WifiData>,
    location2Results: List<WifiData>,
    location3Results: List<WifiData>,
    allLocationsScanned: Boolean,
    scanProgress: Int,
    isScanning: Boolean
) {
    var showComparisonResults by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            GradientTextSimple(
                text = "WiFi Signal Strength Scanner",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select Location:",
            fontWeight = FontWeight.Bold,
            color = LightText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedLocation == 1,
                onClick = { onLocationSelected(1) },
                enabled = !isScanning,
                colors = RadioButtonDefaults.colors(
                    selectedColor = LightGreen,
                    unselectedColor = LightText
                )
            )
            Text("Location 1", color = LightText)
            Spacer(modifier = Modifier.width(8.dp))
            LocationStatusIndicator(isScanned = isLocation1Scanned)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedLocation == 2,
                onClick = { onLocationSelected(2) },
                enabled = !isScanning,
                colors = RadioButtonDefaults.colors(
                    selectedColor = LightGreen,
                    unselectedColor = LightText
                )
            )
            Text("Location 2", color = LightText)
            Spacer(modifier = Modifier.width(8.dp))
            LocationStatusIndicator(isScanned = isLocation2Scanned)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedLocation == 3,
                onClick = { onLocationSelected(3) },
                enabled = !isScanning,
                colors = RadioButtonDefaults.colors(
                    selectedColor = LightGreen,
                    unselectedColor = LightText
                )
            )
            Text("Location 3", color = LightText)
            Spacer(modifier = Modifier.width(8.dp))
            LocationStatusIndicator(isScanned = isLocation3Scanned)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isScanning) {
            Text("Scanning Location $selectedLocation: $scanProgress%", color = LightText)
            LinearProgressIndicator(
                progress = scanProgress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = LightGreen,
                trackColor = DarkText
            )

            Button(
                onClick = { onCancelScanClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel Scanning")
            }
        } else {
            Button(
                onClick = { onStartScanClicked() },
                enabled = !isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightText,
                    contentColor = DarkText,
                    disabledContainerColor = LightText.copy(alpha = 0.5f),
                    disabledContentColor = DarkText.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Start 100 Scans at Location $selectedLocation")
            }
        }

        Button(
            onClick = { showComparisonResults = !showComparisonResults },
            enabled = allLocationsScanned,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LightText,
                contentColor = DarkText,
                disabledContainerColor = LightText.copy(alpha = 0.5f),
                disabledContentColor = DarkText.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (showComparisonResults) "Show Current Location Data" else "Compare RSS Ranges Across Locations")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = DarkText,
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                if (showComparisonResults && allLocationsScanned) {
                    ComparisonResults(
                        location1Results = location1Results,
                        location2Results = location2Results,
                        location3Results = location3Results
                    )
                } else {
                    val locationComplete = when (selectedLocation) {
                        1 -> isLocation1Scanned
                        2 -> isLocation2Scanned
                        3 -> isLocation3Scanned
                        else -> false
                    }

                    if (isScanning) {
                        Text(
                            text = "Scanning in progress...",
                            fontWeight = FontWeight.Bold,
                            color = White,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else if (locationComplete) {
                        Column {
                            Text(
                                text = "WiFi Networks at Location $selectedLocation:",
                                fontWeight = FontWeight.Bold,
                                color = White,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            val resultsToShow = when (selectedLocation) {
                                1 -> location1Results
                                2 -> location2Results
                                3 -> location3Results
                                else -> emptyList()
                            }

                            if (resultsToShow.isEmpty()) {
                                Text("No networks found at this location.", color = White)
                            } else {
                                LazyColumn {
                                    items(resultsToShow) { wifiData ->
                                        WifiNetworkItem(wifiData)
                                    }
                                }
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = "Location $selectedLocation has not been scanned yet.",
                                color = White,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Text("Press 'Start 100 Scans' to collect data at this location.", color = LightText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationStatusIndicator(isScanned: Boolean) {
    val color = if (isScanned) LightGreen else RedColor
    val text = if (isScanned) "Scanned" else "Not Scanned"

    Surface(
        modifier = Modifier
            .size(12.dp)
            .padding(end = 4.dp),
        color = color,
        shape = MaterialTheme.shapes.small
    ) { }

    Text(text, fontSize = 12.sp, color = if (isScanned) LightGreen else RedColor)
}

@Composable
fun WifiNetworkItem(wifiData: WifiData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "SSID: ${wifiData.ssid}", fontWeight = FontWeight.Bold, color = LightGreen)
            Text(text = "BSSID: ${wifiData.bssid}", color = LightText)
            Text(text = "Signal Range: ${wifiData.minSignalStrength} to ${wifiData.maxSignalStrength} dBm", color = White)
            Text(text = "Range Difference: ${wifiData.rangeDifference} dBm", color = LightBlue)
            Text(text = "Samples: ${wifiData.samplesCount}", color = LightText)
        }
    }
}

@Composable
fun ComparisonResults(
    location1Results: List<WifiData>,
    location2Results: List<WifiData>,
    location3Results: List<WifiData>
) {
    val location1Bssids = location1Results.map { it.bssid }.toSet()
    val location2Bssids = location2Results.map { it.bssid }.toSet()
    val location3Bssids = location3Results.map { it.bssid }.toSet()

    val commonBssids = location1Bssids.intersect(location2Bssids).intersect(location3Bssids)

    val comparisons = commonBssids.map { bssid ->
        val ap1 = location1Results.first { it.bssid == bssid }
        val ap2 = location2Results.first { it.bssid == bssid }
        val ap3 = location3Results.first { it.bssid == bssid }

        LocationComparison(
            ssid = ap1.ssid,
            bssid = bssid,
            location1Range = "${ap1.minSignalStrength} to ${ap1.maxSignalStrength} dBm (Δ${ap1.rangeDifference})",
            location2Range = "${ap2.minSignalStrength} to ${ap2.maxSignalStrength} dBm (Δ${ap2.rangeDifference})",
            location3Range = "${ap3.minSignalStrength} to ${ap3.maxSignalStrength} dBm (Δ${ap3.rangeDifference})"
        )
    }

    Column {
        Text(
            text = "Signal Strength Comparison (Common WiFi Networks)",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (comparisons.isEmpty()) {
            Text("No common WiFi networks found across all three locations.", color = White)
        } else {
            LazyColumn {
                items(comparisons) { comparison ->
                    ComparisonItem(comparison)
                }
            }
        }
    }
}

data class LocationComparison(
    val ssid: String,
    val bssid: String,
    val location1Range: String,
    val location2Range: String,
    val location3Range: String
)

@Composable
fun ComparisonItem(comparison: LocationComparison) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = comparison.ssid, fontWeight = FontWeight.Bold, color = LightGreen)
            Text(text = "BSSID: ${comparison.bssid}", color = LightText)
            Text(text = "Location 1: ${comparison.location1Range}", color = White)
            Text(text = "Location 2: ${comparison.location2Range}", color = White)
            Text(text = "Location 3: ${comparison.location3Range}", color = White)
        }
    }
}