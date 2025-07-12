package com.ablomm.monthlygoalmanager.utils

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
import com.ablomm.monthlygoalmanager.*
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * PDF exporter utility for goals and monthly reports
 */
class PdfExporter(private val context: Context) {
    
    /**
     * Export goals to PDF and return sharing intent
     */
    fun exportGoalsToPdf(
        goals: List<GoalItem>,
        higherGoals: List<HigherGoal>,
        yearMonth: String
    ): Intent? {
        return try {
            val fileName = "MonthlyGoals_${yearMonth.replace(" ", "_")}_${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            }.pdf"
            
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            
            createPdf(file, goals, higherGoals, yearMonth)
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Monthly Goals Report - $yearMonth")
                putExtra(Intent.EXTRA_TEXT, "Here's my monthly goals report for $yearMonth")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            Intent.createChooser(shareIntent, "Share Goals Report")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Create PDF document with goals data
     */
    private fun createPdf(
        file: File,
        goals: List<GoalItem>,
        higherGoals: List<HigherGoal>,
        yearMonth: String
    ) {
        val writer = PdfWriter(FileOutputStream(file))
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)
        
        // Title
        document.add(
            Paragraph("Monthly Goals Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
        )
        
        document.add(
            Paragraph(yearMonth)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16f)
                .setMarginBottom(20f)
        )
        
        // Summary statistics
        val completedCount = goals.count { it.isCompleted }
        val totalCount = goals.size
        val averageProgress = if (goals.isNotEmpty()) {
            goals.map { it.currentProgress }.average()
        } else 0.0
        
        document.add(
            Paragraph("Summary")
                .setFontSize(16f)
                .setBold()
                .setMarginBottom(10f)
        )
        
        document.add(
            Paragraph("Total Goals: $totalCount")
                .setFontSize(12f)
        )
        
        document.add(
            Paragraph("Completed: $completedCount")
                .setFontSize(12f)
        )
        
        document.add(
            Paragraph("Average Progress: ${averageProgress.toInt()}%")
                .setFontSize(12f)
                .setMarginBottom(20f)
        )
        
        // Goals table
        if (goals.isNotEmpty()) {
            document.add(
                Paragraph("Goals Details")
                    .setFontSize(16f)
                    .setBold()
                    .setMarginBottom(10f)
            )
            
            val table = Table(UnitValue.createPercentArray(floatArrayOf(30f, 40f, 15f, 15f)))
                .setWidth(UnitValue.createPercentValue(100f))
            
            // Header
            table.addCell(Paragraph("Title").setBold())
            table.addCell(Paragraph("Description").setBold())
            table.addCell(Paragraph("Progress").setBold())
            table.addCell(Paragraph("Priority").setBold())
            
            // Goals data
            goals.forEach { goal ->
                table.addCell(Paragraph(goal.title))
                table.addCell(Paragraph(goal.description))
                table.addCell(Paragraph("${goal.currentProgress}%"))
                table.addCell(Paragraph(goal.priority.name))
            }
            
            document.add(table)
        }
        
        // Higher goals section
        if (higherGoals.isNotEmpty()) {
            document.add(
                Paragraph("Higher Goals")
                    .setFontSize(16f)
                    .setBold()
                    .setMarginTop(20f)
                    .setMarginBottom(10f)
            )
            
            val higherGoalTable = Table(UnitValue.createPercentArray(floatArrayOf(30f, 50f, 20f)))
                .setWidth(UnitValue.createPercentValue(100f))
            
            // Header
            higherGoalTable.addCell(Paragraph("Title").setBold())
            higherGoalTable.addCell(Paragraph("Description").setBold())
            higherGoalTable.addCell(Paragraph("Category").setBold())
            
            // Higher goals data
            higherGoals.forEach { goal ->
                higherGoalTable.addCell(Paragraph(goal.title))
                higherGoalTable.addCell(Paragraph(goal.description))
                higherGoalTable.addCell(Paragraph(goal.category))
            }
            
            document.add(higherGoalTable)
        }
        
        // Footer
        document.add(
            Paragraph("Generated on ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(8f)
                .setMarginTop(30f)
        )
        
        document.close()
    }
    
    /**
     * Export monthly review to PDF
     */
    fun exportMonthlyReviewToPdf(
        goals: List<GoalItem>,
        review: MonthlyReview,
        yearMonth: String
    ): Intent? {
        return try {
            val fileName = "MonthlyReview_${yearMonth.replace(" ", "_")}_${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            }.pdf"
            
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            
            createReviewPdf(file, goals, review, yearMonth)
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Monthly Review - $yearMonth")
                putExtra(Intent.EXTRA_TEXT, "Here's my monthly review for $yearMonth")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            Intent.createChooser(shareIntent, "Share Monthly Review")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Create PDF document with monthly review data
     */
    private fun createReviewPdf(
        file: File,
        goals: List<GoalItem>,
        review: MonthlyReview,
        yearMonth: String
    ) {
        val writer = PdfWriter(FileOutputStream(file))
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)
        
        // Title
        document.add(
            Paragraph("Monthly Review")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
        )
        
        document.add(
            Paragraph(yearMonth)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16f)
                .setMarginBottom(20f)
        )
        
        // Overall reflection
        document.add(
            Paragraph("Overall Reflection")
                .setFontSize(16f)
                .setBold()
                .setMarginBottom(10f)
        )
        
        document.add(
            Paragraph(review.overallReflection)
                .setFontSize(12f)
                .setMarginBottom(20f)
        )
        
        // Goals summary
        val completedCount = goals.count { it.isCompleted }
        val totalCount = goals.size
        
        document.add(
            Paragraph("Goals Summary")
                .setFontSize(16f)
                .setBold()
                .setMarginBottom(10f)
        )
        
        document.add(
            Paragraph("Completed: $completedCount out of $totalCount goals")
                .setFontSize(12f)
                .setMarginBottom(20f)
        )
        
        document.close()
    }
}
