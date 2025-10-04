# Integração com llama.cpp para Visão

## Status Atual

A implementação atual usa **funções stub** (mockadas) para as funções de visão. Para ter inferência real de modelos LLaVA, você precisa integrar a biblioteca llama.cpp completa.

## Como Integrar llama.cpp Real

### 1. Clone o llama.cpp com suporte a visão

```bash
cd app/src/main/cpp/
git clone https://github.com/ggerganov/llama.cpp.git
cd llama.cpp
git checkout master  # ou uma tag estável
```

### 2. Atualize o CMakeLists.txt

Adicione o llama.cpp ao seu `CMakeLists.txt`:

```cmake
# Adicione o subdirectory do llama.cpp
add_subdirectory(llama.cpp)

# Link contra as bibliotecas do llama.cpp
target_link_libraries(${CMAKE_PROJECT_NAME}
    ${log-lib}
    ${jnigraphics-lib}
    ${android-lib}
    llama
    llava
)
```

### 3. Atualize native-lib.cpp

Substitua as funções stub por implementações reais usando a API do llama.cpp:

```cpp
#include "llama.cpp/llama.h"
#include "llama.cpp/examples/llava/llava.h"

// Variáveis globais para contexto
static llama_model* g_model = nullptr;
static llama_context* g_ctx = nullptr;
static clip_ctx* g_clip_ctx = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_aion_aicontroller_NativeLib_loadVisionModel(...) {
    // Parâmetros do modelo
    llama_model_params model_params = llama_model_default_params();
    
    // Carregar modelo
    g_model = llama_load_model_from_file(modelPathStr, model_params);
    if (!g_model) {
        LOGE("Failed to load model");
        return JNI_FALSE;
    }
    
    // Criar contexto
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 4096;
    g_ctx = llama_new_context_with_model(g_model, ctx_params);
    
    // Carregar CLIP para processamento de imagens
    g_clip_ctx = clip_model_load(mmProjPathStr, 1);
    
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_aion_aicontroller_NativeLib_generateVisionResponse(...) {
    // Processar imagem com CLIP
    llava_image_embed* image_embed = llava_image_embed_make_with_filename(
        g_clip_ctx, 
        n_threads, 
        imagePathStr
    );
    
    // Preparar prompt
    std::string full_prompt = std::string(promptStr);
    
    // Tokenizar
    std::vector<llama_token> tokens = llama_tokenize(g_ctx, full_prompt, true);
    
    // Gerar resposta
    std::string response;
    // ... lógica de geração com llama_decode, llama_sample, etc.
    
    // Liberar recursos
    llava_image_embed_free(image_embed);
    
    return env->NewStringUTF(response.c_str());
}
```

### 4. Modelos Compatíveis

Os seguintes modelos GGUF são compatíveis com llama.cpp + LLaVA:

- **LLaVA 1.5** - Melhor suporte, mais estável
- **LLaVA 1.6** - Versão mais nova com melhorias
- **BakLLaVA** - Otimizado para tarefas específicas
- **LLaVA Phi-3** - Versão compacta

### 5. Compilação para Android

Certifique-se de que o llama.cpp compile para Android:

```cmake
# No CMakeLists.txt do llama.cpp, configure para Android
set(CMAKE_SYSTEM_NAME Android)
set(CMAKE_ANDROID_ARCH_ABI arm64-v8a)  # ou armeabi-v7a
```

### 6. Otimizações

Para melhor performance em Android:

- Use quantização **Q4_K_M** ou **Q4_K_S** para modelos menores
- Habilite **NEON** para ARM: `-DLLAMA_ARM_NEON=ON`
- Configure threads apropriadamente (4-8 threads para mobile)
- Limite o contexto: `n_ctx = 2048` ou `4096`

### 7. Testando

Após integrar, teste com:

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Recursos

- [llama.cpp GitHub](https://github.com/ggerganov/llama.cpp)
- [LLaVA Example](https://github.com/ggerganov/llama.cpp/tree/master/examples/llava)
- [Android Build Guide](https://github.com/ggerganov/llama.cpp/discussions/categories/android)

## Nota

A implementação atual com stubs permite que o app compile e teste a UI/fluxo, mas não gera respostas reais dos modelos. Para uso em produção, a integração completa do llama.cpp é **obrigatória**.
