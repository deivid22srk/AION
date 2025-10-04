#ifndef LLAMA_BRIDGE_H
#define LLAMA_BRIDGE_H

#include <string>
#include <memory>

class LlamaBridge {
public:
    LlamaBridge();
    ~LlamaBridge();
    
    bool loadModel(const std::string& modelPath, const std::string& mmProjPath);
    void unloadModel();
    bool isLoaded() const;
    
    std::string generateResponse(
        const std::string& imagePath,
        const std::string& prompt,
        float temperature,
        int maxTokens
    );
    
private:
    struct Impl;
    std::unique_ptr<Impl> impl_;
    bool loaded_;
};

#endif // LLAMA_BRIDGE_H
