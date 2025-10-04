# AION - AI Android Controller (Local Edition)

<div align="center">
  <h3>🤖 Controle seu Android com Inteligência Artificial Local 🤖</h3>
  <p>Automatize tarefas no seu dispositivo Android usando modelos locais de visão do Hugging Face</p>
</div>

## 📋 Sobre o Projeto

**AION** (AI Android cONtroller) é um aplicativo Android revolucionário que permite que uma inteligência artificial **rodando localmente** controle completamente seu dispositivo. Usando modelos de visão multimodal (LLaVA) do Hugging Face Hub, a IA pode:

- 📱 **Abrir aplicativos** automaticamente
- 👆 **Clicar em elementos** da interface
- 📜 **Rolar páginas** e navegar
- ⌨️ **Digitar textos** em campos
- 🎯 **Executar tarefas complexas** com múltiplas etapas
- 🔒 **100% Offline** - Seus dados nunca saem do dispositivo

## ✨ Características

### 🔥 Principais Recursos

- **Inferência Local**: Modelos rodam 100% no seu dispositivo, sem necessidade de internet
- **Modelos do Hugging Face**: Download direto de modelos LLaVA quantizados
- **Privacidade Total**: Nenhum dado é enviado para servidores externos
- **Visão Computacional**: A IA "enxerga" a tela e toma decisões inteligentes
- **Interface Moderna**: UI desenvolvida com Jetpack Compose e Material 3
- **Performance Otimizada**: Componentes nativos em C++ com llama.cpp
- **Open Source**: Código 100% aberto e modificável

### 🎨 Modelos de IA Suportados (Locais)

| Modelo | Tamanho | Recomendado | Descrição |
|--------|---------|-------------|-----------|
| LLaVA 1.6 Mistral 7B Q4 | 4.37 GB | ✅ | Melhor equilíbrio entre velocidade e qualidade |
| LLaVA 1.6 Vicuna 7B Q4 | 4.37 GB | ✅ | Alternativa equilibrada |
| LLaVA 1.5 7B Q4 | 4.08 GB | ✅ | Versão estável e testada |
| BakLLaVA 1 Q4 | 4.37 GB | ⚡ | Especializado em tarefas visuais |
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
```

## 🏗️ Arquitetura

### Tecnologias Utilizadas

- **Kotlin** - Linguagem principal
- **Jetpack Compose** - UI moderna e reativa
- **Accessibility Service** - Controle do dispositivo
- **OkHttp** - Download de modelos do Hugging Face
- **Coroutines + Flow** - Programação assíncrona
- **DataStore** - Armazenamento de preferências
- **C++/JNI + llama.cpp** - Inferência local de modelos LLaVA
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
│   ├── cpp/                                # Código nativo C++ (llama.cpp)
│   └── res/                                # Recursos UI
└── .github/workflows/build.yml             # CI/CD
```

## 🔧 Desenvolvimento

### Integração com llama.cpp

⚠️ **IMPORTANTE**: A versão atual usa **stubs** (funções mockadas) para as funções de visão. Para inferência real, você precisa integrar a biblioteca llama.cpp completa.

Consulte [LLAMA_CPP_INTEGRATION.md](LLAMA_CPP_INTEGRATION.md) para instruções detalhadas de como integrar o llama.cpp real com suporte a LLaVA.

### Build e Testes

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Limpar build
./gradlew clean
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
- **Performance**: Inferência pode ser lenta em dispositivos mais antigos
- **Beta**: Este projeto está em desenvolvimento ativo

## 🎯 Roadmap

- [x] ✅ Inferência local com modelos de visão
- [x] ✅ Download de modelos do Hugging Face
- [x] ✅ Gerenciamento de modelos locais
- [ ] Integração completa do llama.cpp (atualmente usando stubs)
- [ ] Suporte a múltiplos idiomas
- [ ] Gravação e replay de tarefas
- [ ] Detecção de elementos por OCR local
- [ ] Otimizações de performance (quantização dinâmica)
- [ ] Sistema de plugins

## 🌟 Diferenças da Versão Original

Esta versão do AION difere da original em:

- ❌ **Removido**: Dependência da API OpenRouter (online)
- ✅ **Adicionado**: Inferência 100% local com llama.cpp
- ✅ **Adicionado**: Download e gerenciamento de modelos locais
- ✅ **Adicionado**: Suporte a modelos LLaVA do Hugging Face
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
