package com.example.goushi.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goushi.data.model.Note
import com.example.goushi.ui.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Int? = null,
    viewModel: NoteViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var selectedImages by remember { mutableStateOf(listOf<String>()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.getNoteById(noteId)?.let { note ->
                title = note.title
                content = note.content
                selectedTags = note.tags.toSet()
                selectedImages = note.images
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "新建笔记" else "编辑笔记") },
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
                            scope.launch {
                                try {
                                    val note = Note(
                                        id = noteId ?: 0,
                                        title = title.trim(),
                                        content = content.trim(),
                                        tags = selectedTags.toList(),
                                        images = selectedImages,
                                        createTime = if (noteId == null) LocalDateTime.now() else viewModel.getNoteById(noteId)?.createTime ?: LocalDateTime.now(),
                                        updateTime = LocalDateTime.now()
                                    )
                                    if (noteId == null) {
                                        viewModel.insertNote(note)
                                    } else {
                                        viewModel.updateNote(note)
                                    }
                                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
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
            
            // 图片选择
            // TODO: 实现图片选择功能
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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