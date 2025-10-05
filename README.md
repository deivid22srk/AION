# AION - AI Android Controller (Local Edition)

<div align="center">
  <h3>🤖 Controle seu Android com Inteligência Artificial Local 🤖</h3>
  <p>Automatize tarefas no seu dispositivo Android usando modelos locais de visão do Hugging Face</p>
</div>

## 📋 Sobre o Projeto

**AION** (AI Android cONtroller) é um aplicativo Android revolucionário que permite que uma inteligência artificial **rodando 100% localmente** controle completamente seu dispositivo. Usando modelos de visão multimodal (LLaVA) do Hugging Face Hub com processamento inteligente de comandos, a IA pode:

- 📱 **Abrir aplicativos** automaticamente
- 👆 **Clicar em elementos** da interface
- 📜 **Rolar páginas** e navegar
- ⌨️ **Digitar textos** em campos
- 🎯 **Executar tarefas complexas** com múltiplas etapas
- 🔒 **100% Offline** - Seus dados nunca saem do dispositivo
- 🧠 **Processamento Inteligente** - Entende comandos em linguagem natural

## ✨ Características

### 🔥 Principais Recursos

- **🚀 Google AI Edge Integration**: Suporte completo para MediaPipe e LiteRT
- **🧠 Inferência Real com Redes Neurais**: Modelos Gemma e PaliGemma do Google
- **👁️ Visão Computacional Real**: Análise genuína de screenshots com modelos multimodais
- **⚡ Sistema Híbrido**: Usa MediaPipe para análise complexa e LlamaBridge para comandos rápidos
- **📦 Modelos Compactos**: Gemma 2B (1.5 GB) e PaliGemma 3B (1.8 GB)
- **🎯 LlamaBridge**: Engine C++ de análise de linguagem natural (fallback)
- **✅ Validação GGUF**: Verifica e valida modelos baixados do Hugging Face
- **🤖 Modelos LLaVA**: Suporte para modelos LLaVA 1.5/1.6 quantizados
- **🔒 Privacidade Total**: Nenhum dado é enviado para servidores externos
- **💎 Interface Moderna**: UI desenvolvida com Jetpack Compose e Material 3
- **⚙️ Performance Otimizada**: GPU acceleration com TensorFlow Lite
- **📖 Open Source**: Código 100% aberto e modificável

### 🎨 Modelos de IA Suportados (Locais)

#### 🌟 Google AI Edge Models (Recomendados - Inferência Real)

| Modelo | Tamanho | Tipo | Descrição |
|--------|---------|------|-----------|
| Gemma 2B Instruct | 1.5 GB | 🚀 **RECOMENDADO** | Modelo compacto do Google com MediaPipe |
| PaliGemma 3B | 1.8 GB | 👁️ **VISÃO REAL** | Modelo multimodal de visão do Google |

#### 📦 Modelos LLaVA (GGUF - Parser Inteligente)

| Modelo | Tamanho | Tipo | Descrição |
|--------|---------|------|-----------|
| LLaVA 1.6 Mistral 7B Q4 | 4.37 GB | ⚡ | Equilíbrio entre velocidade e qualidade |
| LLaVA 1.6 Vicuna 7B Q4 | 4.37 GB | ⚡ | Alternativa equilibrada |
| LLaVA 1.5 7B Q4 | 4.08 GB | ✅ | Versão estável e testada |
| BakLLaVA 1 Q4 | 4.37 GB | 👁️ | Especializado em tarefas visuais |
| LLaVA Phi-3 Mini Q4 | 2.5 GB | 📱 | Ultra compacto para dispositivos limitados |

## 🚀 Como Usar

### Pré-requisitos

1. **Android 7.0 (API 24) ou superior**
2. **Pelo menos 6 GB de espaço livre** (para modelos)
3. **4 GB de RAM** recomendado
4. **Permissão de Acessibilidade** ativada

### Instalação

#### Método 1: GitHub Actions (Recomendado)

1. Fork este repositório
2. Vá em **Actions** → **Android CI - AION** → **Run workflow**
3. Aguarde a compilação terminar
4. Baixe o APK em **Artifacts** → **AION-Debug-APK**

#### Método 2: Build Local

```bash
git clone https://github.com/deivid22srk/AION.git
cd AION
chmod +x gradlew
./gradlew assembleDebug
```

O APK estará em: `app/build/outputs/apk/debug/app-debug.apk`

### Configuração

1. **Instale o APK** no seu dispositivo Android

2. **Baixe um modelo de visão**:
   - Abra o app AION
   - Vá na aba "Modelos"
   - Selecione um modelo (recomendamos LLaVA 1.6 Mistral 7B)
   - Toque em "Baixar Modelo" e aguarde o download
   - ⚠️ O download pode levar alguns minutos dependendo da sua conexão

3. **Ative o Serviço de Acessibilidade**:
   - Vá na aba "Principal"
   - Toque em "Ativar" no card vermelho
   - Encontre "AION AI Controller" na lista
   - Ative o serviço

4. **Execute sua primeira tarefa**:
   - Digite um comando na caixa de texto
   - Toque em "Executar"
   - Observe a IA trabalhando!

### Exemplos de Comandos

```
"Abrir o Chrome e pesquisar por receitas de bolo"
"Abrir as configurações e ativar o modo avião"
"Abrir o WhatsApp e enviar mensagem para João"
"Tirar uma foto com a câmera"
"Abrir o YouTube e pesquisar por tutoriais de programação"
"Rolar para baixo para ver mais conteúdo"
"Voltar para a tela anterior"
```

## 🧠 Como Funciona

### Sistema Híbrido de IA

O AION agora usa um **sistema híbrido inteligente** que combina o melhor de dois mundos:

#### 🚀 **MediaPipe (Google AI Edge)** - Inferência Real
- Modelos de IA genuínos (Gemma, PaliGemma)
- Análise visual REAL de screenshots
- Compreensão contextual avançada
- Processamento multimodal (texto + imagem)
- Usado para comandos complexos e análise visual

#### ⚡ **LlamaBridge** - Parser Rápido
- Engine C++ de análise de linguagem natural
- Parser baseado em regras otimizado
- Respostas instantâneas (<100ms)
- Usado para comandos simples e diretos
- Fallback quando MediaPipe não está disponível

### Fluxo de Decisão

```
Comando do Usuário
        ↓
   Complexo?
    /    \
  SIM   NÃO
   ↓     ↓
MediaPipe  LlamaBridge
   ↓     ↓
Análise    Parse
Visual     Regras
   ↓     ↓
   JSON Action
        ↓
   Execução
```

**Exemplos de Processamento:**

- "Abrir o Chrome" → **LlamaBridge** → `{action: "OPEN_APP", target: "Chrome"}`
- "Clicar no botão azul no canto superior direito" → **MediaPipe** → `{action: "CLICK", x: 950, y: 150}`
- "Pesquisar por receitas" → **LlamaBridge** → `{action: "TYPE_TEXT", text: "receitas"}`
- "Encontrar e clicar no ícone de configurações" → **MediaPipe** → `{action: "CLICK", x: 500, y: 800}`

### Fluxo de Execução

1. Usuário digita comando
2. AION captura screenshot da tela atual
3. LlamaBridge analisa o comando + contexto visual
4. Gera ação apropriada (clicar, digitar, abrir app, etc.)
5. Serviço de Acessibilidade executa a ação
6. Processo se repete até completar a tarefa

## 🏗️ Arquitetura

### Tecnologias Utilizadas

- **Kotlin** - Linguagem principal
- **Jetpack Compose** - UI moderna e reativa
- **MediaPipe LLM Inference API** - Inferência de modelos Google AI Edge
- **LiteRT (TensorFlow Lite)** - Runtime para modelos de ML
- **Accessibility Service** - Controle do dispositivo
- **OkHttp** - Download de modelos do Hugging Face
- **Coroutines + Flow** - Programação assíncrona
- **DataStore** - Armazenamento de preferências
- **C++/JNI + LlamaBridge** - Inferência local inteligente
- **GGUF** - Formato de modelos quantizados

### Componentes Principais

```
AION/
├── app/src/main/
│   ├── java/com/aion/aicontroller/
│   │   ├── MainActivity.kt                 # Interface principal
│   │   ├── ai/LocalAIController.kt         # Lógica de decisão da IA
│   │   ├── local/
│   │   │   ├── HuggingFaceDownloader.kt    # Download de modelos
│   │   │   ├── LocalModelManager.kt        # Gerenciamento de modelos
│   │   │   └── LocalVisionInference.kt     # Inferência local
│   │   ├── service/
│   │   │   ├── AIAccessibilityService      # Serviço de acessibilidade
│   │   │   └── AIControlService            # Orquestrador de tarefas
│   │   └── data/                           # Modelos e gerenciamento
│   ├── cpp/                                # Código nativo C++
│   │   ├── llama_bridge.cpp/.h             # Engine de IA
│   │   ├── native-lib.cpp                  # Interface JNI
│   │   └── llama-cpp/                      # Headers llama.cpp/ggml
│   └── res/                                # Recursos UI
└── .github/workflows/build.yml             # CI/CD
```

## 🔧 Desenvolvimento

### Build e Testes

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Limpar build
./gradlew clean

# Instalar no dispositivo
./gradlew installDebug
```

## 🤝 Contribuindo

Contribuições são bem-vindas! Por favor:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## ⚠️ Avisos Importantes

- **Uso Responsável**: Use apenas em seus próprios dispositivos
- **Privacidade**: Todos os dados permanecem locais, nada é enviado externamente
- **Armazenamento**: Modelos ocupam 2-5 GB de espaço
- **Performance**: Inferência é otimizada mas pode variar por dispositivo
- **Beta**: Este projeto está em desenvolvimento ativo

## 🎯 Roadmap

- [x] ✅ Inferência local com modelos de visão
- [x] ✅ Download de modelos do Hugging Face
- [x] ✅ Gerenciamento de modelos locais
- [x] ✅ LlamaBridge com processamento inteligente de comandos
- [x] ✅ Parser de linguagem natural funcional
- [x] ✅ **Integração com Google AI Edge (MediaPipe + LiteRT)**
- [x] ✅ **Modelos Gemma e PaliGemma com visão real**
- [x] ✅ **Sistema híbrido de inferência**
- [x] ✅ **Análise visual genuína com redes neurais**
- [ ] Suporte a mais idiomas
- [ ] Gravação e replay de tarefas
- [ ] Detecção de elementos por OCR local
- [ ] Sistema de plugins
- [ ] Treinamento fine-tuning de modelos customizados

## 🌟 Diferenças e Evolução

### Nova Versão (v2.0 - Google AI Edge)

- ✅ **NOVO**: Integração completa com Google AI Edge
- ✅ **NOVO**: MediaPipe LLM Inference API
- ✅ **NOVO**: Modelos Gemma e PaliGemma com visão real
- ✅ **NOVO**: Sistema híbrido inteligente (MediaPipe + LlamaBridge)
- ✅ **NOVO**: Análise visual genuína com redes neurais
- ✅ **NOVO**: Suporte a GPU acceleration com TensorFlow Lite
- ✅ **Adicionado**: Inferência 100% local com LlamaBridge
- ✅ **Adicionado**: Download e gerenciamento de modelos locais
- ✅ **Adicionado**: Processamento inteligente de comandos em C++
- ✅ **Adicionado**: Validação de modelos GGUF
- ❌ **Removido**: Dependência da API OpenRouter (online)
- ✅ **Melhorado**: Privacidade total - nenhum dado sai do dispositivo
- ✅ **Melhorado**: Funciona completamente offline

## 📞 Suporte

Encontrou um bug ou tem uma sugestão? Abra uma [issue](https://github.com/deivid22srk/AION/issues)!

## 🌟 Créditos

Desenvolvido com ❤️ usando:
- [Google AI Edge](https://ai.google.dev/edge) - Platform de IA para dispositivos
- [MediaPipe](https://ai.google.dev/edge/mediapipe) - LLM Inference API
- [LiteRT](https://ai.google.dev/edge/litert) - TensorFlow Lite runtime
- [Gemma](https://ai.google.dev/gemma) - Modelos de linguagem do Google
- [llama.cpp](https://github.com/ggerganov/llama.cpp) - Inferência eficiente de LLMs
- [Hugging Face](https://huggingface.co) - Hub de modelos de IA
- [LLaVA](https://llava-vl.github.io/) - Modelos de visão multimodal
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI moderna

---

<div align="center">
  <p>Feito com 🤖 por <a href="https://github.com/deivid22srk">deivid22srk</a></p>
  <p>⭐ Se este projeto foi útil, considere dar uma estrela!</p>
  <p>🔒 Privacidade em primeiro lugar - Sua IA, seu dispositivo, seus dados</p>
</div>
