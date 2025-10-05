#include "llama_bridge.h"
#include <android/log.h>
#include <fstream>
#include <sstream>
#include <cstring>
#include <vector>

#define LOG_TAG "LlamaBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct LlamaBridge::Impl {
    std::string modelPath;
    std::string mmProjPath;
    
    bool validateModel(const std::string& path) {
        std::ifstream file(path, std::ios::binary);
        if (!file.is_open()) {
            LOGE("Cannot open file: %s", path.c_str());
            return false;
        }
        
        char magic[4];
        file.read(magic, 4);
        
        if (std::strncmp(magic, "GGUF", 4) == 0) {
            LOGI("Valid GGUF model detected");
            return true;
        } else if (std::strncmp(magic, "GGJT", 4) == 0) {
            LOGI("Valid GGJT model detected (old format)");
            return true;
        }
        
        LOGE("Invalid model format");
        return false;
    }
    
    std::string processPrompt(const std::string& prompt, const std::string& imagePath) {
        LOGI("[MULTIMODAL] Processing prompt with image: %s", imagePath.c_str());
        LOGI("[MULTIMODAL] Full prompt: %s", prompt.c_str());
        
        // NOTA: Este é um processador híbrido que usa análise de texto + contexto visual
        // Para modelos multimodais completos (LLaVA, MiniCPM-V, etc.), a imagem seria
        // processada por um encoder de visão (CLIP) e fusionada com embeddings de texto.
        // Atualmente estamos usando regras + validação GGUF para demonstração.
        
        std::ostringstream response;
        
        // Extrair tarefa do prompt multimodal
        if (prompt.find("Tarefa:") != std::string::npos) {
            size_t taskStart = prompt.find("Tarefa:") + 8;
            size_t taskEnd = prompt.find("\n", taskStart);
            std::string task = prompt.substr(taskStart, taskEnd - taskStart);
            
            LOGI("Extracted task: %s", task.c_str());
            
            if (task.find("abrir") != std::string::npos || task.find("Abrir") != std::string::npos) {
                if (task.find("Chrome") != std::string::npos || task.find("chrome") != std::string::npos) {
                    response << R"({
  "action": "OPEN_APP",
  "target": "Chrome",
  "reasoning": "Abrindo o aplicativo Chrome conforme solicitado"
})";
                } else if (task.find("WhatsApp") != std::string::npos) {
                    response << R"({
  "action": "OPEN_APP",
  "target": "WhatsApp",
  "reasoning": "Abrindo o aplicativo WhatsApp conforme solicitado"
})";
                } else if (task.find("configurações") != std::string::npos || task.find("Configurações") != std::string::npos) {
                    response << R"({
  "action": "OPEN_APP",
  "target": "Settings",
  "reasoning": "Abrindo as configurações do sistema"
})";
                } else {
                    response << R"({
  "action": "HOME",
  "reasoning": "Voltando para a tela inicial para localizar o aplicativo"
})";
                }
            } else if (task.find("pesquisar") != std::string::npos || task.find("buscar") != std::string::npos) {
                size_t searchStart = task.find("por") + 4;
                if (searchStart != std::string::npos && searchStart < task.length()) {
                    std::string searchTerm = task.substr(searchStart);
                    response << R"({
  "action": "TYPE_TEXT",
  "text": ")" << searchTerm << R"(",
  "reasoning": "Digitando o termo de busca solicitado"
})";
                } else {
                    response << R"({
  "action": "CLICK",
  "x": 540,
  "y": 200,
  "reasoning": "Clicando na barra de pesquisa"
})";
                }
            } else if (task.find("rolar") != std::string::npos || task.find("scroll") != std::string::npos) {
                bool scrollDown = task.find("baixo") != std::string::npos || task.find("down") != std::string::npos;
                response << R"({
  "action": "SCROLL",
  "direction": ")" << (scrollDown ? "DOWN" : "UP") << R"(",
  "amount": 500,
  "reasoning": "Rolando a tela para ver mais conteúdo"
})";
            } else if (task.find("voltar") != std::string::npos || task.find("back") != std::string::npos) {
                response << R"({
  "action": "BACK",
  "reasoning": "Voltando para a tela anterior"
})";
            } else if (task.find("foto") != std::string::npos || task.find("câmera") != std::string::npos) {
                response << R"({
  "action": "OPEN_APP",
  "target": "Camera",
  "reasoning": "Abrindo o aplicativo da câmera"
})";
            } else {
                response << R"({
  "action": "CLICK",
  "x": 540,
  "y": 960,
  "reasoning": "Clicando no elemento central identificado na tela"
})";
            }
        } else {
            response << R"({
  "action": "WAIT",
  "reasoning": "Tarefa concluída - aguardando próxima instrução"
})";
        }
        
        return response.str();
    }
};

LlamaBridge::LlamaBridge() : impl_(std::make_unique<Impl>()), loaded_(false) {
    LOGI("LlamaBridge initialized");
}

LlamaBridge::~LlamaBridge() {
    unloadModel();
    LOGI("LlamaBridge destroyed");
}

bool LlamaBridge::loadModel(const std::string& modelPath, const std::string& mmProjPath) {
    LOGI("Loading model: %s", modelPath.c_str());
    LOGI("Loading mmproj: %s", mmProjPath.c_str());
    
    if (!impl_->validateModel(modelPath)) {
        LOGE("Model validation failed");
        return false;
    }
    
    if (!impl_->validateModel(mmProjPath)) {
        LOGE("MMProj validation failed");
        return false;
    }
    
    impl_->modelPath = modelPath;
    impl_->mmProjPath = mmProjPath;
    loaded_ = true;
    
    LOGI("Model loaded successfully (inference stub)");
    LOGI("NOTE: This is a stub implementation. For real inference, integrate llama.cpp");
    
    return true;
}

void LlamaBridge::unloadModel() {
    if (loaded_) {
        LOGI("Unloading model");
        impl_->modelPath.clear();
        impl_->mmProjPath.clear();
        loaded_ = false;
    }
}

bool LlamaBridge::isLoaded() const {
    return loaded_;
}

std::string LlamaBridge::generateResponse(
    const std::string& imagePath,
    const std::string& prompt,
    float temperature,
    int maxTokens
) {
    if (!loaded_) {
        LOGE("Model not loaded");
        return "";
    }
    
    LOGI("Generating response");
    LOGI("Image: %s", imagePath.c_str());
    LOGI("Temperature: %.2f, MaxTokens: %d", temperature, maxTokens);
    
    std::string response = impl_->processPrompt(prompt, imagePath);
    
    LOGI("Response generated: %s", response.c_str());
    
    return response;
}
