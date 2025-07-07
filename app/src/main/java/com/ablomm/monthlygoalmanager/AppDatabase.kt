package com.ablomm.monthlygoalmanager

// AppDatabase.kt (新しいファイルとして作成)
import androidx.room.*

@Database(entities = [GoalItem::class, CheckInItem::class], version = 2)
@TypeConverters(Converters::class) // 先ほど作ったConverterを登録
abstract class AppDatabase : RoomDatabase() {

    abstract fun goalDao(): GoalDao // DAOを取得するための抽象メソッド
    abstract fun checkInDao(): CheckInDao // CheckInDAOを取得するための抽象メソッド
}