package com.aion.aicontroller.data

data class LocalVisionModel(
    val id: String,
    val name: String,
    val description: String,
    val repoId: String,
    val modelFilename: String,
    val mmProjFilename: String,
    val estimatedSize: String,
    val isLiteRT: Boolean = false
)

val AVAILABLE_LOCAL_MODELS = listOf(
    LocalVisionModel(
        id = "gemma3-1b-it",
        name = "Gemma 3 1B (LiteRT) 🔥",
        description = "ULTRA LEVE! Modelo multimodal oficial do Google - RECOMENDADO para a maioria dos dispositivos (584 MB)",
        repoId = "litert-community/Gemma3-1B-IT",
        modelFilename = "gemma3-1b-it-int4.litertlm",
        mmProjFilename = "",
        estimatedSize = "584 MB",
        isLiteRT = true
    ),
    LocalVisionModel(
        id = "gemma-3n-e2b-it",
        name = "Gemma 3n E2B (LiteRT) ⚡",
        description = "Modelo multimodal avançado com 2B parâmetros eficientes - Alta precisão (3.39 GB)",
        repoId = "google/gemma-3n-E2B-it-litert-lm",
        modelFilename = "gemma-3n-E2B-it-int4.litertlm",
        mmProjFilename = "",
        estimatedSize = "3.39 GB",
        isLiteRT = true
    )
)

data class AIAction(
    val type: ActionType,
    val target: String? = null,
    val x: Int? = null,
    val y: Int? = null,
    val text: String? = null,
    val direction: String? = null,
    val amount: Int? = null
)

enum class ActionType {
    CLICK,
    LONG_CLICK,
    TYPE_TEXT,
    SCROLL,
    SWIPE,
    BACK,
    HOME,
    RECENT_APPS,
    OPEN_APP,
    WAIT,
    TAKE_SCREENSHOT
}

data class TaskStatus(
    val status: Status,
    val message: String = "",
    val progress: Int = 0
)

enum class Status {
    IDLE,
    PROCESSING,
    EXECUTING,
    COMPLETED,
    ERROR
}
