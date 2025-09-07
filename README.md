# CarPlayerForAndroid

A modern Android application for car entertainment systems with USB camera support, built for Android 12+ tablets.

## Overview

CarPlayerForAndroid is a specialized Android application designed for car entertainment systems, featuring USB camera integration and modern Android compatibility. The app has been updated to work seamlessly with Android 12+ devices, including Samsung tablets.

## Features

- **Android 12+ Compatibility**: Fully updated for modern Android versions
- **USB Camera Support**: Connect and use USB cameras via OTG
- **Multi-resolution Support**: 480p, 720p, 1080p camera previews
- **Modern UI**: Updated Material Design components
- **Tablet Optimized**: Designed for car tablet installations
- **Real-time Camera Feed**: Live USB camera preview
- **Media Capture**: Photo and video recording capabilities
- **Cross-platform**: Supports ARM architectures (armeabi-v7a, arm64-v8a)

## Requirements

- Android 12+ (API level 31+)
- Device with OTG support
- USB camera compatible with UVC standard
- Minimum 2GB RAM recommended

## Installation

### Prerequisites

1. Android Studio Arctic Fox or later
2. Android SDK with API level 31+
3. USB OTG cable for camera connection

### Building from Source

1. Clone the repository:
```bash
git clone https://github.com/thuyiya/CarPlayerForAndroid.git
cd CarPlayerForAndroid
```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Build the project:
```bash
./gradlew assembleDebug
```

5. Install on your device:
```bash
./gradlew installDebug
```

## Usage

### Basic Setup

1. Connect your USB camera to the Android device via OTG cable
2. Launch the CarPlayerForAndroid app
3. Grant necessary permissions when prompted
4. The app will automatically detect and connect to your USB camera

### Camera Features

- **Live Preview**: Real-time camera feed display
- **Photo Capture**: Take photos with the connected camera
- **Video Recording**: Record videos using the USB camera
- **Settings**: Adjust camera parameters like brightness, contrast, etc.

## Project Structure

```
CarPlayerForAndroid/
├── app/                    # Main application module
├── libausbc/              # Camera library module
├── libuvc/                # USB Video Class library
├── libnative/             # Native C/C++ libraries
└── gradle/                # Gradle wrapper files
```

## Technical Details

- **Target SDK**: 36 (Android 14)
- **Minimum SDK**: 31 (Android 12)
- **Build Tools**: Gradle 8.10, Android Gradle Plugin 8.1.4
- **Language**: Kotlin
- **Architecture**: MVVM with ViewBinding

## Permissions

The app requires the following permissions:
- Camera access
- Storage access (for saving photos/videos)
- USB device access (for OTG cameras)

## Troubleshooting

### Common Issues

1. **Camera not detected**: Ensure OTG cable is properly connected and camera is UVC compatible
2. **App crashes on startup**: Check if device supports Android 12+ and has sufficient RAM
3. **Permission denied**: Grant all required permissions in device settings

### Debug Information

To collect debug logs:
```bash
adb shell logcat -v threadtime > carplayer.log
```

## Contributing

We welcome contributions! Please feel free to submit issues and pull requests.

## Special Thanks

This project is built upon the excellent work of the AUSBC (Android USB Camera) library. We extend our special thanks to:

- **[AUSBC Library](https://github.com/jiangdongguo/AndroidUSBCamera)** - The original USB camera framework that made this project possible
- **[UVCCamera](https://github.com/saki4510t/UVCCamera)** - The foundational UVC camera implementation

## License

```
Copyright 2024 thuyiya

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Contact

- **GitHub**: [@thuyiya](https://github.com/thuyiya)
- **Repository**: [CarPlayerForAndroid](https://github.com/thuyiya/CarPlayerForAndroid)

---

*Built with ❤️ for the Android car entertainment community*
