package com.aion.aicontroller.data

data class FreeModel(
    val id: String,
    val name: String,
    val description: String,
    val supportsVision: Boolean
)

val AVAILABLE_FREE_MODELS = listOf(
    FreeModel(
        id = "google/gemini-2.0-flash-exp:free",
        name = "Gemini 2.0 Flash Experimental",
        description = "Modelo Google mais rápido com visão (1.05M contexto) - RECOMENDADO",
        supportsVision = true
    ),
    FreeModel(
        id = "meta-llama/llama-4-maverick:free",
        name = "Llama 4 Maverick",
        description = "Modelo Meta avançado com visão (400B total, 17B ativo)",
        supportsVision = true
    ),
    FreeModel(
        id = "meta-llama/llama-4-scout:free",
        name = "Llama 4 Scout",
        description = "Llama 4 otimizado (109B total, 10M tokens contexto)",
        supportsVision = true
    ),
    FreeModel(
        id = "qwen/qwen2.5-vl-72b-instruct:free",
        name = "Qwen 2.5 VL 72B",
        description = "Modelo multimodal poderoso com visão (72B parâmetros)",
        supportsVision = true
    ),
    FreeModel(
        id = "mistralai/mistral-small-3.2-24b-instruct:free",
        name = "Mistral Small 3.2 24B",
        description = "Mistral atualizado com visão e melhor precisão (24B)",
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
        id = "moonshotai/kimi-vl-a3b-thinking:free",
        name = "Kimi VL A3B Thinking",
        description = "Modelo com visão e raciocínio avançado",
        supportsVision = true
    ),
    FreeModel(
        id = "deepseek/deepseek-r1:free",
        name = "DeepSeek R1",
        description = "Modelo de raciocínio avançado (671B total, 37B ativo)",
        supportsVision = false
    ),
    FreeModel(
        id = "tngtech/deepseek-r1t2-chimera:free",
        name = "DeepSeek R1T2 Chimera",
        description = "Modelo híbrido otimizado, 20% mais rápido (671B)",
        supportsVision = false
    ),
    FreeModel(
        id = "deepseek/deepseek-r1-distill-qwen-32b:free",
        name = "DeepSeek R1 Distill Qwen 32B",
        description = "Versão destilada rápida e eficiente (32B)",
        supportsVision = false
    ),
    FreeModel(
        id = "deepseek/deepseek-chat-v3.1:free",
        name = "DeepSeek Chat V3.1",
        description = "Modelo conversacional equilibrado (671B total, 37B ativo)",
        supportsVision = false
    ),
    FreeModel(
        id = "mistralai/mistral-small-3.1-24b-instruct:free",
        name = "Mistral Small 3.1 24B",
        description = "Modelo Mistral eficiente para instruções (24B)",
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
