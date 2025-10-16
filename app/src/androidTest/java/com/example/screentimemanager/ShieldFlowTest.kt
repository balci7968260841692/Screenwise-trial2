package com.example.screentimemanager

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.screentimemanager.ui.MainActivity
import org.junit.Rule
import org.junit.Test

class ShieldFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunches() {
        composeRule.onRoot().assertExists()
    }
}
