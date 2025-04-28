# WiFi Signal Strength Scanner App

This Android application logs WiFi signal strength readings over multiple scans at different physical locations and visualizes how the received signal strength (RSS) ranges differ across locations. The app is built using Kotlin and Jetpack Compose with a modern dark theme UI.

## Features

- Scan WiFi access points at three distinct physical locations
- Collect 100 scans at each location to establish reliable signal strength ranges
- Visualize and compare signal strength ranges for common WiFi networks across locations
- Modern dark theme UI with gradient text elements

## Technical Implementation

### Architecture Overview

The app follows a simple architecture with the following key components:

1. **MainActivity**: Handles WiFi scanning functionality and data collection
2. **WifiScannerUI**: Contains all UI components and layout structures
3. **Theme**: Implements a custom dark theme with a specific color scheme

### WiFi Scanning Mechanism

The app implements a robust WiFi scanning approach that:

1. Performs 100 consecutive scans at each location
2. Records signal strength for each detected WiFi access point
3. Calculates the minimum and maximum signal strength values
4. Handles scan failures and timeouts gracefully

#### Key Components:

- **WifiManager**: Used to access Android's WiFi scanning functionality
- **BroadcastReceiver**: Listens for scan results
- **Timeout Handling**: Prevents the app from hanging if scan results aren't received
- **Progress Tracking**: Shows scan progress to the user

```kotlin
private fun performSingleScan() {
    if (!isScanning.value) return
    
    try {
        // set waiting flag before starting scan
        waitingForScanResults = true
        
        // set timeout for this scan (3 seconds)
        handler.postDelayed(scanTimeoutRunnable, 3000)
        
        // start the scan
        val success = wifiManager.startScan()
        
        if (!success) {
            // manually trigger the timeout in case of a failed scan
            handler.postDelayed({
                if (waitingForScanResults) {
                    scanTimeoutRunnable.run()
                }
            }, 500)
        }
    } catch (e: Exception) {
        // error handling...
    }
}
```

### Data Model

The app uses two main data structures:

1. **In-memory storage during scanning**:
   - Maps from BSSID to list of signal strengths for each location
   - Stores raw scan data for statistical processing

```kotlin
private val location1Results = mutableStateOf<Map<String, MutableList<Int>>>(mutableMapOf())
private val location2Results = mutableStateOf<Map<String, MutableList<Int>>>(mutableMapOf())
private val location3Results = mutableStateOf<Map<String, MutableList<Int>>>(mutableMapOf())
```

2. **Display data model**:
   - Processed data for UI display with statistical calculations
   - Contains min/max signal strengths and range differences

```kotlin
data class WifiData(
    val ssid: String,
    val bssid: String,
    val minSignalStrength: Int,
    val maxSignalStrength: Int,
    val rangeDifference: Int,
    val samplesCount: Int
)
```

### UI Implementation

The UI is implemented using Jetpack Compose with a custom dark theme featuring:

1. **Color Scheme**:
   - Dark Background: `#0F1316`
   - Dark Text: `#122026`
   - Light Text: `#D9EDDF`
   - Light Green: `#23E09C`
   - Light Blue: `#27AAF4`
   - Red Color: `#DE5753`
   - White: `#FFFFFF`

2. **Custom Components**:
   - **GradientText**: Creates text with horizontal gradient from LightGreen to LightBlue
   - **LocationStatusIndicator**: Shows scan status for each location
   - **WifiNetworkItem**: Card displaying WiFi network details
   - **ComparisonItem**: Presents comparison data between locations

3. **Responsive Layout**:
   - Adapts to different screen sizes
   - Shows appropriate UI states based on scanning status

```kotlin
@Composable
fun GradientTextSimple(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    startColor: Color = LightGreen,
    endColor: Color = LightBlue
) {
    val gradientColors = listOf(startColor, endColor)
    
    Text(
        text = text,
        style = TextStyle(
            brush = Brush.horizontalGradient(colors = gradientColors),
            fontSize = fontSize,
            fontWeight = fontWeight
        ),
        modifier = modifier
    )
}
```

### Permissions

The app requires the following permissions:
- `ACCESS_FINE_LOCATION`: Required for WiFi scanning on modern Android versions
- `ACCESS_COARSE_LOCATION`: Alternative location permission
- `ACCESS_WIFI_STATE`: To access WiFi information
- `CHANGE_WIFI_STATE`: To initiate WiFi scans

## User Flow

1. **Initial Setup**:
   - User grants necessary permissions
   - App presents three location options with status indicators

2. **Data Collection**:
   - User selects Location 1 and taps "Start 100 Scans"
   - App performs 100 WiFi scans and displays progress
   - User moves to Location 2, selects it, and repeats
   - Process is repeated for Location 3

3. **Data Visualization**:
   - After scanning all three locations, "Compare RSS Ranges" button is enabled
   - User can view signal strength ranges for networks detected in all locations
   - User can toggle between comparison view and individual location data

## Technical Challenges and Solutions

### 1. Scan Throttling

**Challenge**: Android limits the frequency of WiFi scans to preserve battery life.

**Solution**: Implemented a robust scanning mechanism with timeout handling to ensure progress even if scans are throttled.

### 2. Reliability of Scan Results

**Challenge**: WiFi scan results can be inconsistent or fail entirely.

**Solution**: Performed 100 scans at each location to establish reliable signal strength ranges and implemented error recovery.

### 3. UI State Management

**Challenge**: Maintaining clear UI state during scanning and completion.

**Solution**: Used Compose state management to track scanning status and location completion, with appropriate UI feedback.

## Implementation Notes

- **Kotlin DSL** is used for Gradle build files
- **Jetpack Compose** is used for the modern UI implementation
- **Material Design 3** components are used with a custom theme
- **Coroutines** and **Flows** are avoided in favor of simpler state management for this specific use case

## Building and Running

1. Clone the repository
2. Open in Android Studio
3. Run on a physical device (emulators may not provide real WiFi data)
4. Grant location permissions when prompted
5. Move to different physical locations for each scanning session

## Conclusion

This app demonstrates effective implementation of:
- WiFi scanning and signal strength analysis
- Robust error handling and timeout management
- Modern UI with Jetpack Compose
- Custom theming and gradient effects
- Data collection and statistical processing
