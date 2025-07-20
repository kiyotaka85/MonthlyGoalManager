package com.ablomm.monthlygoalmanager

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

// JSON用のシリアライズ可能なデータクラス
@Serializable
data class ExportData(
    val version: String = "1.0",
    val exportDate: String,
    val goals: List<SerializableGoalItem>,
    val higherGoals: List<SerializableHigherGoal>,
    val actionSteps: List<SerializableActionStep>,
    val checkIns: List<SerializableCheckInItem>,
    val monthlyReviews: List<SerializableMonthlyReview>,
    val finalCheckIns: List<SerializableFinalCheckIn>
)

@Serializable
data class SerializableGoalItem(
    val id: String,
    val title: String,
    val detailedDescription: String? = null,
    val targetMonth: Int,
    val targetNumericValue: Double,
    val startNumericValue: Double,
    val currentNumericValue: Double,
    val unit: String,
    val currentProgress: Int,
    val priority: String, // GoalPriorityを文字列として保存
    val isCompleted: Boolean,
    val displayOrder: Int,
    val higherGoalId: String? = null,
    val celebration: String? = null,
    val isDecimal: Boolean
)

@Serializable
data class SerializableHigherGoal(
    val id: String,
    val title: String,
    val description: String? = null,
    val color: String,
    val createdAt: Long
)

@Serializable
data class SerializableActionStep(
    val id: String,
    val goalId: String,
    val title: String,
    val isCompleted: Boolean,
    val order: Int
)

@Serializable
data class SerializableCheckInItem(
    val id: String,
    val goalId: String,
    val progressPercent: Int,
    val comment: String,
    val checkInDate: Long
)

@Serializable
data class SerializableMonthlyReview(
    val id: String,
    val year: Int,
    val month: Int,
    val overallReflection: String,
    val createdDate: Long
)

@Serializable
data class SerializableFinalCheckIn(
    val id: String,
    val goalId: String,
    val monthlyReviewId: String,
    val finalProgress: Int,
    val achievements: String,
    val challenges: String,
    val learnings: String,
    val satisfactionRating: Int
)

// データ変換用の拡張関数
fun GoalItem.toSerializable() = SerializableGoalItem(
    id = id.toString(),
    title = title,
    detailedDescription = detailedDescription,
    targetMonth = targetMonth,
    targetNumericValue = targetNumericValue,
    startNumericValue = startNumericValue,
    currentNumericValue = currentNumericValue,
    unit = unit,
    currentProgress = currentProgress,
    priority = priority.name,
    isCompleted = isCompleted,
    displayOrder = displayOrder,
    higherGoalId = higherGoalId?.toString(),
    celebration = celebration,
    isDecimal = isDecimal
)

fun SerializableGoalItem.toGoalItem() = GoalItem(
    id = UUID.fromString(id),
    title = title,
    detailedDescription = detailedDescription,
    targetMonth = targetMonth,
    targetNumericValue = targetNumericValue,
    startNumericValue = startNumericValue,
    currentNumericValue = currentNumericValue,
    unit = unit,
    currentProgress = currentProgress,
    priority = GoalPriority.valueOf(priority),
    isCompleted = isCompleted,
    displayOrder = displayOrder,
    higherGoalId = higherGoalId?.let { UUID.fromString(it) },
    celebration = celebration,
    isDecimal = isDecimal
)

fun HigherGoal.toSerializable() = SerializableHigherGoal(
    id = id.toString(),
    title = title,
    description = description,
    color = color,
    createdAt = createdAt
)

fun SerializableHigherGoal.toHigherGoal() = HigherGoal(
    id = UUID.fromString(id),
    title = title,
    description = description,
    color = color,
    createdAt = createdAt
)

fun ActionStep.toSerializable() = SerializableActionStep(
    id = id.toString(),
    goalId = goalId.toString(),
    title = title,
    isCompleted = isCompleted,
    order = order
)

fun SerializableActionStep.toActionStep() = ActionStep(
    id = UUID.fromString(id),
    goalId = UUID.fromString(goalId),
    title = title,
    isCompleted = isCompleted,
    order = order
)

fun CheckInItem.toSerializable() = SerializableCheckInItem(
    id = id.toString(),
    goalId = goalId.toString(),
    progressPercent = progressPercent,
    comment = comment,
    checkInDate = checkInDate
)

fun SerializableCheckInItem.toCheckInItem() = CheckInItem(
    id = UUID.fromString(id),
    goalId = UUID.fromString(goalId),
    progressPercent = progressPercent,
    comment = comment,
    checkInDate = checkInDate
)

fun MonthlyReview.toSerializable() = SerializableMonthlyReview(
    id = id.toString(),
    year = year,
    month = month,
    overallReflection = overallReflection,
    createdDate = createdDate
)

fun SerializableMonthlyReview.toMonthlyReview() = MonthlyReview(
    id = UUID.fromString(id),
    year = year,
    month = month,
    overallReflection = overallReflection,
    createdDate = createdDate
)

fun FinalCheckIn.toSerializable() = SerializableFinalCheckIn(
    id = id.toString(),
    goalId = goalId.toString(),
    monthlyReviewId = monthlyReviewId.toString(),
    finalProgress = finalProgress,
    achievements = achievements,
    challenges = challenges,
    learnings = learnings,
    satisfactionRating = satisfactionRating
)

fun SerializableFinalCheckIn.toFinalCheckIn() = FinalCheckIn(
    id = UUID.fromString(id),
    goalId = UUID.fromString(goalId),
    monthlyReviewId = UUID.fromString(monthlyReviewId),
    finalProgress = finalProgress,
    achievements = achievements,
    challenges = challenges,
    learnings = learnings,
    satisfactionRating = satisfactionRating
)

@Singleton
class DataExportImportManager @Inject constructor(
    private val repository: GoalsRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportAllData(): String = withContext(Dispatchers.IO) {
        val goals = repository.getAllGoalsOnce()
        val higherGoals = repository.getAllHigherGoalsOnce()
        val actionSteps = repository.getAllActionStepsOnce()
        val checkIns = repository.getAllCheckInsOnce()
        val monthlyReviews = repository.getAllMonthlyReviewsOnce()
        val finalCheckIns = repository.getAllFinalCheckInsOnce()

        val exportData = ExportData(
            exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            goals = goals.map { it.toSerializable() },
            higherGoals = higherGoals.map { it.toSerializable() },
            actionSteps = actionSteps.map { it.toSerializable() },
            checkIns = checkIns.map { it.toSerializable() },
            monthlyReviews = monthlyReviews.map { it.toSerializable() },
            finalCheckIns = finalCheckIns.map { it.toSerializable() }
        )

        json.encodeToString(exportData)
    }

    suspend fun importData(jsonString: String, replaceExisting: Boolean = false): ImportResult = withContext(Dispatchers.IO) {
        try {
            val exportData = json.decodeFromString<ExportData>(jsonString)
            val errors = mutableListOf<String>()

            if (replaceExisting) {
                // 既存データを全て削除
                repository.deleteAllData()
            }

            var importedGoals = 0
            var importedHigherGoals = 0
            var importedActionSteps = 0
            var importedCheckIns = 0
            var importedMonthlyReviews = 0
            var importedFinalCheckIns = 0

            // 上位目標をインポート
            exportData.higherGoals.forEach { serializableHigherGoal ->
                try {
                    val higherGoal = serializableHigherGoal.toHigherGoal()
                    repository.insertHigherGoal(higherGoal)
                    importedHigherGoals++
                } catch (e: Exception) {
                    errors.add("上位目標 '${serializableHigherGoal.title}' のインポートに失敗: ${e.message}")
                }
            }

            // 目標をインポート
            exportData.goals.forEach { serializableGoal ->
                try {
                    val goal = serializableGoal.toGoalItem()
                    repository.insertGoal(goal)
                    importedGoals++
                } catch (e: Exception) {
                    errors.add("目標 '${serializableGoal.title}' のインポートに失敗: ${e.message}")
                }
            }

            // アクションステップをインポート
            exportData.actionSteps.forEach { serializableActionStep ->
                try {
                    val actionStep = serializableActionStep.toActionStep()
                    repository.insertActionStep(actionStep)
                    importedActionSteps++
                } catch (e: Exception) {
                    errors.add("アクションステップ '${serializableActionStep.title}' のインポートに失敗: ${e.message}")
                }
            }

            // チェックインをインポート
            exportData.checkIns.forEach { serializableCheckIn ->
                try {
                    val checkIn = serializableCheckIn.toCheckInItem()
                    repository.insertCheckIn(checkIn)
                    importedCheckIns++
                } catch (e: Exception) {
                    errors.add("チェックイン（ID: ${serializableCheckIn.id}）のインポートに失敗: ${e.message}")
                }
            }

            // 月次レビューをインポート
            exportData.monthlyReviews.forEach { serializableMonthlyReview ->
                try {
                    val monthlyReview = serializableMonthlyReview.toMonthlyReview()
                    repository.insertMonthlyReview(monthlyReview)
                    importedMonthlyReviews++
                } catch (e: Exception) {
                    errors.add("月次レビュー（${serializableMonthlyReview.year}年${serializableMonthlyReview.month}月）のインポートに失敗: ${e.message}")
                }
            }

            // 最終チェックインをインポート
            exportData.finalCheckIns.forEach { serializableFinalCheckIn ->
                try {
                    val finalCheckIn = serializableFinalCheckIn.toFinalCheckIn()
                    repository.insertFinalCheckIn(finalCheckIn)
                    importedFinalCheckIns++
                } catch (e: Exception) {
                    errors.add("最終チェックイン（ID: ${serializableFinalCheckIn.id}）のインポートに失敗: ${e.message}")
                }
            }

            ImportResult(
                success = true,
                message = "データのインポートが完了しました。",
                importedGoals = importedGoals,
                importedHigherGoals = importedHigherGoals,
                importedActionSteps = importedActionSteps,
                importedCheckIns = importedCheckIns,
                importedMonthlyReviews = importedMonthlyReviews,
                importedFinalCheckIns = importedFinalCheckIns,
                errors = errors
            )

        } catch (e: Exception) {
            ImportResult(
                success = false,
                message = "データのインポートに失敗しました: ${e.message}",
                errors = listOf(e.message ?: "不明なエラー")
            )
        }
    }

    // ファイルからJSONデータをエクスポート
    suspend fun exportToFile(context: Context, uri: Uri, jsonData: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonData.toByteArray())
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ファイルからJSONデータをインポート
    suspend fun importFromFile(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // エクスポートファイル名を生成
    fun generateExportFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        return "Litmo_backup_$timestamp.json"
    }

    // インポートファイルの妥当性をチェック
    suspend fun validateImportFile(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val exportData = json.decodeFromString<ExportData>(jsonString)
            // 基本的な構造をチェック
            exportData.version.isNotEmpty() && exportData.exportDate.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}

// インポート結果を表すデータクラス
@Serializable
data class ImportResult(
    val success: Boolean,
    val message: String,
    val importedGoals: Int = 0,
    val importedHigherGoals: Int = 0,
    val importedActionSteps: Int = 0,
    val importedCheckIns: Int = 0,
    val importedMonthlyReviews: Int = 0,
    val importedFinalCheckIns: Int = 0,
    val errors: List<String> = emptyList()
)
