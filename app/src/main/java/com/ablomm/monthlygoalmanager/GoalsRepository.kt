package com.ablomm.monthlygoalmanager

// GoalsRepository.kt (新しいファイルとして作成)
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GoalsRepository(private val goalDao: GoalDao) {
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
}