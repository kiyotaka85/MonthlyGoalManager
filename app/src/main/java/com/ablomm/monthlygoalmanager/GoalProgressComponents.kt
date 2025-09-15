// filepath: /Users/kiyotaka/AndroidStudioProjects/MonthlyGoalManager/app/src/main/java/com/ablomm/monthlygoalmanager/GoalProgressComponents.kt
package com.ablomm.monthlygoalmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun GoalProgressIndicatorWithBubble(
    goal: GoalItem,
    modifier: Modifier = Modifier
) {
    val progress = calculateProgressPrecise(
        startValue = goal.startNumericValue,
        targetValue = goal.targetNumericValue,
        currentValue = goal.currentNumericValue
    )
    val progressFraction = ((progress / 100.0).toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val barHeight = 10.dp
            val bubbleHeight = 28.dp
            val bubbleMinWidth = 64.dp
            val corner = 6.dp

            // Progress bar
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(RoundedCornerShape(corner)),
                color = progressColor(progress),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Use scope's constraints explicitly
            val scopeMaxWidth = this.maxWidth
            // Bubble positioned above the current progress point
            val maxStart = (scopeMaxWidth - bubbleMinWidth).coerceAtLeast(0.dp)
            val xDp = maxStart * progressFraction
            val bubbleWidth = bubbleMinWidth
            val pointerOffset = xDp + bubbleWidth.times(0.5f) - 6.dp

            Column(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .height(bubbleHeight + 8.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Bubble box
                Box(
                    modifier = Modifier
                        .offset(x = xDp)
                        .width(bubbleWidth)
                        .height(bubbleHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.inverseSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${progress.roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        textAlign = TextAlign.Center
                    )
                }
                // Pointer (small diamond)
                Box(
                    modifier = Modifier
                        .offset(x = pointerOffset)
                        .size(12.dp)
                        .rotate(45f)
                        .background(MaterialTheme.colorScheme.inverseSurface)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatNumber(goal.currentNumericValue, goal.isDecimal),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = progressColor(progress)
            )
            Text(
                text = "${formatNumber(goal.targetNumericValue, goal.isDecimal)}${goal.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun progressColor(progress: Double): Color {
    return when {
        progress >= 100 -> Color(0xFF4CAF50) // Green
        progress >= 75 -> MaterialTheme.colorScheme.primary
        progress >= 50 -> Color(0xFFFFC107) // Amber
        else -> MaterialTheme.colorScheme.error
    }
}
