package com.ablomm.monthlygoalmanager

// AppDatabase.kt (新しいファイルとして作成)
import androidx.room.*

@Database(entities = [GoalItem::class, CheckInItem::class, MonthlyReview::class, FinalCheckIn::class, HigherGoal::class, ActionStep::class], version = 13)
@TypeConverters(Converters::class) // 先ほど作ったConverterを登録
abstract class AppDatabase : RoomDatabase() {

    abstract fun goalDao(): GoalDao // DAOを取得するための抽象メソッド
    abstract fun checkInDao(): CheckInDao // CheckInDAOを取得するための抽象メソッド
    abstract fun monthlyReviewDao(): MonthlyReviewDao // MonthlyReviewDAOを取得するための抽象メソッド
    abstract fun finalCheckInDao(): FinalCheckInDao // FinalCheckInDAOを取得するための抽象メソッド
    abstract fun higherGoalDao(): HigherGoalDao // HigherGoalDAOを取得するための抽象メソッド
    abstract fun actionStepDao(): ActionStepDao // ActionStepDAOを取得するための抽象メソッド
}