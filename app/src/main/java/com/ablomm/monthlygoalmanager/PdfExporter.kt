package com.ablomm.monthlygoalmanager

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
//import com.itextpdf.layout.property.TextAlignment
//import com.itextpdf.layout.property.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class PdfExporter(private val context: Context) {
    
    fun exportGoalsToPdf(
        goals: List<GoalItem>,
        higherGoals: List<HigherGoal>,
        yearMonth: String
    ): Intent? {
        return try {
            val fileName = "MonthlyGoals_${yearMonth.replace(" ", "_")}_${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            }.pdf"
            
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            val pdfWriter = PdfWriter(FileOutputStream(file))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)
            
            // タイトル
            document.add(
                Paragraph("Monthly Goals Report")
                    .setFontSize(20f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10f)
            )
            
            document.add(
                Paragraph(yearMonth)
                    .setFontSize(16f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20f)
            )
            
            // サマリー情報
            val completedGoals = goals.count { it.isCompleted }
            val averageProgress = if (goals.isNotEmpty()) {
                goals.map { it.currentProgress }.average()
            } else 0.0
            
            document.add(
                Paragraph("Summary")
                    .setFontSize(14f)
                    .setBold()
                    .setMarginBottom(10f)
            )
            
            document.add(
                Paragraph("Total Goals: ${goals.size}")
                    .setMarginBottom(5f)
            )
            
            document.add(
                Paragraph("Completed Goals: $completedGoals")
                    .setMarginBottom(5f)
            )
            
            document.add(
                Paragraph("Average Progress: ${String.format("%.1f", averageProgress)}%")
                    .setMarginBottom(20f)
            )
            
            // 上位目標別に分類
            val goalsByHigherGoal = goals.groupBy { goal ->
                higherGoals.find { it.id == goal.higherGoalId }
            }
            
            goalsByHigherGoal.forEach { (higherGoal, goalList) ->
                // 上位目標のタイトル
                val sectionTitle = higherGoal?.title ?: "No Higher Goal"
                document.add(
                    Paragraph(sectionTitle)
                        .setFontSize(14f)
                        .setBold()
                        .setMarginBottom(10f)
                        .setMarginTop(15f)
                )
                
                // 目標テーブル
                val table = Table(UnitValue.createPercentArray(floatArrayOf(40f, 20f, 15f, 25f)))
                    .setWidth(UnitValue.createPercentValue(100f))
                
                // ヘッダー
                table.addHeaderCell(Paragraph("Goal").setBold())
                table.addHeaderCell(Paragraph("Target").setBold())
                table.addHeaderCell(Paragraph("Progress").setBold())
                table.addHeaderCell(Paragraph("Status").setBold())
                
                // データ行
                goalList.forEach { goal ->
                    table.addCell(Paragraph(goal.title))
                    table.addCell(Paragraph("${goal.targetNumericValue}"))
                    table.addCell(Paragraph("${goal.currentProgress}%"))
                    table.addCell(
                        Paragraph(
                            when {
                                goal.isCompleted -> "✓ Completed"
                                goal.currentProgress >= 75 -> "In Progress"
                                goal.currentProgress > 0 -> "Started"
                                else -> "Not Started"
                            }
                        )
                    )
                }
                
                document.add(table.setMarginBottom(15f))
            }
            
            // フッター
            document.add(
                Paragraph("Generated on ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")
                    .setFontSize(8f)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(30f)
            )
            
            document.close()
            
            // ファイル共有のためのIntent
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_SUBJECT, "Monthly Goals Report - $yearMonth")
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
