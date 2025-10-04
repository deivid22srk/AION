package com.aion.aicontroller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aion.aicontroller.data.AVAILABLE_LOCAL_MODELS
import com.aion.aicontroller.data.PreferencesManager
import com.aion.aicontroller.data.Status
import com.aion.aicontroller.data.LocalVisionModel
import com.aion.aicontroller.local.LocalModelManager
import com.aion.aicontroller.service.AIAccessibilityService
import com.aion.aicontroller.service.AIControlService
import com.aion.aicontroller.ui.theme.AIONTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var modelManager: LocalModelManager
    private var aiControlService: AIControlService? = null
    private var serviceBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AIControlService.LocalBinder
            aiControlService = binder.getService()
            serviceBound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            aiControlService = null
            serviceBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        preferencesManager = PreferencesManager(this)
        modelManager = LocalModelManager(this)
        
        val intent = Intent(this, AIControlService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        setContent {
            AIONTheme {
                MainScreen(
                    preferencesManager = preferencesManager,
                    modelManager = modelManager,
                    getService = { aiControlService },
                    openAccessibilitySettings = { openAccessibilitySettings() }
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
    
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    preferencesManager: PreferencesManager,
    modelManager: LocalModelManager,
    getService: () -> AIControlService?,
    openAccessibilitySettings: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val selectedLocalModel by preferencesManager.selectedLocalModel.collectAsState(initial = PreferencesManager.DEFAULT_LOCAL_MODEL)
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val service = getService()
    val taskStatus by (service?.taskStatus?.collectAsState() ?: remember { mutableStateOf(com.aion.aicontroller.data.TaskStatus(Status.IDLE)) })
    val logs by (service?.logs?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AION", fontWeight = FontWeight.Bold)
                        Text(
                            "AI Android Controller - Local",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Principal") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CloudDownload, contentDescription = null) },
                    label = { Text("Modelos") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Configurações") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> MainTab(
                    preferencesManager = preferencesManager,
                    modelManager = modelManager,
                    selectedLocalModel = selectedLocalModel,
                    getService = getService,
                    openAccessibilitySettings = openAccessibilitySettings,
                    taskStatus = taskStatus,
                    logs = logs
                )
                1 -> ModelsTab(
                    modelManager = modelManager,
                    preferencesManager = preferencesManager,
                    selectedLocalModel = selectedLocalModel
                )
                2 -> SettingsTab(
                    context = context,
                    preferencesManager = preferencesManager,
                    getService = getService,
                    openOverlaySettings = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MainTab(
    preferencesManager: PreferencesManager,
    modelManager: LocalModelManager,
    selectedLocalModel: String,
    getService: () -> AIControlService?,
    openAccessibilitySettings: () -> Unit,
    taskStatus: com.aion.aicontroller.data.TaskStatus,
    logs: List<String>
) {
    val isAccessibilityEnabled = AIAccessibilityService.isServiceEnabled()
    var taskInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    val selectedModel = AVAILABLE_LOCAL_MODELS.find { it.id == selectedLocalModel }
    val isModelDownloaded = selectedModel?.let { modelManager.isModelDownloaded(it) } ?: false
    val isModelLoaded = getService()?.isModelLoaded() ?: false
    
    LaunchedEffect(selectedLocalModel, isModelDownloaded) {
        if (isModelDownloaded && selectedModel != null && !isModelLoaded) {
            val modelFile = modelManager.getModelFile(selectedModel)
            val mmProjFile = modelManager.getMMProjFile(selectedModel)
            getService()?.setupLocalAI(modelFile.absolutePath, mmProjFile.absolutePath)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AccessibilityCard(
            isEnabled = isAccessibilityEnabled,
            onOpenSettings = openAccessibilitySettings
        )
        
        if (!isModelDownloaded) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Text("Baixe um modelo na aba 'Modelos' para começar")
                }
            }
        } else if (!isModelLoaded) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Carregando modelo...")
                }
            }
        }
        
        StatusCard(taskStatus = taskStatus)
        
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Digite uma tarefa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = taskInput,
                    onValueChange = { taskInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: Abrir o Chrome e pesquisar por receitas") },
                    minLines = 3,
                    maxLines = 5
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (taskInput.isNotEmpty() && isModelDownloaded && isModelLoaded && isAccessibilityEnabled) {
                                getService()?.executeTask(taskInput)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = taskInput.isNotEmpty() && isModelDownloaded && isModelLoaded &&
                                  isAccessibilityEnabled && taskStatus.status != Status.PROCESSING && 
                                  taskStatus.status != Status.EXECUTING
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Executar")
                    }
                    
                    if (taskStatus.status == Status.PROCESSING || taskStatus.status == Status.EXECUTING) {
                        Button(
                            onClick = { getService()?.stopTask() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                        }
                    }
                }
            }
        }
        
        LogCard(
            logs = logs,
            onClearLogs = { getService()?.clearLogs() }
        )
    }
}

@Composable
fun ModelsTab(
    modelManager: LocalModelManager,
    preferencesManager: PreferencesManager,
    selectedLocalModel: String
) {
    val coroutineScope = rememberCoroutineScope()
    var downloadingModel by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf(0) }
    var downloadStage by remember { mutableStateOf("") }
    val totalSize = remember { modelManager.getTotalModelsSize() }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(32.dp))
                        Column {
                            Text(
                                "Modelos Locais de Visão",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Espaço usado: ${modelManager.formatFileSize(totalSize)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
        
        items(AVAILABLE_LOCAL_MODELS) { model ->
            val isDownloaded = modelManager.isModelDownloaded(model)
            val isSelected = model.id == selectedLocalModel
            val isDownloading = downloadingModel == model.id
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                model.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                model.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    model.estimatedSize,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                        
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    if (isDownloading) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                downloadStage,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LinearProgressIndicator(
                                progress = { downloadProgress / 100f },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                "$downloadProgress%",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isDownloaded) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        preferencesManager.saveSelectedLocalModel(model.id)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSelected
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isSelected) "Selecionado" else "Selecionar")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        modelManager.deleteModel(model)
                                    }
                                },
                                enabled = !isDownloading && !isSelected
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        } else {
                            Button(
                                onClick = {
                                    downloadingModel = model.id
                                    downloadStage = "Baixando modelo..."
                                    
                                    coroutineScope.launch {
                                        try {
                                            modelManager.downloadModel(model).collect { progress ->
                                                downloadProgress = progress.percentage
                                            }
                                            
                                            downloadStage = "Baixando mmproj..."
                                            downloadProgress = 0
                                            
                                            modelManager.downloadMMProj(model).collect { progress ->
                                                downloadProgress = progress.percentage
                                            }
                                            
                                            downloadingModel = null
                                            downloadStage = ""
                                            downloadProgress = 0
                                            
                                        } catch (e: Exception) {
                                            downloadingModel = null
                                            downloadStage = "Erro: ${e.message}"
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isDownloading
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Baixar Modelo")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccessibilityCard(
    isEnabled: Boolean,
    onOpenSettings: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isEnabled) "Serviço Ativo" else "Serviço Desativado",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (isEnabled) 
                        "O serviço de acessibilidade está ativo" 
                    else 
                        "Ative o serviço de acessibilidade para usar o AION",
                    fontSize = 12.sp
                )
            }
            if (!isEnabled) {
                FilledTonalButton(onClick = onOpenSettings) {
                    Text("Ativar")
                }
            }
        }
    }
}

@Composable
fun StatusCard(taskStatus: com.aion.aicontroller.data.TaskStatus) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (taskStatus.status) {
                    Status.IDLE -> Icon(Icons.Default.HourglassEmpty, contentDescription = null)
                    Status.PROCESSING -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Status.EXECUTING -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Status.COMPLETED -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                    Status.ERROR -> Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        when (taskStatus.status) {
                            Status.IDLE -> "Aguardando"
                            Status.PROCESSING -> "Processando"
                            Status.EXECUTING -> "Executando"
                            Status.COMPLETED -> "Concluído"
                            Status.ERROR -> "Erro"
                        },
                        fontWeight = FontWeight.Bold
                    )
                    if (taskStatus.message.isNotEmpty()) {
                        Text(taskStatus.message, fontSize = 12.sp)
                    }
                }
            }
            
            if (taskStatus.progress > 0) {
                LinearProgressIndicator(
                    progress = { taskStatus.progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun LogCard(
    logs: List<String>,
    onClearLogs: () -> Unit
) {
    Card(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Log de Atividades",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (logs.isNotEmpty()) {
                    IconButton(onClick = onClearLogs) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpar")
                    }
                }
            }
            
            HorizontalDivider()
            
            val listState = rememberLazyListState()
            
            LaunchedEffect(logs.size) {
                if (logs.isNotEmpty()) {
                    listState.animateScrollToItem(logs.size - 1)
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (logs.isEmpty()) {
                    item {
                        Text(
                            "Nenhuma atividade registrada",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    items(logs) { log ->
                        Text(
                            log,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTab(
    context: Context,
    preferencesManager: PreferencesManager,
    getService: () -> AIControlService?,
    openOverlaySettings: () -> Unit
) {
    val floatingLogEnabled by preferencesManager.floatingLogEnabled.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(floatingLogEnabled) {
        getService()?.setFloatingLogEnabled(floatingLogEnabled)
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Log Flutuante",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        "Exibe logs sobre todos os aplicativos (invisível em screenshots)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ativar log flutuante")
                        Switch(
                            checked = floatingLogEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (!Settings.canDrawOverlays(context)) {
                                        openOverlaySettings()
                                    } else {
                                        coroutineScope.launch {
                                            preferencesManager.saveFloatingLogEnabled(enabled)
                                        }
                                    }
                                } else {
                                    coroutineScope.launch {
                                        preferencesManager.saveFloatingLogEnabled(enabled)
                                    }
                                }
                            }
                        )
                    }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
                        !Settings.canDrawOverlays(context) && 
                        floatingLogEnabled) {
                        Text(
                            "⚠ Permissão de sobreposição necessária",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        FilledTonalButton(
                            onClick = openOverlaySettings,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Conceder Permissão")
                        }
                    }
                }
            }
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Sobre o AION",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "AION é um controlador de Android baseado em IA que usa modelos locais de visão do Hugging Face para executar tarefas automaticamente.",
                        fontSize = 12.sp
                    )
                    Text(
                        "Versão 2.0 - Local AI",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
