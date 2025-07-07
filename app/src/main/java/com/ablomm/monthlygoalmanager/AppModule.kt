package com.ablomm.monthlygoalmanager

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // アプリ全体で共有する設定
object AppModule {

    @Provides
    @Singleton // アプリ内で常に同じインスタンス（一つだけ）を使う
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "goals_database")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // データベースが初めて作成される時に一度だけ実行される
                    // ここで初期データを投入する

                    // データベースのインスタンスを直接使うのではなく、
                    // Hiltが管理するDAOを取得して使うのがより安全です。
                    // ただし、このコールバック内では直接インスタンスを生成する必要があります。
                    val database = provideAppDatabase(context)
                    val dao = database.goalDao()

                }
            })
            .build()
    }

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
    fun provideGoalsRepository(
        goalDao: GoalDao, 
        checkInDao: CheckInDao,
        monthlyReviewDao: MonthlyReviewDao,
        finalCheckInDao: FinalCheckInDao
    ): GoalsRepository = GoalsRepository(goalDao, checkInDao, monthlyReviewDao, finalCheckInDao)
}