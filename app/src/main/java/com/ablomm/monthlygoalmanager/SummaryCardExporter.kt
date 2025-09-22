package com.ablomm.monthlygoalmanager

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.YearMonth
import java.util.Locale

// Data for one summary row
data class SummaryGoalRow(
    val name: String,
    val current: Double,
    val target: Double,
    val unit: String,
    val isDecimal: Boolean = false
)

// Create a clean, minimal summary card bitmap suitable for social sharing.
fun createSummaryCardBitmap(
    context: Context,
    title: String,
    rows: List<SummaryGoalRow>,
    widthPx: Int = 1080,
): Bitmap {
    val dm = context.resources.displayMetrics
    fun dp(v: Float) = (v * dm.density)

    val paddingH = dp(24f)
    val paddingTop = dp(28f)
    val paddingBottom = dp(24f)
    val lineSpacing = dp(8f)
    val rowHeight = dp(40f)
    val dividerHeight = dp(1f)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = dp(22f)
    }
    val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textSize = dp(18f)
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textSize = dp(18f)
        textAlign = Paint.Align.RIGHT
    }
    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textSize = dp(14f)
        textAlign = Paint.Align.LEFT
    }
    val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = dividerHeight
    }

    val titleMetrics = Paint.FontMetrics().also { titlePaint.getFontMetrics(it) }
    val titleHeight = titleMetrics.bottom - titleMetrics.top
    val contentHeight = rows.size * (rowHeight + lineSpacing)
    val footerMetrics = Paint.FontMetrics().also { footerPaint.getFontMetrics(it) }
    val footerHeight = footerMetrics.bottom - footerMetrics.top

    val heightPx = (paddingTop + titleHeight + dp(16f) + contentHeight + dp(16f) + dividerHeight + dp(8f) + footerHeight + paddingBottom).toInt()

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    var y = paddingTop

    // Title
    val titleBaseline = y - titleMetrics.top
    canvas.drawText(title, paddingH, titleBaseline, titlePaint)
    y += titleHeight + dp(16f)

    val leftX = paddingH
    val rightX = widthPx - paddingH

    // Rows: name left, values right: "current / target unit"
    rows.forEach { row ->
        val baseline = y + namePaint.textSize
        canvas.drawText(row.name, leftX, baseline, namePaint)

        val currentStr = formatNumberForSummary(row.current, row.isDecimal)
        val targetStr = formatNumberForSummary(row.target, row.isDecimal)
        val valueStr = "$currentStr / $targetStr ${row.unit}".trim()
        canvas.drawText(valueStr, rightX, baseline, valuePaint)

        y += rowHeight + lineSpacing
    }

    // Divider
    val dividerY = y + dp(8f)
    canvas.drawLine(paddingH, dividerY, rightX, dividerY, dividerPaint)

    // Footer
    val footerText = "Monthly Goal Manager • Prove your progress with real numbers"
    val footerBaseline = dividerY + dp(8f) - footerMetrics.top
    canvas.drawText(footerText, paddingH, footerBaseline, footerPaint)

    return bitmap
}

private fun formatNumberForSummary(value: Double, isDecimal: Boolean): String {
    return if (!isDecimal && value % 1.0 == 0.0) value.toInt().toString()
    else String.format(Locale.getDefault(), "%.1f", value)
}

// Save the bitmap to Pictures/MonthlyGoalManager (optional utility kept for future needs)
fun saveSummaryBitmapToPictures(
    context: Context,
    bitmap: Bitmap,
    filename: String = "monthly_goal_summary_${System.currentTimeMillis()}.png"
): String? {
    return try {
        val mimeType = "image/png"
        var uriString: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MonthlyGoalManager")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                uriString = uri.toString()
            }
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val folder = File(dir, "MonthlyGoalManager").apply { mkdirs() }
            val file = File(folder, filename)
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            uriString = file.absolutePath
        }
        uriString
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Share via Android share sheet using FileProvider
fun shareSummaryBitmap(
    context: Context,
    bitmap: Bitmap,
    filename: String = "monthly_goal_summary_${System.currentTimeMillis()}.png"
) {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val folder = File(dir, "MonthlyGoalManagerShare").apply { mkdirs() }
    val file = File(folder, filename)
    FileOutputStream(file).use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    }
    val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share summary image"))
}

// Build a title like "YYYY/MM Summary"
fun buildSummaryTitle(ym: YearMonth): String {
    return String.format(Locale.getDefault(), "%04d/%02d Summary", ym.year, ym.monthValue)
}
