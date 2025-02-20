package com.example.goushi.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goushi.data.model.Todo
import com.example.goushi.ui.viewmodel.TodoViewModel
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodosScreen(
    viewModel: TodoViewModel = hiltViewModel(),
    onNavigateToEdit: () -> Unit
) {
    val todos by viewModel.todos.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(error) {
        error?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToEdit,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加待办")
            }
        }
    ) { paddingValues ->
        val activeTodos = todos.filter { !it.isCompleted && it.endTime.isAfter(LocalDateTime.now()) }
        val completedOrOverdueTodos = todos.filter { it.isCompleted || it.endTime.isBefore(LocalDateTime.now()) }

        val sortedTodos = activeTodos.sortedWith(compareBy { it.endTime }) + completedOrOverdueTodos

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(sortedTodos) { todo ->
                TodoCard(
                    todo = todo,
                    isEditMode = false,
                    onToggleComplete = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleTodoComplete(todo)
                    },
                    onDelete = { viewModel.deleteTodo(todo) },
                    onLongClick = {
                        viewModel.deleteTodo(todo)
                        Toast.makeText(context, "To-do deleted", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .background(if (todo.isCompleted) Color.Gray else Color.Transparent)
                        .combinedClickable(
                            onClick = { onNavigateToEdit() },
                            onLongClick = {
                                viewModel.deleteTodo(todo)
                                Toast.makeText(context, "To-do deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (todo.isCompleted) Color.Gray else Color.Transparent)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoCard(
    todo: Todo,
    isEditMode: Boolean,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverdue = todo.endTime.isBefore(LocalDateTime.now()) && !todo.isCompleted
    val cardColor = when {
        todo.isCompleted -> Color.Gray // 完成的待办
        isOverdue -> MaterialTheme.colorScheme.errorContainer // 超时的待办
        else -> MaterialTheme.colorScheme.surface // 正常的待办
    }

    val scale by animateFloatAsState(
        targetValue = if (isEditMode && !todo.isCompleted) 0.95f else 1f
    )

    Card(
        modifier = modifier
            .scale(scale)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 完成按钮
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (todo.isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = "完成状态",
                            tint = if (todo.isCompleted) MaterialTheme.colorScheme.onSurface
                                   else MaterialTheme.colorScheme.primary
                        )
                    }

                    // 标题和内容
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = todo.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (todo.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                                   else MaterialTheme.colorScheme.onSurface
                        )
                        if (todo.content.isNotBlank()) {
                            Text(
                                text = todo.content,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = if (todo.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            // 剩余时间，移到下方显示
            if (!todo.isCompleted) {
                RemainingTime(
                    endTime = todo.endTime,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun RemainingTime(
    endTime: LocalDateTime,
    modifier: Modifier = Modifier
) {
    var remainingTime by remember { mutableStateOf(calculateRemainingTime(endTime)) }
    
    LaunchedEffect(endTime) {
        while (true) {
            delay(1000) // 每秒更新一次
            remainingTime = calculateRemainingTime(endTime)
        }
    }

    val isOverdue = endTime.isBefore(LocalDateTime.now())
    
    Text(
        text = remainingTime,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        ),
        color = if (isOverdue) MaterialTheme.colorScheme.error
               else MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

private fun calculateRemainingTime(endTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val duration = if (endTime.isAfter(now)) {
        Duration.between(now, endTime)
    } else {
        Duration.between(endTime, now)
    }
    
    val days = duration.toDays()
    val hours = duration.toHoursPart()
    val minutes = duration.toMinutesPart()
    
    return buildString {
        if (days > 0) append("${days}天")
        if (hours > 0) append("${hours}时")
        if (minutes > 0 || (days == 0L && hours == 0)) append("${minutes}分")
        if (endTime.isBefore(now) && !endTime.isEqual(now)) append(" 已超时")
    }
}
