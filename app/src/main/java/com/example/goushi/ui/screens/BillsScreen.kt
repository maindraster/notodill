package com.example.goushi.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goushi.data.model.Bill
import com.example.goushi.ui.viewmodel.BillViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillsScreen(
    viewModel: BillViewModel = hiltViewModel(),
    onNavigateToEdit: () -> Unit
) {
    val bills by viewModel.monthlyBills.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(error) {
        error?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(bills) {
        Log.d("BillsScreen", "Bills updated: ${bills.size}")
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToEdit,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加账单")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 统计卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 本周统计
                    Text(
                        text = "本周收支",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "收入: ¥${String.format("%.2f", weeklyStats.income)}",
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "支出: ¥${String.format("%.2f", weeklyStats.expense)}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = "净收入: ¥${String.format("%.2f", weeklyStats.netIncome)}",
                            fontWeight = FontWeight.Bold,
                            color = if (weeklyStats.netIncome >= 0) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // 当前选择月份统计
                    Text(
                        text = "${currentMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))}收支",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "收入: ¥${String.format("%.2f", monthlyStats.income)}",
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "支出: ¥${String.format("%.2f", monthlyStats.expense)}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = "净收入: ¥${String.format("%.2f", monthlyStats.netIncome)}",
                            fontWeight = FontWeight.Bold,
                            color = if (monthlyStats.netIncome >= 0) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 标签筛选
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bills.flatMap { it.tags }.distinct()) { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick = { viewModel.toggleTag(tag) },
                        label = { Text(tag) }
                    )
                }
            }

            // 月份选择
            MonthSelector(
                currentMonth = currentMonth,
                onMonthSelected = viewModel::setCurrentMonth,
                modifier = Modifier.fillMaxWidth()
            )

            // 账单列表
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bills) { bill ->
                    BillCard(
                        bill = bill,
                        isEditMode = isEditMode,
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteBill(bill)
                        },
                        onLongClick = viewModel::toggleEditMode,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BillCard(
    bill: Bill,
    isEditMode: Boolean,
    onDelete: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isEditMode) 0.95f else 1f)

    Card(
        modifier = modifier
            .scale(scale)
            .combinedClickable(
                onClick = { /* 点击查看详情 */ },
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (bill.content.isNotEmpty()) {
                    Text(
                        text = bill.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = bill.createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (bill.tags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(bill.tags) { tag ->
                            AssistChip(
                                onClick = { },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (bill.isExpense) "-¥${String.format("%.2f", bill.amount)}"
                           else "+¥${String.format("%.2f", bill.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (bill.isExpense) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                if (isEditMode) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
} 