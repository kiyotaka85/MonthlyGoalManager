package com.ablomm.monthlygoalmanager.data.repository

import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.ablomm.monthlygoalmanager.*

/**
 * Repository class that provides a clean API for data access to the UI layer
 * Combines multiple data sources and provides a single source of truth for the app
 */
@Singleton
class GoalsRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val checkInDao: CheckInDao,
    private val monthlyReviewDao: MonthlyReviewDao,
    private val finalCheckInDao: FinalCheckInDao,
    private val higherGoalDao: HigherGoalDao
) {

    // ========================================
    // Goal Management
    // ========================================

    /**
     * Reactive stream of all goals
     */
    val allGoals: Flow<List<GoalItem>> = goalDao.getAllGoals()

    /**
     * Get a specific goal by ID
     */
    suspend fun getGoalById(id: UUID): GoalItem? = goalDao.getGoalById(id)

    /**
     * Add a new goal or update existing goal
     */
    suspend fun addGoal(goal: GoalItem) = goalDao.upsertGoal(goal)

    /**
     * Update an existing goal
     */
    suspend fun updateGoal(goal: GoalItem) = goalDao.upsertGoal(goal)

    /**
     * Delete a goal
     */
    suspend fun deleteGoal(goal: GoalItem) = goalDao.deleteGoal(goal)

    // ========================================
    // Check-In Management
    // ========================================

    /**
     * Reactive stream of all check-ins
     */
    val allCheckIns: Flow<List<CheckInItem>> = checkInDao.getAllCheckIns()

    /**
     * Get check-ins for a specific goal
     */
    fun getCheckInsForGoal(goalId: UUID): Flow<List<CheckInItem>> = 
        checkInDao.getCheckInsForGoal(goalId)

    /**
     * Add a new check-in
     */
    suspend fun addCheckIn(checkIn: CheckInItem) = checkInDao.insertCheckIn(checkIn)

    /**
     * Update an existing check-in
     */
    suspend fun updateCheckIn(checkIn: CheckInItem) = checkInDao.updateCheckIn(checkIn)

    /**
     * Delete a check-in
     */
    suspend fun deleteCheckIn(checkIn: CheckInItem) = checkInDao.deleteCheckIn(checkIn)

    // ========================================
    // Monthly Review Management
    // ========================================

    /**
     * Reactive stream of all monthly reviews
     */
    val allMonthlyReviews: Flow<List<MonthlyReview>> = monthlyReviewDao.getAllMonthlyReviews()

    /**
     * Get monthly review for specific year and month
     */
    suspend fun getMonthlyReview(year: Int, month: Int): MonthlyReview? = 
        monthlyReviewDao.getMonthlyReview(year, month)

    /**
     * Check if monthly review exists for given year and month
     */
    fun hasMonthlyReview(year: Int, month: Int): Flow<Boolean> = 
        monthlyReviewDao.hasMonthlyReview(year, month)

    /**
     * Insert new monthly review
     */
    suspend fun insertMonthlyReview(review: MonthlyReview): Long = 
        monthlyReviewDao.insertMonthlyReview(review)

    /**
     * Update existing monthly review
     */
    suspend fun updateMonthlyReview(review: MonthlyReview) = 
        monthlyReviewDao.updateMonthlyReview(review)

    // ========================================
    // Final Check-In Management
    // ========================================

    /**
     * Get final check-ins for a specific review
     */
    fun getFinalCheckInsForReview(reviewId: UUID): Flow<List<FinalCheckIn>> = 
        finalCheckInDao.getFinalCheckInsForReview(reviewId)

    /**
     * Get final check-in for specific goal and review
     */
    suspend fun getFinalCheckInForGoal(goalId: UUID, reviewId: UUID): FinalCheckIn? = 
        finalCheckInDao.getFinalCheckInForGoal(goalId, reviewId)

    /**
     * Insert new final check-in
     */
    suspend fun insertFinalCheckIn(checkIn: FinalCheckIn) = 
        finalCheckInDao.insertFinalCheckIn(checkIn)

    /**
     * Update existing final check-in
     */
    suspend fun updateFinalCheckIn(checkIn: FinalCheckIn) = 
        finalCheckInDao.updateFinalCheckIn(checkIn)

    // ========================================
    // Higher Goal Management
    // ========================================

    /**
     * Reactive stream of all higher goals
     */
    val allHigherGoals: Flow<List<HigherGoal>> = higherGoalDao.getAllHigherGoals()

    /**
     * Get higher goal by ID
     */
    suspend fun getHigherGoalById(id: UUID): HigherGoal? = 
        higherGoalDao.getHigherGoalById(id)

    /**
     * Add new higher goal
     */
    suspend fun addHigherGoal(higherGoal: HigherGoal) = 
        higherGoalDao.insertHigherGoal(higherGoal)

    /**
     * Update existing higher goal
     */
    suspend fun updateHigherGoal(higherGoal: HigherGoal) = 
        higherGoalDao.updateHigherGoal(higherGoal)

    /**
     * Delete higher goal
     */
    suspend fun deleteHigherGoal(higherGoal: HigherGoal) = 
        higherGoalDao.deleteHigherGoal(higherGoal)
}
