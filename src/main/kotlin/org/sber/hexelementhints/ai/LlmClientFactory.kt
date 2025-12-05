package org.sber.hexelementhints.ai

import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.openai.OpenAiChatModel
import org.sber.hexelementhints.ai.settings.HexAiSettings
import java.io.FileInputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.time.Duration
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

/**
 * Фабрика для создания LLM клиентов с поддержкой SSL/TLS и клиентского сертификата.
 */
class LlmClientFactory {

    private var cachedClient: ChatLanguageModel? = null
    private var lastSettings: String? = null
    private var lastPassword: String? = null

    /**
     * Создаёт или возвращает закэшированный LLM клиент.
     * При изменении настроек создаётся новый клиент.
     */
    fun getOrCreateClient(settings: HexAiSettings.State, password: String): ChatLanguageModel {
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
    fun createClient(settings: HexAiSettings.State, password: String): ChatLanguageModel {
        val builder = OpenAiChatModel.builder()
            .baseUrl(settings.apiEndpoint)
            .modelName(settings.modelName)
            .timeout(Duration.ofSeconds(120))
            .maxRetries(2)
            .logRequests(true)
            .logResponses(true)

        // Если указан сертификат, настраиваем SSL
        if (settings.certPath.isNotBlank()) {
            val sslContext = createSslContext(settings.certPath, password)
            // OpenAI клиент в LangChain4j использует внутренний HTTP клиент
            // Для кастомного SSL требуется настройка через системные свойства или custom HTTP client
            // В текущей реализации LangChain4j это делается через proxy или системные настройки
            
            // Устанавливаем системные свойства для SSL
            System.setProperty("javax.net.ssl.keyStore", settings.certPath)
            System.setProperty("javax.net.ssl.keyStorePassword", password)
            System.setProperty("javax.net.ssl.keyStoreType", "PKCS12")
        }

        // Для OpenAI-совместимого API без API ключа используем заглушку
        builder.apiKey("not-required-with-cert")

        return builder.build()
    }

    /**
     * Создаёт SSL контекст с клиентским сертификатом PKCS12.
     */
    private fun createSslContext(certPath: String, password: String): SSLContext {
        val keyStore = KeyStore.getInstance("PKCS12")
        FileInputStream(certPath).use { fis ->
            keyStore.load(fis, password.toCharArray())
        }

        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, password.toCharArray())

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        // Используем тот же keystore для trust manager или системный
        try {
            tmf.init(keyStore)
        } catch (e: Exception) {
            // Если в keystore нет доверенных сертификатов, используем системные
            tmf.init(null as KeyStore?)
        }

        return SSLContext.getInstance("TLS").apply {
            init(kmf.keyManagers, tmf.trustManagers, SecureRandom())
        }
    }

    /**
     * Проверяет подключение к LLM API.
     * @return Сообщение о результате проверки
     */
    fun testConnection(settings: HexAiSettings.State, password: String): ConnectionTestResult {
        return try {
            val client = createClient(settings, password)
            val response = client.generate("Say 'OK' if you can read this.")
            
            if (response.contains("OK", ignoreCase = true)) {
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
