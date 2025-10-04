package com.aion.aicontroller.ai

import android.graphics.Bitmap
import android.util.Log
import com.aion.aicontroller.api.*
import com.aion.aicontroller.data.AIAction
import com.aion.aicontroller.data.ActionType
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class AIController(
    private val apiKey: String,
    private val modelId: String
) {
    private val gson = Gson()
    
    companion object {
        private const val TAG = "AIController"
        
        private const val SYSTEM_PROMPT = """Você é um assistente de IA especializado em controlar dispositivos Android.

Você receberá:
1. Uma captura de tela do dispositivo
2. Uma tarefa que o usuário quer executar

Sua função é analisar a tela e decidir qual ação tomar para completar a tarefa.

IMPORTANTE: Sua resposta DEVE ser APENAS um objeto JSON válido com a seguinte estrutura:

{
  "action": "CLICK | LONG_CLICK | TYPE_TEXT | SCROLL | SWIPE | BACK | HOME | RECENT_APPS | OPEN_APP | WAIT | TAKE_SCREENSHOT",
  "target": "descrição do elemento (opcional)",
  "x": coordenada_x_em_pixels (opcional),
  "y": coordenada_y_em_pixels (opcional),
  "text": "texto para digitar (apenas para TYPE_TEXT)",
  "direction": "UP | DOWN | LEFT | RIGHT (para SCROLL e SWIPE)",
  "amount": quantidade_de_scroll_ou_distancia (opcional),
  "reasoning": "breve explicação da sua decisão"
}

Exemplos de respostas válidas:

Para clicar em um botão:
{"action": "CLICK", "x": 500, "y": 800, "reasoning": "Clicando no botão de configurações no centro da tela"}

Para abrir um app:
{"action": "OPEN_APP", "target": "Chrome", "reasoning": "Abrindo o navegador Chrome"}

Para digitar texto:
{"action": "TYPE_TEXT", "text": "receitas de bolo", "reasoning": "Digitando a busca solicitada"}

Para rolar a tela:
{"action": "SCROLL", "direction": "DOWN", "amount": 500, "reasoning": "Rolando para baixo para ver mais conteúdo"}

Para voltar:
{"action": "BACK", "reasoning": "Voltando para a tela anterior"}

Quando a tarefa estiver completa, retorne:
{"action": "WAIT", "reasoning": "Tarefa concluída com sucesso"}

LEMBRE-SE: Responda APENAS com o JSON, sem texto adicional antes ou depois."""
    }
    
    suspend fun analyzeScreenAndDecide(
        screenshot: Bitmap,
        task: String,
        conversationHistory: List<String> = emptyList()
    ): AIAction? {
        try {
            val base64Image = OpenRouterAPI.bitmapToBase64(screenshot)
            val imageUrl = OpenRouterAPI.createImageDataUrl(base64Image)
            
            val messages = mutableListOf<Message>()
            
            messages.add(
                Message(
                    role = "system",
                    content = listOf(ContentPart.Text(text = SYSTEM_PROMPT))
                )
            )
            
            conversationHistory.forEach { historyItem ->
                messages.add(
                    Message(
                        role = "assistant",
                        content = listOf(ContentPart.Text(text = historyItem))
                    )
                )
            }
            
            messages.add(
                Message(
                    role = "user",
                    content = listOf(
                        ContentPart.Text(text = "Tarefa: $task\n\nAnálise da tela atual:"),
                        ContentPart.ImageUrl(
                            imageUrl = ImageUrlData(url = imageUrl)
                        )
                    )
                )
            )
            
            val request = ChatRequest(
                model = modelId,
                messages = messages,
                temperature = 0.7,
                maxTokens = 4000
            )
            
            Log.d(TAG, "Enviando requisição para OpenRouter...")
            
            val response = OpenRouterAPI.service.chat(
                authorization = "Bearer $apiKey",
                request = request
            )
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                val content = responseBody?.choices?.firstOrNull()?.message?.content
                
                Log.d(TAG, "Resposta da IA recebida")
                Log.d(TAG, "Conteúdo: $content")
                
                if (content != null) {
                    return parseAIResponse(content)
                } else {
                    Log.e(TAG, "Resposta vazia da API")
                }
            } else {
                Log.e(TAG, "Erro na API: ${response.code()} - ${response.errorBody()?.string()}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao analisar tela: ${e.message}", e)
        }
        
        return null
    }
    
    private fun parseAIResponse(response: String): AIAction? {
        try {
            Log.d(TAG, "Fazendo parse da resposta...")
            val cleanedResponse = response.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            
            Log.d(TAG, "Resposta limpa: $cleanedResponse")
            
            val jsonResponse = gson.fromJson(cleanedResponse, AIResponse::class.java)
            
            Log.d(TAG, "A\u00e7\u00e3o parseada: ${jsonResponse.action}")
            Log.d(TAG, "Reasoning: ${jsonResponse.reasoning}")
            
            val actionType = try {
                ActionType.valueOf(jsonResponse.action.uppercase())
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Tipo de a\u00e7\u00e3o inv\u00e1lido: ${jsonResponse.action}")
                return null
            }
            
            val action = AIAction(
                type = actionType,
                target = jsonResponse.target,
                x = jsonResponse.x,
                y = jsonResponse.y,
                text = jsonResponse.text,
                direction = jsonResponse.direction,
                amount = jsonResponse.amount
            )
            
            Log.d(TAG, "A\u00e7\u00e3o criada com sucesso: $action")
            return action
            
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Erro ao fazer parse da resposta JSON: ${e.message}")
            Log.e(TAG, "Resposta recebida: $response")
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao processar resposta: ${e.message}", e)
            Log.e(TAG, "Resposta completa: $response")
        }
        
        return null
    }
    
    private data class AIResponse(
        val action: String,
        val target: String? = null,
        val x: Int? = null,
        val y: Int? = null,
        val text: String? = null,
        val direction: String? = null,
        val amount: Int? = null,
        val reasoning: String? = null
    )
}
