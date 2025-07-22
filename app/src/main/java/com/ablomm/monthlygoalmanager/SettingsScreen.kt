package com.ablomm.monthlygoalmanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: GoalsViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isTipsHidden by viewModel.isTipsHidden.collectAsState(initial = false)
    val isHideCompletedGoals by viewModel.isHideCompletedGoals.collectAsState(initial = false)
    val isHideCompletedHigherGoals by viewModel.isHideCompletedHigherGoals.collectAsState(initial = false)

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }

    // ファイル保存用のランチャー
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                isExporting = true
                try {
                    val jsonData = viewModel.exportAllData()
                    val success = viewModel.exportToFile(context, uri, jsonData)
                    if (success) {
                        showExportDialog = true
                    }
                } finally {
                    isExporting = false
                }
            }
        }
    }

    // ファイル選択用のランチャー
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                isImporting = true
                try {
                    val jsonData = viewModel.importFromFile(context, uri)
                    if (jsonData != null) {
                        val result = viewModel.importData(jsonData, replaceExisting = false)
                        importResult = result
                        showImportDialog = true
                    }
                } finally {
                    isImporting = false
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // タイトル
        TopAppBar(
            title = {
                Text(
                    text = "詳細設定",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 表示設定セクション
            item {
                Text(
                    text = "表示設定",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // ヒント非表示設定
            item {
                SettingsToggleItem(
                    title = "ヒントを非表示",
                    description = "ホーム画面のヒント表示を非表示にします",
                    checked = isTipsHidden,
                    onCheckedChange = { viewModel.setTipsHidden(it) },
                    icon = Icons.Default.Lightbulb
                )
            }

            // 完了済み目標非表示設定
            item {
                SettingsToggleItem(
                    title = "完了済み目標を非表示",
                    description = "完了済みの月次目標を一覧から非表示にします",
                    checked = isHideCompletedGoals,
                    onCheckedChange = { viewModel.setHideCompletedGoals(it) },
                    icon = Icons.Default.CheckCircle
                )
            }

            // 完了済み上位目標非表示設定
            item {
                SettingsToggleItem(
                    title = "完了済み上位目標を非表示",
                    description = "達成済みの上位目標を一覧から非表示にします",
                    checked = isHideCompletedHigherGoals,
                    onCheckedChange = { viewModel.setHideCompletedHigherGoals(it) },
                    icon = Icons.Default.EmojiEvents
                )
            }

            // データの管理
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "データの管理",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // データエクスポート
                        OutlinedButton(
                            onClick = {
                                val fileName = viewModel.generateExportFileName()
                                exportLauncher.launch(fileName)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isExporting
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Default.FileDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (isExporting) "エクスポート中..." else "データをエクスポート")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // データインポート
                        OutlinedButton(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isImporting
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Default.FileUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (isImporting) "インポート中..." else "データをインポート")
                        }
                    }
                }
            }
            
            // アプリ情報
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "アプリ情報",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Litmo - 月次目標管理アプリ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "バージョン 1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // エクスポート完了ダイアログ
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("エクスポート完了") },
            text = { Text("データのエクスポートが完了しました。") },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // インポート結果ダイアログ
    if (showImportDialog && importResult != null) {
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                importResult = null
            },
            title = {
                Text(if (importResult!!.success) "インポート完了" else "インポートエラー")
            },
            text = {
                Column {
                    Text(importResult!!.message)
                    if (importResult!!.success) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = buildString {
                                append("インポートしたデータ:\n")
                                append("• 目標: ${importResult!!.importedGoals}件\n")
                                append("• 上位目標: ${importResult!!.importedHigherGoals}件\n")
                                append("• アクションステップ: ${importResult!!.importedActionSteps}件\n")
                                append("• チェックイン: ${importResult!!.importedCheckIns}件\n")
                                append("• 月次レビュー: ${importResult!!.importedMonthlyReviews}件\n")
                                append("• 最終チェックイン: ${importResult!!.importedFinalCheckIns}件")
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    importResult = null
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
