package com.ablomm.monthlygoalmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.time.YearMonth
import android.os.Build
import androidx.annotation.RequiresApi

@Entity(tableName = "higher_goals")
data class HigherGoal(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String? = null,
    val color: String = "#2196F3", // デフォルト色
    val createdAt: Long = System.currentTimeMillis()
)

// Action Step for goals
@Entity(tableName = "action_steps")
data class ActionStep(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val goalId: UUID,
    val title: String,
    val isCompleted: Boolean = false,
    val order: Int = 0
)

@Entity(tableName = "goals")
data class GoalItem(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val detailedDescription: String? = null,
    val targetMonth: Int = 2025005,
    val targetNumericValue: Double = 0.0, // 数値目標の目標値（必須）
    val startNumericValue: Double = 0.0, // 数値目標の開始値（デフォルトは0）
    val currentNumericValue: Double = 0.0, // 数値目標の現在値（必須）
    val unit: String = "", // 数値目標の単位（必須）
    val currentProgress: Int = 0,
    val priority: GoalPriority = GoalPriority.Middle,
    val isCompleted: Boolean = false,
    val displayOrder: Int = 0,
    val higherGoalId: UUID? = null, // 上位目標への参照
    val celebration: String? = null, // ご褒美
    val isDecimal: Boolean = false // 小数点表示フラグを追加
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
}

data class MissionItem(
    val title: String
)


enum class GoalPriority{
    High, Middle, Low
}

// 進捗率計算のヘルパー関数
fun calculateProgress(
    startValue: Double,
    targetValue: Double,
    currentValue: Double
): Int {
    val range = targetValue - startValue
    val progressInRange = currentValue - startValue

    return if (range != 0.0) {
        (progressInRange / range * 100).coerceAtLeast(0.0).toInt() // 下限は0%だが上限は設けない（オーバーアチーブ許可）
    } else {
        if (currentValue >= targetValue) 100 else 0
    }
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: GoalsRepository,
    private val preferencesManager: PreferencesManager,
    private val dataExportImportManager: DataExportImportManager // 依存関係を追加
): ViewModel() {
    val goalList: Flow<List<GoalItem>> = repository.allGoals
    
    // Preferences関連
    val isTipsHidden: Flow<Boolean> = preferencesManager.isTipsHidden
    val isHideCompletedGoals: Flow<Boolean> = preferencesManager.isHideCompletedGoals
    
    // 現在表示中の年月を管理
    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun setCurrentYearMonth(yearMonth: YearMonth) {
        _currentYearMonth.value = yearMonth
    }
    
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
    
    // HigherGoal関連
    val higherGoalList: Flow<List<HigherGoal>> = repository.allHigherGoals
    
    fun addHigherGoal(higherGoal: HigherGoal) {
        viewModelScope.launch {
            repository.addHigherGoal(higherGoal)
        }
    }
    
    fun updateHigherGoal(higherGoal: HigherGoal) {
        viewModelScope.launch {
            repository.updateHigherGoal(higherGoal)
        }
    }
    
    fun deleteHigherGoal(higherGoal: HigherGoal) {
        viewModelScope.launch {
            repository.deleteHigherGoal(higherGoal)
        }
    }
    
    suspend fun getHigherGoalById(id: UUID): HigherGoal? {
        return repository.getHigherGoalById(id)
    }

    // ActionStep関連のメソッド
    fun getActionStepsForGoal(goalId: UUID): Flow<List<ActionStep>> {
        return repository.getActionStepsForGoal(goalId)
    }

    fun addActionStep(actionStep: ActionStep) {
        viewModelScope.launch {
            repository.addActionStep(actionStep)
        }
    }

    fun updateActionStep(actionStep: ActionStep) {
        viewModelScope.launch {
            repository.updateActionStep(actionStep)
        }
    }

    fun deleteActionStep(actionStep: ActionStep) {
        viewModelScope.launch {
            repository.deleteActionStep(actionStep)
        }
    }

    // 月次レビュー削除機能を追加
    fun deleteMonthlyReview(review: MonthlyReview) {
        viewModelScope.launch {
            repository.deleteMonthlyReview(review)
        }
    }

    // 編集中GoalItemの状態管理
    private val _editingGoalItem = MutableStateFlow<GoalItem?>(null)
    val editingGoalItem: StateFlow<GoalItem?> = _editingGoalItem

    fun setEditingGoalItem(goal: GoalItem?) {
        _editingGoalItem.value = goal
    }

    fun updateEditingGoalItem(update: (GoalItem) -> GoalItem) {
        _editingGoalItem.value = _editingGoalItem.value?.let(update)
    }

    // エクスポート・インポート機能
    suspend fun exportAllData(): String {
        return dataExportImportManager.exportAllData()
    }

    suspend fun importData(jsonString: String, replaceExisting: Boolean = false): ImportResult {
        return dataExportImportManager.importData(jsonString, replaceExisting)
    }

    suspend fun exportToFile(context: android.content.Context, uri: android.net.Uri, jsonData: String): Boolean {
        return dataExportImportManager.exportToFile(context, uri, jsonData)
    }

    suspend fun importFromFile(context: android.content.Context, uri: android.net.Uri): String? {
        return dataExportImportManager.importFromFile(context, uri)
    }

    fun generateExportFileName(): String {
        return dataExportImportManager.generateExportFileName()
    }

    suspend fun validateImportFile(jsonString: String): Boolean {
        return dataExportImportManager.validateImportFile(jsonString)
    }
}



val juneGoals = listOf(
    GoalItem(
        title = "MATH 1201 を6月末までに完了する",
        detailedDescription = "SophiaにてCollege Algebraをスコア90%以上で突破する。",
        targetMonth = 2025006,
        targetNumericValue = 100.0, // 完了目標
        currentProgress = 100,
        priority = GoalPriority.High,
        displayOrder = 0
    ),
    GoalItem(
        title = "CS 2203 を6月末までに完了する",
        targetMonth = 2025006,
        targetNumericValue = 100.0,
        currentProgress = 5,
        priority = GoalPriority.High,
        displayOrder = 1
    ),
    GoalItem(
        title = "目標入力フォーム画面を完成させる",
        targetMonth = 2025006,
        targetNumericValue = 100.0,
        currentProgress = 50,
        priority = GoalPriority.Middle,
        displayOrder = 2
    ),
    GoalItem(
        title = "チェックイン画面のUIを仮実装する",
        targetMonth = 2025006,
        targetNumericValue = 100.0,
        currentProgress = 10,
        priority = GoalPriority.Middle,
        displayOrder = 3
    ),
    GoalItem(
        title = "Plan 2028 のチェックリスト2項目を深掘りする",
        targetMonth = 2025006,
        targetNumericValue = 100.0,
        currentProgress = 80,
        priority = GoalPriority.Middle,
        displayOrder = 4
    ),
    GoalItem(
        title = "週1回の自己内省ジャーナルを書く",
        targetMonth = 2025006,
        targetNumericValue = 4.0,
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
        targetNumericValue = 100.0,
        currentProgress = 20,
        priority = GoalPriority.High,
        displayOrder = 0
    ),
    GoalItem(
        title = "Sophia Database教材のPDFをすべて事前学習する",
        detailedDescription = "サブスク再開前にPDFを1周読み、章末まとめをNotion等に整理する。余力があればEthics教材にも着手。",
        targetMonth = 2025007,
        targetNumericValue = 100.0,
        currentProgress = 10,
        priority = GoalPriority.High,
        displayOrder = 1
    ),
    GoalItem(
        title = "人生設計とキャリア・学位計画をブラッシュアップする",
        detailedDescription = "NotionやPDFにて「Plan2028」含むライフ・キャリアビジョンを言語化・更新し、現実解像度を高める。",
        targetMonth = 2025007,
        targetNumericValue = 100.0,
        currentProgress = 30,
        priority = GoalPriority.High,
        displayOrder = 2
    ),
    GoalItem(
        title = "Jetpack Composeチュートリアル Unit3〜4 を完了する",
        targetMonth = 2025007,
        targetNumericValue = 100.0,
        currentProgress = 20,
        priority = GoalPriority.Middle,
        displayOrder = 3
    ),
    GoalItem(
        title = "運動習慣を週1回以上継続する",
        detailedDescription = "ジムまたは外ランニングを週1回以上行う。Grabでジム移動も1回実施する。",
        targetMonth = 2025007,
        targetNumericValue = 4.0,
        currentProgress = 0,
        priority = GoalPriority.Middle,
        displayOrder = 4
    ),
    GoalItem(
        title = "ベトナム語を毎日1フレーズ＋週末に実践する",
        detailedDescription = "日々の暮らしで1日1表現を記録し、週末にGrabや買い物などで実践チャレンジする。",
        targetMonth = 2025007,
        targetNumericValue = 7.0,
        currentProgress = 0,
        priority = GoalPriority.Low,
        displayOrder = 5
    ),
    GoalItem(
        title = "育児を家族と分担し信頼を築く",
        detailedDescription = "朝のミルクと午後の沐浴は可能な限り担当。柔軟に妻をサポートし、家庭の安心感を保つ。",
        targetMonth = 2025007,
        targetNumericValue = 5.0,
        currentProgress = 0,
        priority = GoalPriority.Middle,
        displayOrder = 6
    ),
    GoalItem(
        title = "Grabで1人でTime Cityのジムに行く",
        detailedDescription = "道順・語彙・流れを確認し、ベトナム滞在中に自立移動体験を達成する。",
        targetMonth = 2025007,
        targetNumericValue = 1.0,
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
    val learnings: String,    // 学んだこと
    val satisfactionRating: Int = 3 // 満足度評価（1-5の星評価）
)
