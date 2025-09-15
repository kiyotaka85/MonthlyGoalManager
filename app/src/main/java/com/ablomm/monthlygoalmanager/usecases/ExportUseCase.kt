package com.ablomm.monthlygoalmanager.usecases

import android.content.Intent
import com.ablomm.monthlygoalmanager.GoalItem
import com.ablomm.monthlygoalmanager.HigherGoal

/**
 * Use case interface for exporting goals data.
 * This interface is kept for future implementation of export functionality.
 */
interface ExportUseCase {
    /**
     * Exports goals to a specific format.
     * 
     * @param goals List of goals to export
     * @param higherGoals List of higher goals for context
     * @param yearMonth The year-month string for the report
     * @return Intent for sharing the exported data, or null if export fails
     */
    suspend fun exportGoals(
        goals: List<GoalItem>,
        higherGoals: List<HigherGoal>,
        yearMonth: String
    ): Intent?
}