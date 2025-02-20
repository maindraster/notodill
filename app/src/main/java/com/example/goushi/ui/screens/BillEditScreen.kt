package com.example.goushi.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goushi.ui.viewmodel.BillViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillEditScreen(
    viewModel: BillViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    val context = LocalContext.current

    val isValidAmount = amount.toDoubleOrNull() != null && amount.toDoubleOrNull() ?: 0.0 > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记一笔") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "标题不能为空", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            if (!isValidAmount) {
                                Toast.makeText(context, "请输入有效金额", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            try {
                                viewModel.addBill(
                                    title = title.trim(),
                                    content = content.trim(),
                                    amount = amount.toDoubleOrNull() ?: 0.0,
                                    isExpense = isExpense,
                                    tags = selectedTags.toList()
                                )
                                Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            } catch (e: Exception) {
                                Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = title.isNotBlank() && isValidAmount
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "完成")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 标签选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(selectedTags.toList()) { tag ->
                        AssistChip(
                            onClick = { selectedTags -= tag },
                            label = { Text(text = tag) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
                
                IconButton(onClick = { showTagDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "添加标签")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("备注") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 收支类型选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = isExpense,
                    onClick = { isExpense = true },
                    label = { Text("支出") }
                )
                FilterChip(
                    selected = !isExpense,
                    onClick = { isExpense = false },
                    label = { Text("收入") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 金额输入
            OutlinedTextField(
                value = amount,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amount = it
                    }
                },
                label = { Text("金额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("¥") },
                modifier = Modifier.fillMaxWidth(),
                isError = amount.isNotEmpty() && !isValidAmount
            )
        }
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("添加标签") },
            text = {
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    label = { Text("标签名称") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTag.isNotEmpty()) {
                            selectedTags += newTag
                            newTag = ""
                        }
                        showTagDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
} 