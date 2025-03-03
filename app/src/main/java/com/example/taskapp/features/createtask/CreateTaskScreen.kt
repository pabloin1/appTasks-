// Archivo: app/src/main/java/com/example/taskapp/features/createtask/CreateTaskScreen.kt
package com.example.taskapp.features.createtask

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskapp.core.ui.TaskButton
import com.example.taskapp.core.ui.TaskTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onBackClick: () -> Unit,
    onTaskCreated: () -> Unit
) {
    // Creamos el ViewModel usando la factory actualizada
    val context = LocalContext.current
    val viewModel: CreateTaskViewModel = viewModel(factory = CreateTaskViewModel.Factory(context))

    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Efecto para mostrar errores
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Efecto para navegar cuando la tarea se crea con éxito
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onTaskCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Nueva Tarea") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                TaskTextField(
                    value = state.title,
                    onValueChange = { viewModel.onTitleChanged(it) },
                    label = "Título",
                    placeholder = "Introduce el título de la tarea",
                    leadingIcon = Icons.Default.Title
                )

                Spacer(modifier = Modifier.height(16.dp))

                TaskTextField(
                    value = state.description,
                    onValueChange = { viewModel.onDescriptionChanged(it) },
                    label = "Descripción",
                    placeholder = "Introduce la descripción de la tarea",
                    leadingIcon = Icons.Default.Description,
                    keyboardType = KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(32.dp))

                TaskButton(
                    text = "Crear Tarea",
                    onClick = { viewModel.createTask() },
                    isLoading = state.isLoading,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}