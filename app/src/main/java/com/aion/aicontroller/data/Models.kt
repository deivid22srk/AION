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
        id = "llama-3.2-vision-1b-q4",
        name = "LLaMA 3.2 Vision 1B (Q4) ⚡",
        description = "ULTRA LEVE! Modelo multimodal mais rápido e eficiente - RECOMENDADO (0.9 GB)",
        repoId = "mllmTeam/llama-3.2-1b-mllm",
        modelFilename = "llama-3.2-1b-q4_k.mllm",
        mmProjFilename = "llama-3.2-1b-mmproj-f16.mllm",
        estimatedSize = "0.9 GB"
    ),
    LocalVisionModel(
        id = "llama-3.2-vision-3b-q4",
        name = "LLaMA 3.2 Vision 3B (Q4) ⚡",
        description = "Modelo leve e poderoso da Meta - RECOMENDADO (1.9 GB)",
        repoId = "mllmTeam/llama-3.2-3b-mllm",
        modelFilename = "llama-3.2-3b-q4_k.mllm",
        mmProjFilename = "llama-3.2-3b-mmproj-f16.mllm",
        estimatedSize = "1.9 GB"
    ),
    LocalVisionModel(
        id = "minicpm-v-2b-q4",
        name = "MiniCPM-V 2B (Q4) 🔥",
        description = "Modelo multimodal ultra eficiente para mobile (1.4 GB)",
        repoId = "openbmb/MiniCPM-V-2-gguf",
        modelFilename = "minicpm-v-2-q4_k.gguf",
        mmProjFilename = "minicpm-v-2-mmproj-f16.gguf",
        estimatedSize = "1.4 GB"
    ),
    LocalVisionModel(
        id = "llava-phi-3-mini-q4",
        name = "LLaVA Phi-3 Mini (Q4) 💎",
        description = "Modelo compacto e rápido da Microsoft (2.5 GB)",
        repoId = "xtuner/llava-phi-3-mini-gguf",
        modelFilename = "llava-phi-3-mini-int4.gguf",
        mmProjFilename = "llava-phi-3-mini-mmproj-f16.gguf",
        estimatedSize = "2.5 GB"
    ),
    LocalVisionModel(
        id = "gemma-2-2b-vision-q4",
        name = "Gemma 2 2B Vision (Q4) 🌟",
        description = "Modelo multimodal eficiente do Google (1.6 GB)",
        repoId = "mllmTeam/gemma-2-2b-mllm",
        modelFilename = "gemma-2-2b-vision-q4_k.mllm",
        mmProjFilename = "gemma-2-2b-mmproj-f16.mllm",
        estimatedSize = "1.6 GB"
    ),
    LocalVisionModel(
        id = "llava-v1.6-mistral-7b-q4",
        name = "LLaVA 1.6 Mistral 7B (Q4)",
        description = "Modelo de visão compacto e preciso (4.37 GB)",
        repoId = "cjpais/llava-1.6-mistral-7b-gguf",
        modelFilename = "llava-v1.6-mistral-7b.Q4_K_M.gguf",
        mmProjFilename = "mmproj-model-f16.gguf",
        estimatedSize = "4.37 GB"
    ),
    LocalVisionModel(
        id = "llava-v1.5-7b-q4",
        name = "LLaVA 1.5 7B (Q4)",
        description = "Versão clássica estável e testada (4.08 GB)",
        repoId = "mys/ggml_llava-v1.5-7b",
        modelFilename = "ggml-model-q4_k.gguf",
        mmProjFilename = "mmproj-model-f16.gguf",
        estimatedSize = "4.08 GB"
    ),
    LocalVisionModel(
        id = "phi-3-vision-q4",
        name = "Phi-3 Vision (Q4)",
        description = "Modelo multimodal avançado da Microsoft (2.8 GB)",
        repoId = "mllmTeam/phi-3-vision-mllm",
        modelFilename = "phi-3-vision-q4_k.mllm",
        mmProjFilename = "phi-3-vision-mmproj-f16.mllm",
        estimatedSize = "2.8 GB"
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
