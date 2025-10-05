package com.aion.aicontroller.data

/**
 * Modelos de Visão Local suportados
 * 
 * Baseado no Google AI Edge Gallery
 */
data class VisionModel(
    val id: String,
    val name: String,
    val description: String,
    val type: ModelType,
    val url: String,
    val size: Long,
    val fileName: String,
    val requiredFiles: List<String> = emptyList(),
    val supportsVision: Boolean = true,
    val supportsStreaming: Boolean = false,
    val minRamMb: Int = 2048
)

enum class ModelType {
    TFLITE_VISION,      // TensorFlow Lite para detecção de objetos
    LITERT_MULTIMODAL,  // LiteRT LM para visão + linguagem
    GGUF_VISION         // Modelos GGUF com suporte a visão (LLaVA)
}

/**
 * Catálogo de modelos de visão disponíveis
 */
object VisionModelCatalog {
    
    // Modelos TFLite para detecção de elementos UI
    val MOBILENET_OBJECT_DETECTION = VisionModel(
        id = "mobilenet_v1_detection",
        name = "MobileNet V1 Object Detection",
        description = "Modelo leve e rápido para detecção de elementos UI. Ideal para análise rápida de telas.",
        type = ModelType.TFLITE_VISION,
        url = "https://storage.googleapis.com/tfweb/app_gallery_models/mobilenet_v1.tflite",
        size = 16900760L,
        fileName = "mobilenet_v1_detector.tflite",
        requiredFiles = listOf("mobilenet_labels_v1.txt"),
        minRamMb = 1024
    )
    
    val EFFICIENTDET_LITE = VisionModel(
        id = "efficientdet_lite0",
        name = "EfficientDet-Lite0",
        description = "Detector de objetos eficiente e preciso. Bom balanço entre velocidade e precisão.",
        type = ModelType.TFLITE_VISION,
        url = "https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite0/int8/1/efficientdet_lite0.tflite",
        size = 4400000L,
        fileName = "efficientdet_lite0.tflite",
        minRamMb = 1024
    )
    
    // Modelos LiteRT Multimodais (visão + linguagem)
    val GEMMA_2B_VISION = VisionModel(
        id = "gemma_2b_vision",
        name = "Gemma 2B Vision",
        description = "Modelo multimodal compacto do Google. Analisa imagens e gera texto. Requer 3GB RAM.",
        type = ModelType.LITERT_MULTIMODAL,
        url = "https://huggingface.co/litert-community/gemma-2b-it-gpu-int4/resolve/main/gemma-2b-it-gpu-int4.bin",
        size = 1700000000L,
        fileName = "gemma_2b_vision.bin",
        supportsStreaming = true,
        minRamMb = 3072
    )
    
    val PALIGEMMA_3B = VisionModel(
        id = "paligemma_3b",
        name = "PaliGemma 3B",
        description = "Modelo multimodal especializado em visão. Excelente para análise de UI. Requer 4GB RAM.",
        type = ModelType.LITERT_MULTIMODAL,
        url = "https://huggingface.co/litert-community/paligemma-3b-mix-224-gpu-int4/resolve/main/paligemma-3b-mix-224.bin",
        size = 2100000000L,
        fileName = "paligemma_3b.bin",
        supportsStreaming = true,
        minRamMb = 4096
    )
    
    // Modelos GGUF com visão (LLaVA)
    val LLAVA_LLAMA3_2_1B = VisionModel(
        id = "llava_llama3_2_1b",
        name = "LLaVA Llama 3.2 1B",
        description = "Modelo LLaVA ultra compacto baseado em Llama 3.2. Roda bem em dispositivos limitados.",
        type = ModelType.GGUF_VISION,
        url = "https://huggingface.co/xtuner/llava-llama-3.2-1b-vision-q4_k_m-gguf/resolve/main/llava-llama-3.2-1b-vision-q4_k_m.gguf",
        size = 900000000L,
        fileName = "llava_llama3_2_1b.gguf",
        requiredFiles = listOf("mmproj-llama3-2-1b-f16.gguf"),
        minRamMb = 2048
    )
    
    val LLAVA_PHI3_MINI = VisionModel(
        id = "llava_phi3_mini",
        name = "LLaVA Phi-3 Mini",
        description = "Modelo LLaVA compacto baseado em Phi-3. Excelente para dispositivos móveis.",
        type = ModelType.GGUF_VISION,
        url = "https://huggingface.co/xtuner/llava-phi-3-mini-gguf/resolve/main/llava-phi-3-mini-q4_k_m.gguf",
        size = 2500000000L,
        fileName = "llava_phi3_mini.gguf",
        requiredFiles = listOf("mmproj-phi3-mini-f16.gguf"),
        minRamMb = 3072
    )
    
    /**
     * Lista de todos os modelos recomendados
     */
    val RECOMMENDED_MODELS = listOf(
        MOBILENET_OBJECT_DETECTION,
        EFFICIENTDET_LITE,
        GEMMA_2B_VISION,
        LLAVA_LLAMA3_2_1B,
        LLAVA_PHI3_MINI
    )
    
    /**
     * Lista de modelos leves (< 2GB RAM)
     */
    val LIGHTWEIGHT_MODELS = RECOMMENDED_MODELS.filter { it.minRamMb < 2048 }
    
    /**
     * Lista de modelos por tipo
     */
    fun getModelsByType(type: ModelType): List<VisionModel> {
        return RECOMMENDED_MODELS.filter { it.type == type }
    }
    
    /**
     * Busca modelo por ID
     */
    fun getModelById(id: String): VisionModel? {
        return RECOMMENDED_MODELS.find { it.id == id }
    }
}
