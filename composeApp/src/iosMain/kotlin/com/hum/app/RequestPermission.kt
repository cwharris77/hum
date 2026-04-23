package com.hum.app

import androidx.compose.runtime.Composable

@Composable
actual fun RequestMicrophonePermission() {
    // iOS requests permission automatically when accessing the mic.
    // NSMicrophoneUsageDescription must be set in Info.plist.
}
