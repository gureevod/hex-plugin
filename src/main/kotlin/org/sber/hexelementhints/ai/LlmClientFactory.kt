package org.sber.hexelementhints.ai

import chat.giga.client.auth.AuthClient
import chat.giga.http.client.JdkHttpClientBuilder
import chat.giga.http.client.SSL
import chat.giga.langchain4j.GigaChatChatModel
import chat.giga.langchain4j.GigaChatChatRequestParameters
import org.sber.hexelementhints.ai.settings.HexAiSettings
import java.net.http.HttpClient

/**
 * Фабрика для создания LLM клиентов с поддержкой SSL/TLS и клиентского сертификата.
 */
class LlmClientFactory {

    private var cachedClient: GigaChatChatModel? = null
    private var lastSettings: String? = null
    private var lastPassword: String? = null

    /**
     * Создаёт или возвращает закэшированный LLM клиент.
     * При изменении настроек создаётся новый клиент.
     */
    fun getOrCreateClient(settings: HexAiSettings.State, password: String): GigaChatChatModel {
        val settingsKey = "${settings.certPath}|${settings.apiEndpoint}|${settings.modelName}"
        
        if (cachedClient != null && lastSettings == settingsKey && lastPassword == password) {
            return cachedClient!!
        }

        cachedClient = createClient(settings, password)
        lastSettings = settingsKey
        lastPassword = password
        
        return cachedClient!!
    }

    /**
     * Создаёт новый LLM клиент с настроенным SSL контекстом.
     */
    fun createClient(settings: HexAiSettings.State, password: String): GigaChatChatModel {
        val builder = GigaChatChatModel.builder()
            .defaultChatRequestParameters(GigaChatChatRequestParameters.builder()
                .modelName(settings.modelName)
                .build())
            .authClient(
                AuthClient.builder()
                .withCertificatesAuth(JdkHttpClientBuilder()
                    .httpClientBuilder(HttpClient.newBuilder())
                    .ssl(
                        SSL.builder()
                        .truststorePassword(password)
                        .trustStoreType("PKCS12")
                        .truststorePath(settings.certPath)
                        .keystorePassword(password)
                        .keystoreType("PKCS12")
                        .keystorePath(settings.certPath)
                        .build())
                    .build())
                .build())
            .apiUrl(settings.apiEndpoint)
            .logRequests(true)
            .logResponses(true)

        return builder.build()
    }

    /**
     * Проверяет подключение к LLM API.
     * @return Сообщение о результате проверки
     */
    fun testConnection(settings: HexAiSettings.State, password: String): ConnectionTestResult {
        return try {
            val client = createClient(settings, password)
            val response = client.chat("Скажи 'ОК' если можешь это прочитать.")
            
            if (response.contains("ОК", ignoreCase = true)) {
                ConnectionTestResult.Success("Подключение успешно! Модель: ${settings.modelName}")
            } else {
                ConnectionTestResult.Success("Подключение установлено. Ответ: ${response.take(50)}")
            }
        } catch (e: Exception) {
            ConnectionTestResult.Error("Ошибка подключения: ${e.message}")
        }
    }

    /**
     * Очищает кэш клиента
     */
    fun clearCache() {
        cachedClient = null
        lastSettings = null
        lastPassword = null
    }

    sealed class ConnectionTestResult {
        data class Success(val message: String) : ConnectionTestResult()
        data class Error(val message: String) : ConnectionTestResult()
    }
}
