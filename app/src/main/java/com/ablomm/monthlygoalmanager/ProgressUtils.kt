// filepath: /Users/kiyotaka/AndroidStudioProjects/MonthlyGoalManager/app/src/main/java/com/ablomm/monthlygoalmanager/ProgressUtils.kt
package com.ablomm.monthlygoalmanager

import java.util.Locale

// Shared numeric formatter for UI
fun formatNumber(value: Double, isDecimal: Boolean): String {
    if (!isDecimal && value % 1.0 == 0.0) {
        return value.toInt().toString()
    }
    return String.format(Locale.getDefault(), "%.1f", value)
}

// Shared precise progress calculator (allows overachievement)
fun calculateProgressPrecise(
    startValue: Double,
    targetValue: Double,
    currentValue: Double
): Double {
    val range = targetValue - startValue
    val progressInRange = currentValue - startValue

    return if (range != 0.0) {
        (progressInRange / range * 100).coerceAtLeast(0.0)
    } else {
        if (currentValue >= targetValue) 100.0 else 0.0
    }
}

