package com.example.screentimemanager.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class UsageGuardService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Monitor for restricted apps and launch ShieldActivity when needed.
    }

    override fun onInterrupt() {
    }
}
