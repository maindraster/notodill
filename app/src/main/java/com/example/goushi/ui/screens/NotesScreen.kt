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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goushi.data.model.Note
import com.example.goushi.ui.viewmodel.NoteViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    viewModel: NoteViewModel = hiltViewModel(),
    onNavigateToEdit: (Int?) -> Unit
) {
    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // 错误处理
    LaunchedEffect(error) {
        error?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // 生命周期日志
    LaunchedEffect(Unit) {
        Log.d("NotesScreen", "Screen launched")
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("NotesScreen", "Screen disposed")
        }
    }

    Scaffold(
        topBar = {
            Column {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { query -> viewModel.setSearchQuery(query) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                TagsBar(
                    tags = notes.flatMap { note -> note.tags }.distinct(),
                    selectedTags = selectedTags,
                    onTagClick = { tag -> viewModel.toggleTag(tag) },
                    modifier = Modifier.fillMaxWidth()
                )

                MonthSelector(
                    currentMonth = currentMonth,
                    onMonthSelected = { month -> viewModel.setCurrentMonth(month) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEdit(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加笔记")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (notes.isEmpty()) {
                Text(
                    text = "暂无笔记",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = notes,
                        key = { note -> note.id }
                    ) { note ->
                        NoteCardContent(
                            note = note,
                            isEditMode = isEditMode,
                            onStarClick = {
                                try {
                                    viewModel.toggleNoteStar(note)
                                } catch (e: Exception) {
                                    Log.e("NotesScreen", "Error toggling star", e)
                                    Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDelete = {
                                try {
                                    viewModel.deleteNote(note)
                                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("NotesScreen", "Error deleting note", e)
                                    Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onMoveToTop = {
                                try {
                                    viewModel.moveNoteToTop(note)
                                } catch (e: Exception) {
                                    Log.e("NotesScreen", "Error moving note", e)
                                    Toast.makeText(context, "移动失败", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onLongClick = viewModel::toggleEditMode,
                            onNavigateToEdit = onNavigateToEdit,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("搜索笔记") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清除"
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 30.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun TagsBar(
    tags: List<String>,
    selectedTags: Set<String>,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    Column(modifier = modifier) {
        IconButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)  // 减小高度
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "收起标签" else "展开标签",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isExpanded && tags.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tags) { tag ->
                    val isSelected = tag in selectedTags
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTagClick(tag) },
                        label = { Text(text = tag) },
                        modifier = Modifier.padding(vertical = 4.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    currentMonth: String,
    onMonthSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                // 计算上个月
                val currentDate = LocalDate.parse("$currentMonth-01")
                val previousMonth = currentDate.minusMonths(1)
                onMonthSelected(previousMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))
            }
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
        }

        Text(
            text = currentMonth,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        IconButton(
            onClick = {
                // 计算下个月
                val currentDate = LocalDate.parse("$currentMonth-01")
                val nextMonth = currentDate.plusMonths(1)
                onMonthSelected(nextMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))
            }
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCardContent(
    note: Note,
    isEditMode: Boolean,
    onStarClick: () -> Unit,
    onDelete: () -> Unit,
    onMoveToTop: () -> Unit,
    onLongClick: () -> Unit,
    onNavigateToEdit: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isEditMode) 0.95f else 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .scale(scale)
            .combinedClickable(
                onClick = { onNavigateToEdit(note.id) },
                onLongClick = onLongClick
            )
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (note.content.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "创建于 ${note.createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (note.updateTime != note.createTime) {
                        Text(
                            text = "更新于 ${note.updateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onStarClick) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "收藏",
                            tint = when (note.starLevel) {
                                1 -> MaterialTheme.colorScheme.primary
                                2 -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    if (isEditMode) {
                        IconButton(onClick = onMoveToTop) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "移到顶部"
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(note.tags) { tag ->
                        AssistChip(
                            onClick = { /* 点击标签 */ },
                            label = { Text(tag) }
                        )
                    }
                }
            }
        }
    }
} 