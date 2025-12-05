package org.sber.hexelementhints.ai.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import org.sber.hexelementhints.ai.HexAiService
import org.sber.hexelementhints.ai.settings.HexAiSettings
import org.sber.hexelementhints.ai.settings.PasswordStorage
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

/**
 * Панель настройки подключения к AI (выбор сертификата, endpoint, модель).
 */
class SetupPanel(
    private val project: Project,
    private val onSettingsSaved: () -> Unit
) : JPanel(BorderLayout()) {

    private val certPathField = TextFieldWithBrowseButton()
    private val passwordField = JBPasswordField()
    private val savePasswordCheckbox = JCheckBox("Сохранить пароль в Credential Store", true)
    private val apiEndpointField = JBTextField()
    private val modelNameField = JBTextField()
    
    private val testButton = JButton("Проверить подключение")
    private val saveButton = JButton("Сохранить")
    private val cancelButton = JButton("Отмена")
    
    private val statusLabel = JBLabel("")

    init {
        setupUI()
        loadSettings()
        setupListeners()
    }

    private fun setupUI() {
        border = JBUI.Borders.empty(16)

        // Настройка выбора файла сертификата
        val fileDescriptor = FileChooserDescriptorFactory
            .createSingleFileDescriptor()
            .withFileFilter { file -> 
                file.extension?.lowercase() in listOf("p12", "pfx")
            }
            .withTitle("Выберите сертификат")
            .withDescription("Файл клиентского сертификата PKCS12 (.p12, .pfx)")

        certPathField.addBrowseFolderListener(
            "Выберите сертификат",
            "Файл клиентского сертификата PKCS12 (.p12, .pfx)",
            project,
            fileDescriptor
        )

        // Основная форма
        val formPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Сертификат (PKCS12):"), certPathField, 1, false)
            .addLabeledComponent(JBLabel("Пароль сертификата:"), passwordField, 1, false)
            .addComponent(savePasswordCheckbox)
            .addSeparator()
            .addLabeledComponent(JBLabel("API Endpoint:"), apiEndpointField, 1, false)
            .addLabeledComponent(JBLabel("Модель:"), modelNameField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        add(formPanel, BorderLayout.CENTER)

        // Кнопки внизу
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        buttonPanel.add(testButton)
        buttonPanel.add(cancelButton)
        buttonPanel.add(saveButton)

        val bottomPanel = JPanel(BorderLayout())
        bottomPanel.add(statusLabel, BorderLayout.WEST)
        bottomPanel.add(buttonPanel, BorderLayout.EAST)
        
        add(bottomPanel, BorderLayout.SOUTH)

        // Подсказки
        apiEndpointField.emptyText.text = "https://api.openai.com/v1"
        modelNameField.emptyText.text = "gpt-4"
    }

    private fun loadSettings() {
        val settings = HexAiSettings.getInstance()
        
        certPathField.text = settings.certPath
        apiEndpointField.text = settings.apiEndpoint.ifBlank { "https://api.openai.com/v1" }
        modelNameField.text = settings.modelName.ifBlank { "gpt-4" }
        savePasswordCheckbox.isSelected = settings.savePassword
        
        // Загружаем сохранённый пароль
        val savedPassword = PasswordStorage.getPassword()
        if (savedPassword != null) {
            passwordField.text = savedPassword
        }
    }

    private fun setupListeners() {
        testButton.addActionListener {
            testConnection()
        }

        saveButton.addActionListener {
            saveSettings()
        }

        cancelButton.addActionListener {
            loadSettings() // Сбрасываем изменения
            statusLabel.text = "Изменения отменены"
        }
    }

    private fun testConnection() {
        statusLabel.text = "Проверка подключения..."
        statusLabel.icon = null
        testButton.isEnabled = false

        // Временно сохраняем настройки для теста
        applySettingsToState()

        HexAiService.getInstance(project).testConnection(
            onSuccess = { message ->
                statusLabel.text = "✓ $message"
                statusLabel.foreground = JBUI.CurrentTheme.Label.foreground()
                testButton.isEnabled = true
            },
            onError = { message ->
                statusLabel.text = "✗ $message"
                statusLabel.foreground = JBUI.CurrentTheme.Label.foreground()
                testButton.isEnabled = true
            }
        )
    }

    private fun saveSettings() {
        applySettingsToState()
        
        val settings = HexAiSettings.getInstance()
        
        // Сохраняем пароль
        if (savePasswordCheckbox.isSelected) {
            PasswordStorage.savePassword(String(passwordField.password))
        } else {
            PasswordStorage.clearPassword()
        }

        // Очищаем кэш клиента, чтобы при следующем запросе использовались новые настройки
        HexAiService.getInstance(project).clearClientCache()

        statusLabel.text = "✓ Настройки сохранены"
        statusLabel.foreground = JBUI.CurrentTheme.Label.foreground()

        onSettingsSaved()
    }

    private fun applySettingsToState() {
        val settings = HexAiSettings.getInstance()
        settings.certPath = certPathField.text
        settings.apiEndpoint = apiEndpointField.text.ifBlank { "https://api.openai.com/v1" }
        settings.modelName = modelNameField.text.ifBlank { "gpt-4" }
        settings.savePassword = savePasswordCheckbox.isSelected
    }

    /**
     * Получает текущий пароль из поля ввода.
     */
    fun getPassword(): String = String(passwordField.password)

    /**
     * Проверяет, заполнены ли обязательные поля.
     */
    override fun isValid(): Boolean {
        return certPathField.text.isNotBlank() && passwordField.password.isNotEmpty()
    }
}
