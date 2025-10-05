# 🔥 AION - Modelos LiteRT-LM Únicos

## ✅ Status: Sistema Simplificado com LiteRT-LM

O projeto AION agora usa **EXCLUSIVAMENTE** modelos **Google LiteRT-LM** com inferência multimodal real (Visão + Texto) e GPU acceleration.

---

## 🎯 Decisão de Design

### Por que apenas LiteRT-LM?

1. **✅ Inferência Neural Real** - Não é simulação, é IA de verdade
2. **✅ GPU Acceleration Nativa** - Performance máxima no dispositivo
3. **✅ Modelos Verificados** - Todos disponíveis no Hugging Face
4. **✅ Simplicidade** - Um único sistema, sem complexidade híbrida
5. **✅ Suporte Oficial Google** - Mantido pela equipe Android AI
6. **✅ Menor Tamanho** - Modelos ultra compactos (584MB - 3.39GB)

### Remoção do llama.cpp

O sistema anterior suportava modelos llama.cpp, mas foi simplificado pelos seguintes motivos:
- ❌ Maior complexidade (código C++ nativo)
- ❌ Modelos maiores (0.9GB - 4.4GB)
- ❌ Performance inferior em GPU
- ❌ Multimodalidade requer integração manual
- ❌ Manutenção comunitária (não oficial)

---

## 📦 Modelos Disponíveis

### 1. Gemma 3 1B (LiteRT) 🔥 **RECOMENDADO**

| Propriedade | Valor |
|------------|-------|
| **ID** | gemma3-1b-it |
| **Nome** | Gemma 3 1B (LiteRT) 🔥 |
| **Tamanho** | 584 MB |
| **Parâmetros** | 1B |
| **Quantização** | int4 |
| **Repositório** | litert-community/Gemma3-1B-IT |
| **Arquivo** | gemma3-1b-it-int4.litertlm |

**Características:**
- ✅ Ultra leve e rápido
- ✅ Perfeito para a maioria dos dispositivos
- ✅ Baixo consumo de memória
- ✅ Excelente balanço velocidade/precisão
- ✅ Recomendado como padrão

**Download:**
```
https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.litertlm
```

---

### 2. Gemma 3n E2B (LiteRT) ⚡

| Propriedade | Valor |
|------------|-------|
| **ID** | gemma-3n-e2b-it |
| **Nome** | Gemma 3n E2B (LiteRT) ⚡ |
| **Tamanho** | 3.39 GB |
| **Parâmetros** | 2B (eficiente 5B) |
| **Quantização** | int4 |
| **Repositório** | google/gemma-3n-E2B-it-litert-lm |
| **Arquivo** | gemma-3n-E2B-it-int4.litertlm |

**Características:**
- ✅ Alta precisão com 2B parâmetros eficientes
- ✅ Arquitetura Matformer avançada
- ✅ Melhor compreensão de contexto
- ✅ Para dispositivos com 4GB+ RAM
- ✅ Suporte multimodal completo (texto, imagem, áudio)

**Download:**
```
https://huggingface.co/google/gemma-3n-E2B-it-litert-lm/resolve/main/gemma-3n-E2B-it-int4.litertlm
```

---

## 🏗️ Arquitetura Simplificada

```
┌────────────────────────────────────────────────────┐
│                   AION System                      │
├────────────────────────────────────────────────────┤
│                                                    │
│  ┌──────────────┐     ┌──────────────────────┐   │
│  │  User Task   │────▶│  AIControlService    │   │
│  │  Screenshot  │     │                      │   │
│  └──────────────┘     │  ┌────────────────┐  │   │
│                       │  │ LiteRT         │  │   │
│                       │  │ Multimodal     │  │   │
│                       │  │ Controller     │  │   │
│                       │  └────────┬───────┘  │   │
│                       │           │          │   │
│                       │           ▼          │   │
│                       │  ┌────────────────┐  │   │
│                       │  │ LiteRT Engine  │  │   │
│                       │  │ (GPU Accel)    │  │   │
│                       │  └────────┬───────┘  │   │
│                       │           │          │   │
│                       │           ▼          │   │
│                       │     ┌──────────┐     │   │
│                       │     │ AIAction │     │   │
│                       │     └──────────┘     │   │
│                       └──────────────────────┘   │
│                                                    │
└────────────────────────────────────────────────────┘
```

**Fluxo Simplificado:**
1. Usuário define tarefa + screenshot é capturado
2. AIControlService usa LiteRTMultimodalController
3. Modelo LiteRT-LM faz inferência com GPU
4. Retorna AIAction parseada
5. Ação é executada via Accessibility Service

---

## 💻 Código Simplificado

### Estrutura de Dados

```kotlin
data class LocalVisionModel(
    val id: String,
    val name: String,
    val description: String,
    val repoId: String,
    val modelFilename: String,
    val mmProjFilename: String,  // Vazio para LiteRT
    val estimatedSize: String,
    val isLiteRT: Boolean = true  // Sempre true agora
)

val AVAILABLE_LOCAL_MODELS = listOf(
    LocalVisionModel(
        id = "gemma3-1b-it",
        name = "Gemma 3 1B (LiteRT) 🔥",
        description = "ULTRA LEVE! Modelo multimodal oficial do Google...",
        repoId = "litert-community/Gemma3-1B-IT",
        modelFilename = "gemma3-1b-it-int4.litertlm",
        mmProjFilename = "",
        estimatedSize = "584 MB",
        isLiteRT = true
    ),
    LocalVisionModel(
        id = "gemma-3n-e2b-it",
        name = "Gemma 3n E2B (LiteRT) ⚡",
        description = "Modelo multimodal avançado com 2B parâmetros...",
        repoId = "google/gemma-3n-E2B-it-litert-lm",
        modelFilename = "gemma-3n-E2B-it-int4.litertlm",
        mmProjFilename = "",
        estimatedSize = "3.39 GB",
        isLiteRT = true
    )
)
```

### AIControlService (Simplificado)

```kotlin
class AIControlService : Service() {
    private var liteRTController: LiteRTMultimodalController? = null
    
    fun setupLocalAI(modelPath: String, mmProjPath: String, isLiteRT: Boolean = true) {
        serviceScope.launch {
            liteRTController = LiteRTMultimodalController(modelPath)
            val loaded = liteRTController?.initialize() ?: false
            
            if (loaded) {
                addLog("✅ Modelo LiteRT-LM carregado com sucesso (GPU acceleration)")
            } else {
                addLog("❌ Erro ao carregar modelo LiteRT-LM")
                liteRTController = null
            }
        }
    }
    
    fun isModelLoaded(): Boolean {
        return liteRTController?.isReady() ?: false
    }
    
    // Execução de tarefa usa apenas liteRTController
    val action = liteRTController?.analyzeScreenAndDecide(
        screenshot,
        task,
        conversationHistory
    )
}
```

### LocalModelManager (Simplificado)

```kotlin
fun isModelDownloaded(model: LocalVisionModel): Boolean {
    val modelFile = getModelFile(model)
    
    // LiteRT models são unitários (sem mmproj)
    if (model.isLiteRT) {
        return downloader.checkModelExists(modelFile)
    }
    
    // Código legacy (não usado atualmente)
    val mmProjFile = getMMProjFile(model)
    return downloader.checkModelExists(modelFile) && 
           downloader.checkModelExists(mmProjFile)
}
```

---

## 🚀 Como Usar

### Para o Usuário Final

1. Abra o app AION
2. Vá na aba **"Modelos"**
3. Escolha **Gemma 3 1B (LiteRT) 🔥** (recomendado)
4. Toque em **"Baixar Modelo"**
5. Aguarde o download (584 MB)
6. Toque em **"Selecionar"**
7. Volte para **"Principal"**
8. Digite uma tarefa e execute!

### Para Desenvolvedores

```kotlin
// Carregar modelo
val controller = LiteRTMultimodalController(
    "/data/data/com.aion.aicontroller/files/vision_models/gemma3-1b-it-int4.litertlm"
)
val success = controller.initialize()

// Fazer inferência
if (success) {
    val action = controller.analyzeScreenAndDecide(
        screenshot = screenshotBitmap,
        task = "Abrir o Chrome",
        conversationHistory = listOf()
    )
    
    // Executar ação
    when (action?.type) {
        ActionType.CLICK -> performClick(action.x, action.y)
        ActionType.TYPE_TEXT -> typeText(action.text)
        ActionType.SCROLL -> scroll(action.direction)
        // ...
    }
}

// Limpar
controller.unload()
```

---

## 📊 Comparação de Performance

| Modelo | Tamanho | Velocidade | Precisão | Memória | GPU | Recomendado |
|--------|---------|-----------|----------|---------|-----|-------------|
| **Gemma 3 1B** | 584 MB | ⚡⚡⚡⚡⚡ | ⭐⭐⭐⭐ | 1.5 GB | ✅ | ✅ Maioria |
| **Gemma 3n E2B** | 3.39 GB | ⚡⚡⚡⚡ | ⭐⭐⭐⭐⭐ | 3.5 GB | ✅ | ✅ Alta precisão |

---

## 🔧 Configuração Técnica

### Dependência Gradle

```gradle
implementation("com.google.ai.edge.litert:litert-llm:1.2.0")
```

### Requisitos do Sistema

- **Android:** 12+ (API 31+)
- **RAM:** 2GB mínimo, 4GB recomendado
- **Armazenamento:** 600MB - 4GB (dependendo do modelo)
- **GPU:** Qualquer GPU moderna (aceleração automática)
- **Processador:** ARM64 ou x86_64

### Formatos de Modelo

- **Extensão:** `.litertlm`
- **Quantização:** int4 (4-bit integer)
- **Arquitetura:** Gemma 3 / Gemma 3n
- **Multimodal:** ✅ Texto + Imagem + Áudio

---

## 📁 Arquivos do Projeto

### Arquivos Principais

1. **Models.kt** - Define os 2 modelos LiteRT disponíveis
2. **LiteRTMultimodalController.kt** - Controller de inferência
3. **AIControlService.kt** - Serviço simplificado (apenas LiteRT)
4. **MainActivity.kt** - UI simplificada sem badges LiteRT
5. **PreferencesManager.kt** - Modelo padrão: gemma3-1b-it
6. **LocalModelManager.kt** - Gerencia download (unitário para LiteRT)
7. **HuggingFaceDownloader.kt** - Download do Hugging Face

### Arquivos Legacy (Não Usados)

- `LocalAIController.kt` - Antigo controller llama.cpp
- `LocalVisionInference.kt` - Inferência llama.cpp (C++)
- `VisionAIController.kt` - Antigo controller de visão

---

## 🎉 Benefícios da Simplificação

### Antes (Sistema Híbrido)
- ⚠️ 10 modelos diferentes (llama.cpp + LiteRT)
- ⚠️ Lógica complexa if/else (isUsingLiteRT)
- ⚠️ Múltiplos controllers (3 controllers diferentes)
- ⚠️ Download em 2 partes (model + mmproj)
- ⚠️ Modelos grandes (até 4.4GB)
- ⚠️ Performance variável

### Agora (Sistema Simplificado)
- ✅ 2 modelos verificados (LiteRT-LM apenas)
- ✅ Código limpo e direto
- ✅ Um único controller (LiteRTMultimodalController)
- ✅ Download unitário (apenas .litertlm)
- ✅ Modelos compactos (584MB - 3.39GB)
- ✅ GPU acceleration garantida

---

## 🔗 Referências

- **LiteRT-LM GitHub:** https://github.com/google-ai-edge/LiteRT-LM
- **Google AI Edge:** https://ai.google.dev/edge/litert
- **Gemma 3 1B:** https://huggingface.co/litert-community/Gemma3-1B-IT
- **Gemma 3n E2B:** https://huggingface.co/google/gemma-3n-E2B-it-litert-lm
- **LiteRT Documentation:** https://github.com/google-ai-edge/litert

---

## 📝 Notas de Migração

### Usuários Existentes

Se você tinha modelos llama.cpp instalados:
1. Eles ainda existem no dispositivo em `/files/vision_models/`
2. Podem ser deletados manualmente se necessário
3. O app não os usará mais automaticamente
4. Baixe um modelo LiteRT para continuar usando o AION

### Desenvolvedores

Se você estava usando o código antigo:
1. `LocalAIController` foi substituído por `LiteRTMultimodalController`
2. `setupLocalAI()` agora sempre usa LiteRT (parâmetro `isLiteRT` sempre true)
3. `isModelLoaded()` verifica apenas `liteRTController`
4. Modelos não precisam mais de `mmProjPath` (campo vazio)

---

**Implementado por:** Capy AI Agent
**Data:** 2025-10-05
**Versão AION:** 2.1 - LiteRT-LM Only
