package com.ablomm.monthlygoalmanager

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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

    // バージョン13から14へのマイグレーション（colorをiconに変更）
    private val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 1. 新しいiconカラムを追加
            database.execSQL("ALTER TABLE higher_goals ADD COLUMN icon TEXT NOT NULL DEFAULT 'EmojiEvents'")

            // 2. 既存のcolorデータに基づいてiconを設定
            // 色からアイコンへのマッピング
            val colorToIconMap = mapOf(
                "#2196F3" to "EmojiEvents",  // 青 -> トロフィー
                "#4CAF50" to "TrendingUp",   // 緑 -> 成長
                "#FF9800" to "Lightbulb",    // オレンジ -> アイデア
                "#9C27B0" to "Star",         // 紫 -> 星
                "#F44336" to "Work",         // 赤 -> 仕事
                "#00BCD4" to "School",       // シアン -> 学習
                "#FFEB3B" to "FitnessCenter", // 黄 -> フィットネス
                "#795548" to "Home"          // 茶 -> 家庭
            )

            // 各色に対応する���イコンを設定
            colorToIconMap.forEach { (color, icon) ->
                database.execSQL("UPDATE higher_goals SET icon = ? WHERE color = ?", arrayOf(icon, color))
            }

            // 3. colorカラムを削除
            database.execSQL("""
                CREATE TABLE higher_goals_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    icon TEXT NOT NULL DEFAULT 'EmojiEvents',
                    createdAt INTEGER NOT NULL,
                    isCompleted INTEGER NOT NULL DEFAULT 0,
                    completedAt INTEGER
                )
            """)

            database.execSQL("""
                INSERT INTO higher_goals_new (id, title, description, icon, createdAt, isCompleted, completedAt)
                SELECT id, title, description, icon, createdAt, isCompleted, completedAt
                FROM higher_goals
            """)

            database.execSQL("DROP TABLE higher_goals")
            database.execSQL("ALTER TABLE higher_goals_new RENAME TO higher_goals")
        }
    }

    @Provides
    @Singleton // アプリ内で常に同じインスタンス（一つだけ）を使う
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "goals_database")
            .addMigrations(MIGRATION_13_14)
            .fallbackToDestructiveMigration() // 開発中は破壊的変更を許可
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
    fun provideHigherGoalDao(db: AppDatabase): HigherGoalDao = db.higherGoalDao()

    @Provides
    @Singleton
    fun provideActionStepDao(db: AppDatabase): ActionStepDao = db.actionStepDao() // DEPRECATED: ActionStep機能は廃止予定

    @Provides
    @Singleton
    fun provideGoalsRepository(
        goalDao: GoalDao,
        checkInDao: CheckInDao,
        monthlyReviewDao: MonthlyReviewDao,
        finalCheckInDao: FinalCheckInDao,
        higherGoalDao: HigherGoalDao,
        actionStepDao: ActionStepDao
    ): GoalsRepository {
        return GoalsRepository(goalDao, checkInDao, monthlyReviewDao, finalCheckInDao, higherGoalDao, actionStepDao)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }

    // DataExportImportManagerの依存関係を追加
    @Provides
    @Singleton
    fun provideDataExportImportManager(repository: GoalsRepository): DataExportImportManager {
        return DataExportImportManager(repository)
    }
}