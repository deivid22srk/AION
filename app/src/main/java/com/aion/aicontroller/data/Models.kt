package com.aion.aicontroller.data

data class FreeModel(
    val id: String,
    val name: String,
    val description: String,
    val supportsVision: Boolean
)

val AVAILABLE_FREE_MODELS = listOf(
    FreeModel(
        id = "qwen/qwen2.5-vl-72b-instruct:free",
        name = "Qwen 2.5 VL 72B",
        description = "Modelo multimodal poderoso com visão (72B parâmetros)",
        supportsVision = true
    ),
    FreeModel(
        id = "qwen/qwen2.5-vl-32b-instruct:free",
        name = "Qwen 2.5 VL 32B",
        description = "Modelo multimodal equilibrado com visão (32B parâmetros)",
        supportsVision = true
    ),
    FreeModel(
        id = "meta-llama/llama-3.2-11b-vision-instruct:free",
        name = "Llama 3.2 11B Vision",
        description = "Modelo Meta com capacidades de visão",
        supportsVision = true
    ),
    FreeModel(
        id = "google/gemma-3-27b-it:free",
        name = "Gemma 3 27B",
        description = "Modelo Google multimodal (27B parâmetros)",
        supportsVision = true
    ),
    FreeModel(
        id = "deepseek/deepseek-chat-v3.1:free",
        name = "DeepSeek V3.1",
        description = "Modelo de raciocínio híbrido (671B total, 37B ativo)",
        supportsVision = false
    ),
    FreeModel(
        id = "z-ai/glm-4.5-air:free",
        name = "GLM 4.5 Air",
        description = "Modelo leve para agentes (MoE)",
        supportsVision = false
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
