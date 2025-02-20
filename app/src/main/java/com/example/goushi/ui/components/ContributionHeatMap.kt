package com.example.goushi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

data class DayContribution(
    val date: LocalDate,
    val count: Int
)

@Composable
fun ContributionHeatMap(
    title: String,
    contributions: List<DayContribution>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 显示星期标签
            Column(
                modifier = Modifier.padding(end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                DayOfWeek.values().forEach { day ->
                    if (day.ordinal % 2 == 0) {
                        Text(
                            text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.width(24.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            val dateToContribution = contributions.associateBy { it.date }
            val today = LocalDate.now()
            
            // 获取今年1月1日
            val yearStart = LocalDate.of(today.year, 1, 1)
            // 计算1月1日是星期几（1是周一，7是周日）
            val firstDayOfWeek = yearStart.dayOfWeek.value
            
            // 计算从1月1日到今天的总天数
            val totalDays = today.dayOfYear
            
            // 计算需要多少列（周数）
            val totalWeeks = (firstDayOfWeek + totalDays - 1) / 7 + 1

            // 显示热力图网格
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(totalWeeks) { weekIndex ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        (0..6).forEach { dayIndex ->
                            // 计算当前格子对应的日期
                            val dayOffset = weekIndex * 7 + dayIndex - (firstDayOfWeek - 1)
                            val currentDate = if (dayOffset >= 0) {
                                yearStart.plusDays(dayOffset.toLong())
                            } else null

                            if (currentDate == null || currentDate.isAfter(today)) {
                                // 显示空格子
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color.Transparent)
                                        .border(0.5.dp, Color.Gray.copy(alpha = 0.2f))
                                )
                                return@forEach
                            }

                            val contribution = dateToContribution[currentDate]
                            val intensity = when {
                                contribution?.count ?: 0 >= 4 -> 1.0f
                                contribution?.count ?: 0 >= 3 -> 0.75f
                                contribution?.count ?: 0 >= 2 -> 0.5f
                                contribution?.count ?: 0 >= 1 -> 0.25f
                                else -> 0.0f
                            }

                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        when (title) {
                                            "随笔贡献" -> Color(0xFFE57373).copy(alpha = intensity)
                                            "待办完成" -> Color(0xFFFFB74D).copy(alpha = intensity)
                                            "账单记录" -> Color(0xFF81C784).copy(alpha = intensity)
                                            else -> Color.Gray.copy(alpha = intensity)
                                        }
                                    )
                                    .border(0.5.dp, Color.Gray.copy(alpha = 0.2f))
                            )
                        }
                    }
                }
            }
        }
    }
} 