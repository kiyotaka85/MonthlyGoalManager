package com.ablomm.monthlygoalmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Entity(tableName = "goals")
data class GoalItem(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val detailedDescription: String? = null,
    val targetMonth: Int = 2025005,
    val targetValue: String = "0",
    val currentProgress: Int = 0,
    val priority: GoalPriority = GoalPriority.Middle,
    val isCompleted: Boolean = false,
    val displayOrder: Int = 0
    //val associatedMissionItem: MissionItem? = null
)

    //ToDo var checkInLogs: List<CheckInItem>
    //ToDo var actionPlan: List<ActionStepItem>

@Entity(tableName = "check_ins")
data class CheckInItem(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val goalId: UUID,
    val progressPercent: Int,
    val comment: String,
    val checkInDate: Long = System.currentTimeMillis() // Timestamp
)

class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return uuid?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun fromGoalPriority(priority: GoalPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toGoalPriority(priority: String): GoalPriority {
        return GoalPriority.valueOf(priority)
    }
}

data class MissionItem(
    val title: String
)


enum class GoalPriority{
    High, Middle, Low
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: GoalsRepository,
    private val preferencesManager: PreferencesManager
): ViewModel() {
    val goalList: Flow<List<GoalItem>> = repository.allGoals
    
    // Preferences関連
    val isTipsHidden: Flow<Boolean> = preferencesManager.isTipsHidden
    val isHideCompletedGoals: Flow<Boolean> = preferencesManager.isHideCompletedGoals
    
    fun setTipsHidden(hidden: Boolean) {
        viewModelScope.launch {
            preferencesManager.setTipsHidden(hidden)
        }
    }
    
    fun setHideCompletedGoals(hide: Boolean) {
        viewModelScope.launch {
            preferencesManager.setHideCompletedGoals(hide)
        }
    }
    
    // 並べ替え機能
    fun updateGoalOrder(goalId: UUID, newOrder: Int) {
        viewModelScope.launch {
            val goal = repository.getGoalById(goalId)
            goal?.let {
                repository.updateGoal(it.copy(displayOrder = newOrder))
            }
        }
    }
    
    fun reorderGoals(goals: List<GoalItem>) {
        viewModelScope.launch {
            goals.forEachIndexed { index, goal ->
                repository.updateGoal(goal.copy(displayOrder = index))
            }
        }
    }

    suspend fun getGoalById(id: UUID) : GoalItem? {
        return repository.getGoalById(id)
    }

    fun updateGoalItem(updatedGoalItem: GoalItem) {
        viewModelScope.launch {
            repository.updateGoal(updatedGoalItem)
        }
    }

    fun addGoalItem(newGoalItem: GoalItem) {
        viewModelScope.launch {
            repository.addGoal(newGoalItem)
        }
    }

    fun deleteGoalItem(goalItem: GoalItem) {
        viewModelScope.launch {
            repository.deleteGoal(goalItem) // Repositoryのdeleteを呼び出す
        }
    }

    // CheckIn関連のメソッド
    fun getCheckInsForGoal(goalId: UUID): Flow<List<CheckInItem>> {
        return repository.getCheckInsForGoal(goalId)
    }
    
    fun addCheckIn(checkIn: CheckInItem) {
        viewModelScope.launch {
            repository.addCheckIn(checkIn)
        }
    }
    
    fun updateCheckIn(checkIn: CheckInItem) {
        viewModelScope.launch {
            repository.updateCheckIn(checkIn)
        }
    }
    
    fun deleteCheckIn(checkIn: CheckInItem) {
        viewModelScope.launch {
            repository.deleteCheckIn(checkIn)
        }
    }
    
    // MonthlyReview関連のメソッド
    suspend fun getMonthlyReview(year: Int, month: Int): MonthlyReview? {
        return repository.getMonthlyReview(year, month)
    }
    
    fun hasMonthlyReview(year: Int, month: Int): Flow<Boolean> {
        return repository.hasMonthlyReview(year, month)
    }
    
    val allMonthlyReviews: Flow<List<MonthlyReview>> = repository.allMonthlyReviews
    
    fun insertMonthlyReview(review: MonthlyReview) {
        viewModelScope.launch {
            repository.insertMonthlyReview(review)
        }
    }
    
    fun updateMonthlyReview(review: MonthlyReview) {
        viewModelScope.launch {
            repository.updateMonthlyReview(review)
        }
    }
    
    // FinalCheckIn関連のメソッド
    fun getFinalCheckInsForReview(reviewId: UUID): Flow<List<FinalCheckIn>> {
        return repository.getFinalCheckInsForReview(reviewId)
    }
    
    fun insertFinalCheckIn(checkIn: FinalCheckIn) {
        viewModelScope.launch {
            repository.insertFinalCheckIn(checkIn)
        }
    }
    
    fun updateFinalCheckIn(checkIn: FinalCheckIn) {
        viewModelScope.launch {
            repository.updateFinalCheckIn(checkIn)
        }
    }
    
    suspend fun getFinalCheckInForGoal(goalId: UUID, reviewId: UUID): FinalCheckIn? {
        return repository.getFinalCheckInForGoal(goalId, reviewId)
    }
}



val juneGoals = listOf(
    GoalItem(
        title = "MATH 1201 を6月末までに完了する",
        detailedDescription = "SophiaにてCollege Algebraをスコア90%以上で突破する。",
        targetMonth = 2025006,
        targetValue = "100%", // 完了目標
        currentProgress = 100,
        priority = GoalPriority.High,
        displayOrder = 0
    ),
    GoalItem(
        title = "CS 2203 を6月末までに完了する",
        targetMonth = 2025006,
        targetValue = "100%",
        currentProgress = 5,
        priority = GoalPriority.High,
        displayOrder = 1
    ),
    GoalItem(
        title = "目標入力フォーム画面を完成させる",
        targetMonth = 2025006,
        targetValue = "UI完成+ViewModel連携",
        currentProgress = 50,
        priority = GoalPriority.Middle,
        displayOrder = 2
    ),
    GoalItem(
        title = "チェックイン画面のUIを仮実装する",
        targetMonth = 2025006,
        targetValue = "進捗入力+保存動作確認",
        currentProgress = 10,
        priority = GoalPriority.Middle,
        displayOrder = 3
    ),
    GoalItem(
        title = "Plan 2028 のチェックリスト2項目を深掘りする",
        targetMonth = 2025006,
        targetValue = "第4・5項目の自己分析完了",
        currentProgress = 80,
        priority = GoalPriority.Middle,
        displayOrder = 4
    ),
    GoalItem(
        title = "週1回の自己内省ジャーナルを書く",
        targetMonth = 2025006,
        targetValue = "4回分",
        currentProgress = 0,
        priority = GoalPriority.Low,
        displayOrder = 5
    )
)

val julyGoals = listOf(
    GoalItem(
        title = "月次目標管理アプリのMVPを完成させる",
        detailedDescription = "Jetpack Compose Unit3〜4を進めながら、目標入力・チェックイン・レビューのUIとロジックを最低限実装する。",
        targetMonth = 2025007,
        targetValue = "基本画面と保存処理の実装",
        currentProgress = 20,
        priority = GoalPriority.High,
        displayOrder = 0
    ),
    GoalItem(
        title = "Sophia Database教材のPDFをすべて事前学習する",
        detailedDescription = "サブスク再開前にPDFを1周読み、章末まとめをNotion等に整理する。余力があればEthics教材にも着手。",
        targetMonth = 2025007,
        targetValue = "Database教材1周＋要点まとめ",
        currentProgress = 10,
        priority = GoalPriority.High,
        displayOrder = 1
    ),
    GoalItem(
        title = "人生設計とキャリア・学位計画をブラッシュアップする",
        detailedDescription = "NotionやPDFにて「Plan2028」含むライフ・キャリアビジョンを言語化・更新し、現実解像度を高める。",
        targetMonth = 2025007,
        targetValue = "文書化された設計書の完成",
        currentProgress = 30,
        priority = GoalPriority.High,
        displayOrder = 2
    ),
    GoalItem(
        title = "Jetpack Composeチュートリアル Unit3〜4 を完了する",
        targetMonth = 2025007,
        targetValue = "Unit3とUnit4を完了",
        currentProgress = 20,
        priority = GoalPriority.Middle,
        displayOrder = 3
    ),
    GoalItem(
        title = "運動習慣を週1回以上継続する",
        detailedDescription = "ジムまたは外ランニングを週1回以上行う。Grabでジム移動も1回実施する。",
        targetMonth = 2025007,
        targetValue = "月4回の運動実施",
        currentProgress = 0,
        priority = GoalPriority.Middle,
        displayOrder = 4
    ),
    GoalItem(
        title = "ベトナム語を毎日1フレーズ＋週末に実践する",
        detailedDescription = "日々の暮らしで1日1表現を記録し、週末にGrabや買い物などで実践チャレンジする。",
        targetMonth = 2025007,
        targetValue = "毎日継続＋週末実践4回",
        currentProgress = 0,
        priority = GoalPriority.Low,
        displayOrder = 5
    ),
    GoalItem(
        title = "育児を家族と分担し信頼を築く",
        detailedDescription = "朝のミルクと午後の沐浴は可能な限り担当。柔軟に妻をサポートし、家庭の安心感を保つ。",
        targetMonth = 2025007,
        targetValue = "週5日以上ミルク＋沐浴参加",
        currentProgress = 0,
        priority = GoalPriority.Middle,
        displayOrder = 6
    ),
    GoalItem(
        title = "Grabで1人でTime Cityのジムに行く",
        detailedDescription = "道順・語彙・流れを確認し、ベトナム滞在中に自立移動体験を達成する。",
        targetMonth = 2025007,
        targetValue = "1回成功体験を積む",
        currentProgress = 0,
        priority = GoalPriority.Low,
        displayOrder = 7
    )
)

@Entity(tableName = "monthly_reviews")
data class MonthlyReview(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val year: Int,
    val month: Int,
    val overallReflection: String,
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "final_checkins")
data class FinalCheckIn(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val goalId: UUID,
    val monthlyReviewId: UUID,
    val finalProgress: Int,
    val achievements: String, // 達成したこと
    val challenges: String,   // 困難だったこと
    val learnings: String     // 学んだこと
)