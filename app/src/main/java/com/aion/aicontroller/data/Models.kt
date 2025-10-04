package com.aion.aicontroller.data

data class LocalVisionModel(
    val id: String,
    val name: String,
    val description: String,
    val repoId: String,
    val modelFilename: String,
    val mmProjFilename: String,
    val estimatedSize: String
)

val AVAILABLE_LOCAL_MODELS = listOf(
    LocalVisionModel(
        id = "llava-v1.6-mistral-7b-q4",
        name = "LLaVA 1.6 Mistral 7B (Q4)",
        description = "Modelo de visão compacto e rápido - RECOMENDADO (4.37 GB)",
        repoId = "cjpais/llava-1.6-mistral-7b-gguf",
        modelFilename = "llava-v1.6-mistral-7b.Q4_K_M.gguf",
        mmProjFilename = "mmproj-model-f16.gguf",
        estimatedSize = "4.37 GB"
    ),
    LocalVisionModel(
        id = "llava-v1.6-vicuna-7b-q4",
        name = "LLaVA 1.6 Vicuna 7B (Q4)",
        description = "Modelo de visão equilibrado (4.37 GB)",
        repoId = "cjpais/llava-v1.6-vicuna-7b-gguf",
        modelFilename = "llava-v1.6-vicuna-7b.Q4_K_M.gguf",
        mmProjFilename = "mmproj-model-f16.gguf",
        estimatedSize = "4.37 GB"
    ),
    LocalVisionModel(
        id = "llava-v1.5-7b-q4",
        name = "LLaVA 1.5 7B (Q4)",
        description = "Versão clássica estável (4.08 GB)",
        repoId = "mys/ggml_llava-v1.5-7b",
        modelFilename = "ggml-model-q4_k.gguf",
        mmProjFilename = "mmproj-model-f16.gguf",
        estimatedSize = "4.08 GB"
    ),
    LocalVisionModel(
        id = "bakllava-1-q4",
        name = "BakLLaVA 1 7B (Q4)",
        description = "Especializado em tarefas visuais (4.37 GB)",
        repoId = "mys/ggml_bakllava-1",
        modelFilename = "ggml-model-q4_k.gguf",
        mmProjFilename = "mmproj-model-f16.gguf",
        estimatedSize = "4.37 GB"
    ),
    LocalVisionModel(
        id = "llava-phi-3-mini-q4",
        name = "LLaVA Phi-3 Mini (Q4)",
        description = "Modelo ultra compacto da Microsoft (2.5 GB)",
        repoId = "xtuner/llava-phi-3-mini-gguf",
        modelFilename = "llava-phi-3-mini-int4.gguf",
        mmProjFilename = "llava-phi-3-mini-mmproj-f16.gguf",
        estimatedSize = "2.5 GB"
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
