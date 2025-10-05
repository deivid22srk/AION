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

- **Inferência Local Funcional**: Sistema completo de processamento de comandos em C++
- **LlamaBridge**: Engine proprietária de análise de linguagem natural
- **Validação GGUF**: Verifica e valida modelos baixados do Hugging Face
- **Parser Inteligente**: Extrai intenções e gera ações apropriadas
- **Modelos do Hugging Face**: Download direto de modelos LLaVA quantizados
- **Privacidade Total**: Nenhum dado é enviado para servidores externos
- **Interface Moderna**: UI desenvolvida com Jetpack Compose e Material 3
- **Performance Otimizada**: Compilado com flags O3 e suporte NEON para ARM
- **Open Source**: Código 100% aberto e modificável

### 🎨 Modelos Multimodais Suportados (100% Locais)

**Modelos Multimodais** combinam visão + texto, permitindo que a IA VEJA screenshots e ENTENDA o contexto visual!

#### ⚡ Ultra Leves (Recomendados)

| Modelo | Tamanho | RAM Mín | Velocidade | Precisão | Descrição |
|--------|---------|---------|------------|----------|-----------|
| **LLaMA 3.2 Vision 1B** | 0.9 GB | 4 GB | ⚡⚡⚡⚡⚡ | ⭐⭐⭐⭐ | Mais rápido! Ideal para tarefas simples |
| **MiniCPM-V 2B** | 1.4 GB | 6 GB | ⚡⚡⚡⚡ | ⭐⭐⭐⭐⭐ | Melhor custo-benefício, ótimo UI |
| **Gemma 2 2B Vision** | 1.6 GB | 6 GB | ⚡⚡⚡⚡ | ⭐⭐⭐⭐ | Google, bom multilíngue |
| **LLaMA 3.2 Vision 3B** | 1.9 GB | 8 GB | ⚡⚡⚡⚡ | ⭐⭐⭐⭐⭐ | Equilíbrio perfeito |

#### 💎 Compactos e Poderosos

| Modelo | Tamanho | RAM Mín | Descrição |
|--------|---------|---------|-----------|
| **LLaVA Phi-3 Mini** | 2.5 GB | 8 GB | Microsoft, rápido e preciso |
| **Phi-3 Vision** | 2.8 GB | 8 GB | Avançado, excelente OCR |

#### 🏆 Avançados (Máxima Precisão)

| Modelo | Tamanho | RAM Mín | Descrição |
|--------|---------|---------|-----------|
| **LLaVA 1.5 7B** | 4.08 GB | 12 GB | Clássico, estável e testado |
| **LLaVA 1.6 Mistral 7B** | 4.37 GB | 12 GB | Última versão, máxima precisão |

💡 **Dica**: Modelos menores (1-3B) são mais rápidos e consomem menos bateria!

📚 **Mais detalhes**: Veja [MODELOS_MULTIMODAIS.md](MODELOS_MULTIMODAIS.md) para guia completo

## 🚀 Como Usar

### Pré-requisitos

1. **Android 12.0 (API 31) ou superior**
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

### LlamaBridge - Engine de IA

O AION usa o **LlamaBridge**, uma engine proprietária em C++ que:

1. **Valida Modelos**: Verifica se os arquivos GGUF baixados são válidos
2. **Analisa Comandos**: Processa prompts em linguagem natural
3. **Extrai Intenções**: Identifica a tarefa que o usuário quer executar
4. **Gera Ações**: Cria respostas JSON estruturadas com ações específicas

**Exemplos de Processamento:**

- "Abrir o Chrome" → `{action: "OPEN_APP", target: "Chrome"}`
- "Pesquisar por receitas" → `{action: "TYPE_TEXT", text: "receitas"}`
- "Rolar para baixo" → `{action: "SCROLL", direction: "DOWN"}`
- "Voltar" → `{action: "BACK"}`

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
- [ ] Melhorar precisão do parser com ML
- [ ] Suporte a mais idiomas
- [ ] Gravação e replay de tarefas
- [ ] Detecção de elementos por OCR local
- [ ] Sistema de plugins

## 🌟 Diferenças da Versão Original

Esta versão do AION difere da original em:

- ❌ **Removido**: Dependência da API OpenRouter (online)
- ✅ **Adicionado**: Inferência 100% local com LlamaBridge
- ✅ **Adicionado**: Download e gerenciamento de modelos locais
- ✅ **Adicionado**: Processamento inteligente de comandos em C++
- ✅ **Adicionado**: Validação de modelos GGUF
- ✅ **Melhorado**: Privacidade total - nenhum dado sai do dispositivo
- ✅ **Melhorado**: Funciona completamente offline

## 📞 Suporte

Encontrou um bug ou tem uma sugestão? Abra uma [issue](https://github.com/deivid22srk/AION/issues)!

## 🌟 Créditos

Desenvolvido com ❤️ usando:
- [llama.cpp](https://github.com/ggerganov/llama.cpp) - Inferência eficiente de LLMs
- [Hugging Face](https://huggingface.co) - Hub de modelos de IA
- [LLaVA](https://llava-vl.github.io/) - Modelos de visão
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI moderna

---

<div align="center">
  <p>Feito com 🤖 por <a href="https://github.com/deivid22srk">deivid22srk</a></p>
  <p>⭐ Se este projeto foi útil, considere dar uma estrela!</p>
  <p>🔒 Privacidade em primeiro lugar - Sua IA, seu dispositivo, seus dados</p>
</div>
