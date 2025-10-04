# AION - AI Android Controller

<div align="center">
  <h3>🤖 Controle seu Android com Inteligência Artificial 🤖</h3>
  <p>Automatize tarefas no seu dispositivo Android usando modelos de IA com visão computacional</p>
</div>

## 📋 Sobre o Projeto

**AION** (AI Android cONtroller) é um aplicativo Android revolucionário que permite que uma inteligência artificial controle completamente seu dispositivo. Usando modelos de visão multimodal da OpenRouter, a IA pode:

- 📱 **Abrir aplicativos** automaticamente
- 👆 **Clicar em elementos** da interface
- 📜 **Rolar páginas** e navegar
- ⌨️ **Digitar textos** em campos
- 🎯 **Executar tarefas complexas** com múltiplas etapas

## ✨ Características

### 🔥 Principais Recursos

- **Controle Total por IA**: Use comandos em linguagem natural para executar tarefas
- **Modelos Gratuitos**: Suporte a múltiplos modelos gratuitos do OpenRouter
- **Visão Computacional**: A IA "enxerga" a tela e toma decisões inteligentes
- **Interface Moderna**: UI desenvolvida com Jetpack Compose e Material 3
- **Performance Otimizada**: Componentes nativos em C++ para processamento rápido
- **Open Source**: Código 100% aberto e modificável

### 🎨 Modelos de IA Suportados (Gratuitos)

| Modelo | Parâmetros | Suporte a Visão |
|--------|------------|-----------------|
| Qwen 2.5 VL 72B | 72B | ✅ |
| Qwen 2.5 VL 32B | 32B | ✅ |
| Llama 3.2 11B Vision | 11B | ✅ |
| Gemma 3 27B | 27B | ✅ |
| DeepSeek V3.1 | 671B (37B ativo) | ❌ |
| GLM 4.5 Air | MoE | ❌ |

## 🚀 Como Usar

### Pré-requisitos

1. **Android 7.0 (API 24) ou superior**
2. **Conta no OpenRouter** (gratuita)
3. **Permissão de Acessibilidade** ativada

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
2. **Obtenha sua API Key**:
   - Acesse [openrouter.ai](https://openrouter.ai)
   - Crie uma conta gratuita
   - Gere uma API Key em "Keys"
3. **Configure o AION**:
   - Abra o app
   - Vá em "Configurações"
   - Cole sua API Key
   - Selecione um modelo gratuito com visão
4. **Ative o Serviço de Acessibilidade**:
   - Toque em "Ativar" no card vermelho
   - Encontre "AION AI Controller" na lista
   - Ative o serviço

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
- **Retrofit + OkHttp** - Comunicação com API
- **Coroutines + Flow** - Programação assíncrona
- **DataStore** - Armazenamento de preferências
- **C++/JNI** - Processamento de imagens otimizado

### Componentes Principais

```
AION/
├── app/src/main/
│   ├── java/com/aion/aicontroller/
│   │   ├── MainActivity.kt              # Interface principal
│   │   ├── ai/AIController.kt           # Lógica de decisão da IA
│   │   ├── api/OpenRouterAPI.kt         # Cliente API
│   │   ├── service/
│   │   │   ├── AIAccessibilityService   # Serviço de acessibilidade
│   │   │   └── AIControlService         # Orquestrador de tarefas
│   │   └── data/                        # Modelos e gerenciamento
│   ├── cpp/                             # Código nativo C++
│   └── res/                             # Recursos UI
└── .github/workflows/build.yml          # CI/CD
```

## 🔧 Desenvolvimento

### Estrutura do Projeto

O projeto usa uma arquitetura limpa e modular:

1. **UI Layer** (Jetpack Compose)
   - `MainActivity`: Interface principal com navegação
   - Temas e componentes reutilizáveis

2. **Service Layer**
   - `AIAccessibilityService`: Captura de tela e interação
   - `AIControlService`: Gerenciamento de tarefas

3. **AI Layer**
   - `AIController`: Análise de imagens e tomada de decisão
   - `OpenRouterAPI`: Comunicação com modelos de IA

4. **Data Layer**
   - `PreferencesManager`: Persistência de configurações
   - Models: Estruturas de dados

5. **Native Layer** (C++)
   - Processamento otimizado de imagens
   - Operações de baixo nível

### Build e Testes

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Testes unitários
./gradlew test

# Testes instrumentados
./gradlew connectedAndroidTest
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
- **Privacidade**: Suas screenshots são enviadas para o OpenRouter
- **API Limits**: Modelos gratuitos podem ter limites de uso
- **Beta**: Este projeto está em desenvolvimento ativo

## 🎯 Roadmap

- [ ] Suporte a múltiplos idiomas
- [ ] Gravação e replay de tarefas
- [ ] Detecção de elementos por OCR local
- [ ] Modo offline com modelos locais
- [ ] Sistema de plugins
- [ ] Interface web para controle remoto

## 📞 Suporte

Encontrou um bug ou tem uma sugestão? Abra uma [issue](https://github.com/deivid22srk/AION/issues)!

## 🌟 Créditos

Desenvolvido com ❤️ usando:
- [OpenRouter](https://openrouter.ai) - Acesso unificado a modelos de IA
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI moderna
- [Retrofit](https://square.github.io/retrofit/) - Cliente HTTP

---

<div align="center">
  <p>Feito com 🤖 por <a href="https://github.com/deivid22srk">deivid22srk</a></p>
  <p>⭐ Se este projeto foi útil, considere dar uma estrela!</p>
</div>
