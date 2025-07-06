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