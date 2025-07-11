package com.ablomm.monthlygoalmanager.data.database

import androidx.room.*
import com.ablomm.monthlygoalmanager.*

/**
 * Room database for the Monthly Goal Manager application
 * 
 * This database contains all the entities and provides DAOs for data access.
 * 
 * Entities:
 * - GoalItem: Monthly goals with progress tracking
 * - CheckInItem: Progress check-ins for goals
 * - MonthlyReview: Monthly reflection and review data
 * - FinalCheckIn: Final assessment of goals at month end
 * - HigherGoal: Long-term goals that span multiple months
 */
@Database(
    entities = [
        GoalItem::class,
        CheckInItem::class,
        MonthlyReview::class,
        FinalCheckIn::class,
        HigherGoal::class
    ],
    version = 6,
    exportSchema = false // Set to true in production for schema versioning
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to goal-related database operations
     */
    abstract fun goalDao(): GoalDao

    /**
     * Provides access to check-in database operations
     */
    abstract fun checkInDao(): CheckInDao

    /**
     * Provides access to monthly review database operations
     */
    abstract fun monthlyReviewDao(): MonthlyReviewDao

    /**
     * Provides access to final check-in database operations
     */
    abstract fun finalCheckInDao(): FinalCheckInDao

    /**
     * Provides access to higher goal database operations
     */
    abstract fun higherGoalDao(): HigherGoalDao
}
