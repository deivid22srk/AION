# Correções de Build - AION V2.0

## 🐛 Problemas Identificados e Resolvidos

### Build #30 - Erro de Java Version

**Erro:**
```
Unsupported class file major version 65
Failed to transform litertlm-0.0.0-alpha01.aar
```

**Causa:**
- A biblioteca `litertlm` foi compilada com Java 21 (version 65)
- O projeto estava configurado para Java 8 (version 52)

**Solução:**
```kotlin
// app/build.gradle.kts
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlinOptions {
    jvmTarget = "21"
}
```

```properties
# gradle.properties
android.enableJetifier=false  # Jetifier não suporta Java 21
```

---

### Build #08484 - Erro de minSdk

**Erro:**
```
uses-sdk:minSdkVersion 24 cannot be smaller than version 31 declared in library 
[com.google.ai.edge.litertlm:litertlm:0.0.0-alpha01]

Suggestion: increase this project's minSdk version to at least 31
```

**Causa:**
- A biblioteca `litertlm` requer Android 12 (API 31) como mínimo
- O projeto estava configurado com minSdk 24 (Android 7.0)

**Solução:**
```kotlin
// app/build.gradle.kts
defaultConfig {
    applicationId = "com.aion.aicontroller"
    minSdk = 31  // Atualizado de 24 para 31
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"
}
```

```yaml
# .github/workflows/main.yml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'  # Atualizado de '17' para '21'
    distribution: 'temurin'
```

---

## ✅ Configuração Final

### build.gradle.kts
```kotlin
android {
    namespace = "com.aion.aicontroller"
    compileSdk = 34

    defaultConfig {
        minSdk = 31        // Android 12.0+
        targetSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }
}
```

### gradle.properties
```properties
android.useAndroidX=true
android.enableJetifier=false  # Desabilitado para Java 21
```

### GitHub Actions Workflow
```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: gradle
```

---

## 📋 Versões de Java

| Version | Java | Notas |
|---------|------|-------|
| 52 | Java 8 | ❌ Incompatível com litertlm |
| 55 | Java 11 | ❌ Incompatível com litertlm |
| 61 | Java 17 | ❌ Incompatível com litertlm |
| **65** | **Java 21** | ✅ **Obrigatório para litertlm** |

---

## 📋 Versões de Android

| API | Versão | Notas |
|-----|--------|-------|
| 24 | Android 7.0 | ❌ Incompatível com litertlm |
| 26 | Android 8.0 | ❌ Incompatível com litertlm |
| 29 | Android 10.0 | ❌ Incompatível com litertlm |
| **31** | **Android 12.0** | ✅ **Mínimo obrigatório** |
| 33 | Android 13.0 | ✅ Recomendado |
| 34 | Android 14.0 | ✅ Ideal |

---

## 🎯 Impacto das Mudanças

### Compatibilidade
- **Antes:** Android 7.0+ (93% dos dispositivos)
- **Depois:** Android 12.0+ (73% dos dispositivos)
- **Perda:** ~20% de dispositivos antigos

### Benefícios
- ✅ Acesso a modelos multimodais de última geração
- ✅ APIs modernas do Android 12+
- ✅ Melhor performance e segurança
- ✅ Recursos de privacidade aprimorados

### Alternativas para Dispositivos Antigos
Para suportar Android 7.0-11.0, é necessário:
1. Remover dependência do `litertlm`
2. Usar apenas TensorFlow Lite (sem multimodal)
3. Manter apenas modo VISION_ONLY
4. Voltar para Java 8

---

## 🔧 Comandos de Compilação

### Local (Requer Java 21)
```bash
# Verificar versão do Java
java -version  # Deve ser 21.x.x

# Compilar
./gradlew clean
./gradlew assembleDebug

# APK estará em:
# app/build/outputs/apk/debug/app-debug.apk
```

### GitHub Actions
- Workflow atualizado automaticamente
- Usa Java 21 e Android SDK 34
- Compila e faz upload do APK automaticamente

---

## ✅ Status

**Todos os problemas resolvidos:**
- ✅ Java 21 configurado
- ✅ minSdk 31 configurado
- ✅ Jetifier desabilitado
- ✅ GitHub Actions atualizado
- ✅ Documentação atualizada
- ✅ Pronto para build

**Data:** 05/10/2025
**Versão:** 2.0.1
