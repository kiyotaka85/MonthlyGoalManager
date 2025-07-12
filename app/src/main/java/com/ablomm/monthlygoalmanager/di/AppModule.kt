package com.ablomm.monthlygoalmanager.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.ablomm.monthlygoalmanager.*
import com.ablomm.monthlygoalmanager.data.database.AppDatabase
import com.ablomm.monthlygoalmanager.data.repository.GoalsRepository
import com.ablomm.monthlygoalmanager.data.preferences.PreferencesManager

/**
 * Hilt dependency injection module
 * Provides application-wide singleton dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the Room database instance
     * Configured with fallback to destructive migration for development
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context, 
            AppDatabase::class.java, 
            "goals_database"
        )
            .fallbackToDestructiveMigration() // Allow destructive migrations in development
            .addCallback(createDatabaseCallback(context))
            .build()
    }

    /**
     * Creates database callback for initialization
     */
    private fun createDatabaseCallback(context: Context): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Database initialization logic can be added here
                // For initial data seeding, consider using a separate migration
            }
        }
    }

    // Data Access Objects (DAOs)

    @Provides
    @Singleton
    fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()

    @Provides
    @Singleton
    fun provideCheckInDao(db: AppDatabase): CheckInDao = db.checkInDao()

    @Provides
    @Singleton
    fun provideMonthlyReviewDao(db: AppDatabase): MonthlyReviewDao = db.monthlyReviewDao()

    @Provides
    @Singleton
    fun provideFinalCheckInDao(db: AppDatabase): FinalCheckInDao = db.finalCheckInDao()

    @Provides
    @Singleton
    fun provideHigherGoalDao(db: AppDatabase): HigherGoalDao = db.higherGoalDao()

    // Repository

    /**
     * Provides the main repository with all required DAOs
     */
    @Provides
    @Singleton
    fun provideGoalsRepository(
        goalDao: GoalDao, 
        checkInDao: CheckInDao,
        monthlyReviewDao: MonthlyReviewDao,
        finalCheckInDao: FinalCheckInDao,
        higherGoalDao: HigherGoalDao
    ): GoalsRepository = GoalsRepository(
        goalDao = goalDao,
        checkInDao = checkInDao,
        monthlyReviewDao = monthlyReviewDao,
        finalCheckInDao = finalCheckInDao,
        higherGoalDao = higherGoalDao
    )

    // Preferences

    /**
     * Provides preferences manager for app settings
     */
    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
}
