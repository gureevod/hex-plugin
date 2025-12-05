package org.sber.hexelementhints.ai.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Страница настроек AI в Settings > Tools > Hex AI Assistant.
 */
class HexAiConfigurable : Configurable {

    private var mainPanel: JPanel? = null
    private var certPathField: TextFieldWithBrowseButton? = null
    private var passwordField: JBPasswordField? = null
    private var savePasswordCheckbox: JBCheckBox? = null
    private var apiEndpointField: JBTextField? = null
    private var modelNameField: JBTextField? = null

    override fun getDisplayName(): String = "Hex AI Assistant"

    override fun createComponent(): JComponent {
        certPathField = TextFieldWithBrowseButton().apply {
            val descriptor = FileChooserDescriptorFactory
                .createSingleFileDescriptor()
                .withFileFilter { file ->
                    file.extension?.lowercase() in listOf("p12", "pfx")
                }
                .withTitle("Выберите сертификат")
                .withDescription("Файл клиентского сертификата PKCS12 (.p12, .pfx)")

            addBrowseFolderListener(
                "Выберите сертификат",
                "Файл клиентского сертификата PKCS12 (.p12, .pfx)",
                null,
                descriptor
            )
        }

        passwordField = JBPasswordField()
        savePasswordCheckbox = JBCheckBox("Сохранить пароль в Credential Store", true)
        apiEndpointField = JBTextField()
        modelNameField = JBTextField()

        // Подсказки
        apiEndpointField?.emptyText?.text = "https://api.openai.com/v1"
        modelNameField?.emptyText?.text = "gpt-4"

        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Сертификат (PKCS12):"), certPathField!!, 1, false)
            .addLabeledComponent(JBLabel("Пароль сертификата:"), passwordField!!, 1, false)
            .addComponent(savePasswordCheckbox!!)
            .addSeparator()
            .addLabeledComponent(JBLabel("API Endpoint:"), apiEndpointField!!, 1, false)
            .addLabeledComponent(JBLabel("Модель:"), modelNameField!!, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return mainPanel!!
    }

    override fun isModified(): Boolean {
        val settings = HexAiSettings.getInstance()
        return certPathField?.text != settings.certPath ||
                apiEndpointField?.text != settings.apiEndpoint ||
                modelNameField?.text != settings.modelName ||
                savePasswordCheckbox?.isSelected != settings.savePassword ||
                (savePasswordCheckbox?.isSelected == true && 
                    String(passwordField?.password ?: charArrayOf()) != (PasswordStorage.getPassword() ?: ""))
    }

    override fun apply() {
        val settings = HexAiSettings.getInstance()
        
        settings.certPath = certPathField?.text ?: ""
        settings.apiEndpoint = apiEndpointField?.text?.ifBlank { "https://api.openai.com/v1" } ?: "https://api.openai.com/v1"
        settings.modelName = modelNameField?.text?.ifBlank { "gpt-4" } ?: "gpt-4"
        settings.savePassword = savePasswordCheckbox?.isSelected ?: true

        // Сохраняем пароль
        val password = String(passwordField?.password ?: charArrayOf())
        if (settings.savePassword && password.isNotEmpty()) {
            PasswordStorage.savePassword(password)
        } else if (!settings.savePassword) {
            PasswordStorage.clearPassword()
        }
    }

    override fun reset() {
        val settings = HexAiSettings.getInstance()
        
        certPathField?.text = settings.certPath
        apiEndpointField?.text = settings.apiEndpoint
        modelNameField?.text = settings.modelName
        savePasswordCheckbox?.isSelected = settings.savePassword
        
        // Загружаем сохранённый пароль
        val savedPassword = PasswordStorage.getPassword()
        if (savedPassword != null) {
            passwordField?.text = savedPassword
        }
    }

    override fun disposeUIResources() {
        mainPanel = null
        certPathField = null
        passwordField = null
        savePasswordCheckbox = null
        apiEndpointField = null
        modelNameField = null
    }
}
