package com.aion.aicontroller.data

/**
 * Modelos de Visão Local suportados (TensorFlow Lite)
 * 
 * Versão simplificada focada apenas em TensorFlow Lite
 * para máxima compatibilidade e estabilidade.
 */
data class VisionModel(
    val id: String,
    val name: String,
    val description: String,
    val url: String,
    val size: Long,
    val fileName: String,
    val requiredFiles: List<RequiredFile> = emptyList(),
    val minRamMb: Int = 1024,
    val maxResults: Int = 10,
    val scoreThreshold: Float = 0.3f
)

data class RequiredFile(
    val name: String,
    val url: String,
    val size: Long
)

/**
 * Catálogo de modelos de visão TensorFlow Lite
 */
object VisionModelCatalog {
    
    val EFFICIENTDET_LITE0 = VisionModel(
        id = "efficientdet_lite0",
        name = "EfficientDet-Lite0",
        description = "Modelo eficiente para detecção de objetos. Excelente balanço entre velocidade e precisão.",
        url = "https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite0/int8/1/efficientdet_lite0.tflite",
        size = 4400000L,
        fileName = "efficientdet_lite0.tflite",
        minRamMb = 1024,
        maxResults = 10,
        scoreThreshold = 0.3f
    )
    
    val EFFICIENTDET_LITE2 = VisionModel(
        id = "efficientdet_lite2",
        name = "EfficientDet-Lite2",
        description = "Versão mais precisa do EfficientDet. Recomendado para dispositivos com 4GB+ RAM.",
        url = "https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite2/int8/1/efficientdet_lite2.tflite",
        size = 7200000L,
        fileName = "efficientdet_lite2.tflite",
        minRamMb = 2048,
        maxResults = 15,
        scoreThreshold = 0.25f
    )
    
    val MOBILENET_V2 = VisionModel(
        id = "mobilenet_v2",
        name = "MobileNet V2",
        description = "Modelo leve e rápido. Ideal para dispositivos com recursos limitados.",
        url = "https://storage.googleapis.com/tfweb/app_gallery_models/mobilenet_v2.tflite",
        size = 13978596L,
        fileName = "mobilenet_v2.tflite",
        requiredFiles = listOf(
            RequiredFile(
                name = "labels",
                url = "https://raw.githubusercontent.com/leferrad/tensorflow-mobilenet/refs/heads/master/imagenet/labels.txt",
                size = 21685L
            )
        ),
        minRamMb = 1024,
        maxResults = 5,
        scoreThreshold = 0.4f
    )
    
    val SSD_MOBILENET_V1 = VisionModel(
        id = "ssd_mobilenet_v1",
        name = "SSD MobileNet V1",
        description = "Detector de objetos rápido e compacto. Ótimo para análise de UI em tempo real.",
        url = "https://storage.googleapis.com/download.tensorflow.org/models/tflite/coco_ssd_mobilenet_v1_1.0_quant_2018_06_29.zip",
        size = 6900000L,
        fileName = "detect.tflite",
        requiredFiles = listOf(
            RequiredFile(
                name = "labels",
                url = "https://storage.googleapis.com/download.tensorflow.org/models/tflite/coco_ssd_mobilenet_v1_1.0_labels.txt",
                size = 999L
            )
        ),
        minRamMb = 1024,
        maxResults = 10,
        scoreThreshold = 0.35f
    )
    
    /**
     * Lista de todos os modelos recomendados
     */
    val RECOMMENDED_MODELS = listOf(
        EFFICIENTDET_LITE0,
        EFFICIENTDET_LITE2,
        MOBILENET_V2,
        SSD_MOBILENET_V1
    )
    
    /**
     * Lista de modelos leves (< 2GB RAM)
     */
    val LIGHTWEIGHT_MODELS = RECOMMENDED_MODELS.filter { it.minRamMb < 2048 }
    
    /**
     * Modelo padrão recomendado
     */
    val DEFAULT_MODEL = EFFICIENTDET_LITE0
    
    /**
     * Busca modelo por ID
     */
    fun getModelById(id: String): VisionModel? {
        return RECOMMENDED_MODELS.find { it.id == id }
    }
    
    /**
     * Recomenda modelo baseado na RAM disponível
     */
    fun recommendModelForDevice(availableRamMb: Int): VisionModel {
        return when {
            availableRamMb < 2048 -> EFFICIENTDET_LITE0
            availableRamMb < 4096 -> MOBILENET_V2
            else -> EFFICIENTDET_LITE2
        }
    }
}
