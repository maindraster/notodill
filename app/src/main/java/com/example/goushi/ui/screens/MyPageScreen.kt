package com.example.goushi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goushi.ui.components.ContributionHeatMap
import com.example.goushi.ui.viewmodel.MyPageViewModel

@Composable
fun MyPageScreen(viewModel: MyPageViewModel = hiltViewModel()) {
    val todayNotesCount by viewModel.todayNotesCount.collectAsState()
    val todayTodosCount by viewModel.todayTodosCount.collectAsState()
    val todayBillsCount by viewModel.todayBillsCount.collectAsState()
    val noteContributions by viewModel.noteContributions.collectAsState()
    val todoContributions by viewModel.todoContributions.collectAsState()
    val billContributions by viewModel.billContributions.collectAsState()

    // 确保热力图数据不为空
    if (noteContributions.isEmpty() || todoContributions.isEmpty() || billContributions.isEmpty()) {
        // 显示加载提示
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "正在加载数据，请稍候...")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 统计卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "今日统计",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "新增随笔: $todayNotesCount")
                    Text(text = "完成待办: $todayTodosCount")
                    Text(text = "新增账单: $todayBillsCount")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 随笔贡献热力图
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            ContributionHeatMap(
                title = "随笔贡献",
                contributions = noteContributions,
                modifier = Modifier.padding(16.dp)
            )
        }

        // 待办完成热力图
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            ContributionHeatMap(
                title = "待办完成",
                contributions = todoContributions,
                modifier = Modifier.padding(16.dp)
            )
        }

        // 账单记录热力图
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            ContributionHeatMap(
                title = "账单记录",
                contributions = billContributions,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
} 