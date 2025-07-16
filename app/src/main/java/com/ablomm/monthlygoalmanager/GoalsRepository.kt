package com.ablomm.monthlygoalmanager

// GoalsRepository.kt (新しいファイルとして作成)
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GoalsRepository(
    private val goalDao: GoalDao,
    private val checkInDao: CheckInDao,
    private val monthlyReviewDao: MonthlyReviewDao,
    private val finalCheckInDao: FinalCheckInDao,
    private val higherGoalDao: HigherGoalDao,
    private val actionStepDao: ActionStepDao
) {
    val allGoals: Flow<List<GoalItem>> = goalDao.getAllGoals()

    suspend fun getGoalById(id: UUID): GoalItem? {
        return goalDao.getGoalById(id)
    }

    suspend fun updateGoal(goal: GoalItem) {
        goalDao.upsertGoal(goal)
    }

    suspend fun addGoal(goal: GoalItem) {
        goalDao.upsertGoal(goal)
    }

    suspend fun deleteGoal(goal: GoalItem) {
        goalDao.deleteGoal(goal)
    }

    // CheckIn関連のメソッド
    fun getCheckInsForGoal(goalId: UUID): Flow<List<CheckInItem>> {
        return checkInDao.getCheckInsForGoal(goalId)
    }

    val allCheckIns: Flow<List<CheckInItem>> = checkInDao.getAllCheckIns()

    suspend fun addCheckIn(checkIn: CheckInItem) {
        checkInDao.insertCheckIn(checkIn)
    }

    suspend fun updateCheckIn(checkIn: CheckInItem) {
        checkInDao.updateCheckIn(checkIn)
    }

    suspend fun deleteCheckIn(checkIn: CheckInItem) {
        checkInDao.deleteCheckIn(checkIn)
    }

    // MonthlyReview関連のメソッド
    suspend fun getMonthlyReview(year: Int, month: Int): MonthlyReview? {
        return monthlyReviewDao.getMonthlyReview(year, month)
    }

    fun hasMonthlyReview(year: Int, month: Int): Flow<Boolean> {
        return monthlyReviewDao.hasMonthlyReview(year, month)
    }

    val allMonthlyReviews: Flow<List<MonthlyReview>> = monthlyReviewDao.getAllMonthlyReviews()

    suspend fun insertMonthlyReview(review: MonthlyReview): Long {
        return monthlyReviewDao.insertMonthlyReview(review)
    }

    suspend fun updateMonthlyReview(review: MonthlyReview) {
        monthlyReviewDao.updateMonthlyReview(review)
    }

    // FinalCheckIn関連のメソッド
    fun getFinalCheckInsForReview(reviewId: UUID): Flow<List<FinalCheckIn>> {
        return finalCheckInDao.getFinalCheckInsForReview(reviewId)
    }

    suspend fun insertFinalCheckIn(checkIn: FinalCheckIn) {
        finalCheckInDao.insertFinalCheckIn(checkIn)
    }

    suspend fun updateFinalCheckIn(checkIn: FinalCheckIn) {
        finalCheckInDao.updateFinalCheckIn(checkIn)
    }

    suspend fun getFinalCheckInForGoal(goalId: UUID, reviewId: UUID): FinalCheckIn? {
        return finalCheckInDao.getFinalCheckInForGoal(goalId, reviewId)
    }

    // HigherGoal関連のメソッド
    val allHigherGoals: Flow<List<HigherGoal>> = higherGoalDao.getAllHigherGoals()

    suspend fun getHigherGoalById(id: UUID): HigherGoal? {
        return higherGoalDao.getHigherGoalById(id)
    }

    suspend fun addHigherGoal(higherGoal: HigherGoal) {
        higherGoalDao.insertHigherGoal(higherGoal)
    }

    suspend fun updateHigherGoal(higherGoal: HigherGoal) {
        higherGoalDao.updateHigherGoal(higherGoal)
    }

    suspend fun deleteHigherGoal(higherGoal: HigherGoal) {
        higherGoalDao.deleteHigherGoal(higherGoal)
    }

    // 月次レビュー削除機能を追加
    suspend fun deleteMonthlyReview(review: MonthlyReview) {
        // まず関連するFinalCheckInを削除
        finalCheckInDao.deleteFinalCheckInsForReview(review.id)
        // その後、月次レビューを削除
        monthlyReviewDao.deleteMonthlyReview(review)
    }

    // ActionStep関連のメソッド
    fun getActionStepsForGoal(goalId: UUID): Flow<List<ActionStep>> {
        return actionStepDao.getActionStepsForGoal(goalId)
    }

    suspend fun addActionStep(actionStep: ActionStep) {
        actionStepDao.insertActionStep(actionStep)
    }

    suspend fun updateActionStep(actionStep: ActionStep) {
        actionStepDao.updateActionStep(actionStep)
    }

    suspend fun deleteActionStep(actionStep: ActionStep) {
        actionStepDao.deleteActionStep(actionStep)
    }
}