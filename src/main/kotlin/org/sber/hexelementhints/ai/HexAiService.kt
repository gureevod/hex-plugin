package org.sber.hexelementhints.ai

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import org.sber.hexelementhints.ai.settings.HexAiSettings
import org.sber.hexelementhints.ai.settings.PasswordStorage

/**
 * Сервис для генерации кода с помощью AI.
 * Выполняет запросы в фоновом потоке, не блокируя UI.
 */
@Service(Service.Level.PROJECT)
class HexAiService(private val project: Project) {

    private val logger = Logger.getInstance(HexAiService::class.java)
    private val clientFactory = LlmClientFactory()

    /**
     * Генерирует PageObject из HTML в фоновом режиме.
     */
    fun generatePageObject(
        html: String,
        additionalInstructions: String? = null,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Генерация PageObject...",
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Подключение к AI..."

                try {
                    val settings = HexAiSettings.getInstance()
                    val password = PasswordStorage.getPassword() ?: ""

                    if (!settings.isConfigured()) {
                        throw IllegalStateException("AI подключение не настроено. Укажите путь к сертификату в настройках.")
                    }

                    indicator.text = "Генерация кода..."
                    
                    val client = clientFactory.getOrCreateClient(settings.state, password)
                    
                    val systemMessage = SystemMessage.from(PromptBuilder.getSystemPrompt())
                    val userMessage = UserMessage.from(PromptBuilder.buildPageObjectPrompt(html, additionalInstructions))
                    
                    val response = client.chat(listOf(systemMessage, userMessage))
                    
                    val code = CodeExtractor.extractCode(response.aiMessage().text())
                    val formattedCode = CodeExtractor.formatForDisplay(code)

                    indicator.text = "Готово!"

                    // Возврат в EDT для обновления UI
                    ApplicationManager.getApplication().invokeLater {
                        onSuccess(formattedCode)
                    }
                } catch (e: Exception) {
                    logger.error("Error generating PageObject", e)
                    ApplicationManager.getApplication().invokeLater {
                        onError(e)
                    }
                }
            }
        })
    }

    /**
     * Проверяет подключение к AI сервису.
     */
    fun testConnection(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Проверка подключения...",
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                
                try {
                    val settings = HexAiSettings.getInstance()
                    val password = PasswordStorage.getPassword() ?: ""

                    val result = clientFactory.testConnection(settings.state, password)
                    
                    ApplicationManager.getApplication().invokeLater {
                        when (result) {
                            is LlmClientFactory.ConnectionTestResult.Success -> onSuccess(result.message)
                            is LlmClientFactory.ConnectionTestResult.Error -> onError(result.message)
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error testing connection", e)
                    ApplicationManager.getApplication().invokeLater {
                        onError("Ошибка: ${e.message}")
                    }
                }
            }
        })
    }

    /**
     * Проверяет, настроено ли подключение.
     */
    fun isConfigured(): Boolean {
        return HexAiSettings.getInstance().isConfigured()
    }

    /**
     * Очищает кэш LLM клиента (например, при смене настроек).
     */
    fun clearClientCache() {
        clientFactory.clearCache()
    }

    companion object {
        fun getInstance(project: Project): HexAiService {
            return project.getService(HexAiService::class.java)
        }
    }
}
