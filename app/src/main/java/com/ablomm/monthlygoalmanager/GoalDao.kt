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

    // JSON エクスポート用の一括取得
    @Query("SELECT * FROM goals")
    suspend fun getAllGoalsOnce(): List<GoalItem>

    // 全データ削除（インポート時のリセット用）
    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals()
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

    // JSON エクスポート用の一括取得
    @Query("SELECT * FROM check_ins")
    suspend fun getAllCheckInsOnce(): List<CheckInItem>

    // 全データ削除（インポート時のリセット用）
    @Query("DELETE FROM check_ins")
    suspend fun deleteAllCheckIns()
}

@Dao
interface MonthlyReviewDao {
    // 全ての月次レビューを取得
    @Query("SELECT * FROM monthly_reviews ORDER BY year DESC, month DESC")
    fun getAllMonthlyReviews(): Flow<List<MonthlyReview>>

    // 特定の年月の月次レビューを取得
    @Query("SELECT * FROM monthly_reviews WHERE year = :year AND month = :month")
    suspend fun getMonthlyReview(year: Int, month: Int): MonthlyReview?
    
    // 月次レビューを追加
    @Insert
    suspend fun insertMonthlyReview(monthlyReview: MonthlyReview)

    // 月次レビューを更新
    @Update
    suspend fun updateMonthlyReview(monthlyReview: MonthlyReview)

    // 月次レビューを削除
    @Delete
    suspend fun deleteMonthlyReview(monthlyReview: MonthlyReview)

    // JSON エクスポート用の一括取得
    @Query("SELECT * FROM monthly_reviews")
    suspend fun getAllMonthlyReviewsOnce(): List<MonthlyReview>

    // 全データ削除（インポート時のリセット用）
    @Query("DELETE FROM monthly_reviews")
    suspend fun deleteAllMonthlyReviews()

    // hasMonthlyReviewメソッドを追加
    @Query("SELECT EXISTS(SELECT 1 FROM monthly_reviews WHERE year = :year AND month = :month)")
    fun hasMonthlyReview(year: Int, month: Int): Flow<Boolean>
}

@Dao
interface FinalCheckInDao {
    // 全ての最終チェックインを取得
    @Query("SELECT * FROM final_checkins ORDER BY goalId")
    fun getAllFinalCheckIns(): Flow<List<FinalCheckIn>>

    // 特定の目標の最終チェックインを取得
    @Query("SELECT * FROM final_checkins WHERE goalId = :goalId")
    fun getFinalCheckInsForGoal(goalId: UUID): Flow<List<FinalCheckIn>>

    // 特定のレビューの最終チェックインを取得
    @Query("SELECT * FROM final_checkins WHERE monthlyReviewId = :reviewId")
    suspend fun getFinalCheckInByReview(reviewId: UUID): List<FinalCheckIn>

    // getFinalCheckInsForReviewメソッドを追加（Flowバージョン）
    @Query("SELECT * FROM final_checkins WHERE monthlyReviewId = :reviewId")
    fun getFinalCheckInsForReview(reviewId: UUID): Flow<List<FinalCheckIn>>

    // getFinalCheckInForGoalメソッドを追加
    @Query("SELECT * FROM final_checkins WHERE goalId = :goalId AND monthlyReviewId = :reviewId LIMIT 1")
    suspend fun getFinalCheckInForGoal(goalId: UUID, reviewId: UUID): FinalCheckIn?

    // 最終チェックインを追加
    @Insert
    suspend fun insertFinalCheckIn(finalCheckIn: FinalCheckIn)

    // 最終チェックインを更新
    @Update
    suspend fun updateFinalCheckIn(finalCheckIn: FinalCheckIn)

    // 最終チェックインを削除
    @Delete
    suspend fun deleteFinalCheckIn(finalCheckIn: FinalCheckIn)

    // JSON エクスポート用の一括取得
    @Query("SELECT * FROM final_checkins")
    suspend fun getAllFinalCheckInsOnce(): List<FinalCheckIn>

    // 全データ削除（インポート時のリセット用）
    @Query("DELETE FROM final_checkins")
    suspend fun deleteAllFinalCheckIns()
}

@Dao
interface HigherGoalDao {
    // 全ての上位目標を取得
    @Query("SELECT * FROM higher_goals ORDER BY createdAt DESC")
    fun getAllHigherGoals(): Flow<List<HigherGoal>>
    
    // IDを指定して単一の上位目標を取得
    @Query("SELECT * FROM higher_goals WHERE id = :id")
    suspend fun getHigherGoalById(id: UUID): HigherGoal?
    
    // 上位目標を追加
    @Insert
    suspend fun insertHigherGoal(higherGoal: HigherGoal)
    
    // 上位目標を更新
    @Update
    suspend fun updateHigherGoal(higherGoal: HigherGoal)
    
    // 上位目標を削除
    @Delete
    suspend fun deleteHigherGoal(higherGoal: HigherGoal)

    // JSON エクスポート用の一括取得
    @Query("SELECT * FROM higher_goals")
    suspend fun getAllHigherGoalsOnce(): List<HigherGoal>

    // 全データ削除（インポート時のリセット用）
    @Query("DELETE FROM higher_goals")
    suspend fun deleteAllHigherGoals()
}

@Dao
interface ActionStepDao {
    // 特定の目標のアクションステップを取得
    @Query("SELECT * FROM action_steps WHERE goalId = :goalId ORDER BY `order`")
    fun getActionStepsForGoal(goalId: UUID): Flow<List<ActionStep>>

    // アクションステップを追加
    @Insert
    suspend fun insertActionStep(actionStep: ActionStep)

    // アクションステップを更新
    @Update
    suspend fun updateActionStep(actionStep: ActionStep)

    // アクションステップを削除
    @Delete
    suspend fun deleteActionStep(actionStep: ActionStep)

    // JSON エクスポート用の一括取得
    @Query("SELECT * FROM action_steps")
    suspend fun getAllActionStepsOnce(): List<ActionStep>

    // 全データ削除（インポート時のリセット用）
    @Query("DELETE FROM action_steps")
    suspend fun deleteAllActionSteps()
}
