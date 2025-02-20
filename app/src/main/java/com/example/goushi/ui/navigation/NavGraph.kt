package com.example.goushi.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.goushi.ui.screens.NoteEditScreen
import com.example.goushi.ui.screens.NotesScreen
import com.example.goushi.ui.screens.TodoEditScreen
import com.example.goushi.ui.screens.TodosScreen
import com.example.goushi.ui.screens.BillsScreen
import com.example.goushi.ui.screens.BillEditScreen
import com.example.goushi.ui.screens.MyPageScreen

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Notes : Screen("notes", Icons.Default.Edit, "随笔")
    object Todos : Screen("todos", Icons.Default.DateRange, "待办")
    object Bills : Screen("bills", Icons.Default.Add, "账单")
    object Profile : Screen("profile", Icons.Default.AccountCircle, "我的")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var showBottomBar by remember { mutableStateOf(true) }
    val screens = listOf(Screen.Notes, Screen.Todos, Screen.Bills, Screen.Profile)
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    
                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Notes.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Notes.route) {
                showBottomBar = true
                NotesScreen(
                    onNavigateToEdit = { noteId -> 
                        navController.navigate("edit_note/${noteId ?: "new"}")
                    }
                )
            }
            composable(
                route = "edit_note/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.StringType })
            ) {
                showBottomBar = false
                val noteId = it.arguments?.getString("noteId")
                NoteEditScreen(
                    noteId = if (noteId == "new") null else noteId?.toInt(),
                    onNavigateBack = {
                        showBottomBar = true
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Todos.route) {
                showBottomBar = true
                TodosScreen(
                    onNavigateToEdit = {
                        navController.navigate("edit_todo")
                    }
                )
            }
            composable("edit_todo") {
                showBottomBar = false
                TodoEditScreen(
                    onNavigateBack = {
                        showBottomBar = true
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Bills.route) {
                showBottomBar = true
                BillsScreen(
                    onNavigateToEdit = {
                        navController.navigate("edit_bill")
                    }
                )
            }
            composable("edit_bill") {
                showBottomBar = false
                BillEditScreen(
                    onNavigateBack = {
                        showBottomBar = true
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Profile.route) {
                showBottomBar = true
                MyPageScreen()
            }
        }
    }
}