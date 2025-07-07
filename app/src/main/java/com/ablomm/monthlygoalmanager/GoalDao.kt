package com.ablomm.monthlygoalmanager

// GoalDao.kt (新しいファイルとして作成)
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface GoalDao {
    // 全ての目標を取得（Flowでラップすると、データ変更時に自動でUIが更新される）
    @Query("SELECT * FROM goals")
    fun getAllGoals(): Flow<List<GoalItem>>

    // IDを指定して単一の目標を取得
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: UUID): GoalItem?

    // データの追加または更新（UPSERT）
    @Upsert
    suspend fun upsertGoal(goal: GoalItem)

    // データの削除
    @Delete
    suspend fun deleteGoal(goal: GoalItem)
}

@Dao
interface CheckInDao {
    // 特定の目標のチェックイン履歴を取得
    @Query("SELECT * FROM check_ins WHERE goalId = :goalId ORDER BY checkInDate DESC")
    fun getCheckInsForGoal(goalId: UUID): Flow<List<CheckInItem>>
    
    // 全てのチェックイン履歴を取得
    @Query("SELECT * FROM check_ins ORDER BY checkInDate DESC")
    fun getAllCheckIns(): Flow<List<CheckInItem>>
    
    // チェックインを追加
    @Insert
    suspend fun insertCheckIn(checkIn: CheckInItem)
    
    // チェックインを更新
    @Update
    suspend fun updateCheckIn(checkIn: CheckInItem)
    
    // チェックインを削除
    @Delete
    suspend fun deleteCheckIn(checkIn: CheckInItem)
}

@Dao
interface MonthlyReviewDao {
    @Query("SELECT * FROM monthly_reviews WHERE year = :year AND month = :month")
    suspend fun getMonthlyReview(year: Int, month: Int): MonthlyReview?
    
    @Query("SELECT * FROM monthly_reviews ORDER BY year DESC, month DESC")
    fun getAllMonthlyReviews(): Flow<List<MonthlyReview>>
    
    @Insert
    suspend fun insertMonthlyReview(review: MonthlyReview): Long
    
    @Update
    suspend fun updateMonthlyReview(review: MonthlyReview)
}

@Dao
interface FinalCheckInDao {
    @Query("SELECT * FROM final_checkins WHERE monthlyReviewId = :reviewId")
    fun getFinalCheckInsForReview(reviewId: UUID): Flow<List<FinalCheckIn>>
    
    @Insert
    suspend fun insertFinalCheckIn(checkIn: FinalCheckIn)
    
    @Update
    suspend fun updateFinalCheckIn(checkIn: FinalCheckIn)
    
    @Query("SELECT * FROM final_checkins WHERE goalId = :goalId AND monthlyReviewId = :reviewId")
    suspend fun getFinalCheckInForGoal(goalId: UUID, reviewId: UUID): FinalCheckIn?
}