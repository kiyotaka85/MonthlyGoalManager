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

    // Higher Goal関連のメソッド
    val allHigherGoals: Flow<List<HigherGoal>> = higherGoalDao.getAllHigherGoals()

    suspend fun addHigherGoal(higherGoal: HigherGoal) {
        higherGoalDao.insertHigherGoal(higherGoal)
    }

    suspend fun updateHigherGoal(higherGoal: HigherGoal) {
        higherGoalDao.updateHigherGoal(higherGoal)
    }

    suspend fun deleteHigherGoal(higherGoal: HigherGoal) {
        higherGoalDao.deleteHigherGoal(higherGoal)
    }

    suspend fun getHigherGoalById(id: UUID): HigherGoal? {
        return higherGoalDao.getHigherGoalById(id)
    }

    // Action Step関連のメソッド
    @Deprecated("ActionStep機能は廃止予定です。今後は使用しないでください。")
    fun getActionStepsForGoal(goalId: UUID): Flow<List<ActionStep>> {
        return actionStepDao.getActionStepsForGoal(goalId)
    }

    @Deprecated("ActionStep機能は廃止予定です。今後は使用しないでください。")
    suspend fun addActionStep(actionStep: ActionStep) {
        actionStepDao.insertActionStep(actionStep)
    }

    @Deprecated("ActionStep機能は廃止予定です。今後は使用しないでください。")
    suspend fun updateActionStep(actionStep: ActionStep) {
        actionStepDao.updateActionStep(actionStep)
    }

    @Deprecated("ActionStep機能は廃止予定です。今後は使用しないでください。")
    suspend fun deleteActionStep(actionStep: ActionStep) {
        actionStepDao.deleteActionStep(actionStep)
    }

    // Monthly Review関連のメソッド
    val allMonthlyReviews: Flow<List<MonthlyReview>> = monthlyReviewDao.getAllMonthlyReviews()

    suspend fun addMonthlyReview(monthlyReview: MonthlyReview) {
        monthlyReviewDao.insertMonthlyReview(monthlyReview)
    }

    suspend fun updateMonthlyReview(monthlyReview: MonthlyReview) {
        monthlyReviewDao.updateMonthlyReview(monthlyReview)
    }

    suspend fun deleteMonthlyReview(monthlyReview: MonthlyReview) {
        monthlyReviewDao.deleteMonthlyReview(monthlyReview)
    }

    suspend fun getMonthlyReview(year: Int, month: Int): MonthlyReview? {
        return monthlyReviewDao.getMonthlyReview(year, month)
    }

    // hasMonthlyReviewメソッドを追加
    fun hasMonthlyReview(year: Int, month: Int): Flow<Boolean> {
        return monthlyReviewDao.hasMonthlyReview(year, month)
    }

    // insertMonthlyReviewメソッドを追加（重複していた部分を削除）
    suspend fun insertMonthlyReview(monthlyReview: MonthlyReview) {
        monthlyReviewDao.insertMonthlyReview(monthlyReview)
    }

    // Final CheckIn関連のメソッド
    val allFinalCheckIns: Flow<List<FinalCheckIn>> = finalCheckInDao.getAllFinalCheckIns()

    suspend fun addFinalCheckIn(finalCheckIn: FinalCheckIn) {
        finalCheckInDao.insertFinalCheckIn(finalCheckIn)
    }

    suspend fun updateFinalCheckIn(finalCheckIn: FinalCheckIn) {
        finalCheckInDao.updateFinalCheckIn(finalCheckIn)
    }

    suspend fun deleteFinalCheckIn(finalCheckIn: FinalCheckIn) {
        finalCheckInDao.deleteFinalCheckIn(finalCheckIn)
    }

    fun getFinalCheckInsForGoal(goalId: UUID): Flow<List<FinalCheckIn>> {
        return finalCheckInDao.getFinalCheckInsForGoal(goalId)
    }

    suspend fun getFinalCheckInByReview(reviewId: UUID): List<FinalCheckIn> {
        return finalCheckInDao.getFinalCheckInByReview(reviewId)
    }

    // getFinalCheckInsForReviewメソッドを追加
    fun getFinalCheckInsForReview(reviewId: UUID): Flow<List<FinalCheckIn>> {
        return finalCheckInDao.getFinalCheckInsForReview(reviewId)
    }

    // getFinalCheckInForGoalメソッドを追加
    suspend fun getFinalCheckInForGoal(goalId: UUID, reviewId: UUID): FinalCheckIn? {
        return finalCheckInDao.getFinalCheckInForGoal(goalId, reviewId)
    }

    // JSON形式でのデータエクスポート・インポート用のメソッド
    suspend fun getAllGoalsOnce(): List<GoalItem> {
        return goalDao.getAllGoalsOnce()
    }

    suspend fun getAllHigherGoalsOnce(): List<HigherGoal> {
        return higherGoalDao.getAllHigherGoalsOnce()
    }

    @Deprecated("ActionStep機能は廃止予定です。今後は使用しないでください。")
    suspend fun getAllActionStepsOnce(): List<ActionStep> {
        return actionStepDao.getAllActionStepsOnce()
    }

    suspend fun getAllCheckInsOnce(): List<CheckInItem> {
        return checkInDao.getAllCheckInsOnce()
    }

    suspend fun getAllMonthlyReviewsOnce(): List<MonthlyReview> {
        return monthlyReviewDao.getAllMonthlyReviewsOnce()
    }

    suspend fun getAllFinalCheckInsOnce(): List<FinalCheckIn> {
        return finalCheckInDao.getAllFinalCheckInsOnce()
    }

    // インポート用のメソッド
    suspend fun insertGoal(goal: GoalItem) {
        goalDao.upsertGoal(goal)
    }

    suspend fun insertHigherGoal(higherGoal: HigherGoal) {
        higherGoalDao.insertHigherGoal(higherGoal)
    }

    @Deprecated("ActionStep機能は廃止予定です。今後は使用しないでください。")
    suspend fun insertActionStep(actionStep: ActionStep) {
        actionStepDao.insertActionStep(actionStep)
    }

    suspend fun insertCheckIn(checkIn: CheckInItem) {
        checkInDao.insertCheckIn(checkIn)
    }

    suspend fun insertFinalCheckIn(finalCheckIn: FinalCheckIn) {
        finalCheckInDao.insertFinalCheckIn(finalCheckIn)
    }

    // 全データ削除用のメソッド（インポート時にreplaceExisting=trueの場合に使用）
    suspend fun deleteAllData() {
        finalCheckInDao.deleteAllFinalCheckIns()
        monthlyReviewDao.deleteAllMonthlyReviews()
        checkInDao.deleteAllCheckIns()
        actionStepDao.deleteAllActionSteps()
        goalDao.deleteAllGoals()
        higherGoalDao.deleteAllHigherGoals()
    }
}