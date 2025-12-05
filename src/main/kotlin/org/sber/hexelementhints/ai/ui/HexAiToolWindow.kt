package org.sber.hexelementhints.ai.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import org.sber.hexelementhints.ai.settings.HexAiSettings
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import javax.swing.*

/**
 * Главное окно AI Assistant.
 * Показывает либо панель настройки (если сертификат не указан),
 * либо панель генерации.
 */
class HexAiToolWindow(private val project: Project) : JPanel(BorderLayout()) {

    private val cardLayout = CardLayout()
    private val cardPanel = JPanel(cardLayout)
    
    private val setupPanel: SetupPanel
    private val generatorPanel: GeneratorPanel
    private val notConfiguredPanel: JPanel

    private val settingsButton = JButton("⚙")
    private val statusLabel = JBLabel()

    companion object {
        private const val CARD_NOT_CONFIGURED = "notConfigured"
        private const val CARD_SETUP = "setup"
        private const val CARD_GENERATOR = "generator"
    }

    init {
        // Создаём панели
        setupPanel = SetupPanel(project) { onSettingsSaved() }
        generatorPanel = GeneratorPanel(project)
        notConfiguredPanel = createNotConfiguredPanel()

        // Добавляем карточки
        cardPanel.add(notConfiguredPanel, CARD_NOT_CONFIGURED)
        cardPanel.add(setupPanel, CARD_SETUP)
        cardPanel.add(generatorPanel, CARD_GENERATOR)

        // Заголовок
        val headerPanel = createHeaderPanel()
        
        add(headerPanel, BorderLayout.NORTH)
        add(cardPanel, BorderLayout.CENTER)

        // Показываем нужную карточку
        updateView()
    }

    private fun createHeaderPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(8, 12)

        val titleLabel = JBLabel("Hex AI Assistant")
        titleLabel.font = titleLabel.font.deriveFont(14f)
        
        panel.add(titleLabel, BorderLayout.WEST)

        val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 4, 0))
        rightPanel.add(statusLabel)
        rightPanel.add(settingsButton)
        
        panel.add(rightPanel, BorderLayout.EAST)

        // Кнопка настроек
        settingsButton.toolTipText = "Настройки подключения"
        settingsButton.addActionListener {
            showSetup()
        }

        return panel
    }

    private fun createNotConfiguredPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(40)

        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)

        val iconLabel = JBLabel("🔐")
        iconLabel.font = iconLabel.font.deriveFont(48f)
        iconLabel.alignmentX = CENTER_ALIGNMENT

        val titleLabel = JBLabel("Требуется настройка подключения")
        titleLabel.font = titleLabel.font.deriveFont(16f)
        titleLabel.alignmentX = CENTER_ALIGNMENT

        val descLabel = JBLabel("<html><center>Для работы с AI необходимо указать<br>клиентский сертификат (cert.p12)</center></html>")
        descLabel.alignmentX = CENTER_ALIGNMENT

        val setupButton = JButton("Настроить подключение")
        setupButton.alignmentX = CENTER_ALIGNMENT
        setupButton.addActionListener {
            showSetup()
        }

        contentPanel.add(Box.createVerticalGlue())
        contentPanel.add(iconLabel)
        contentPanel.add(Box.createVerticalStrut(16))
        contentPanel.add(titleLabel)
        contentPanel.add(Box.createVerticalStrut(8))
        contentPanel.add(descLabel)
        contentPanel.add(Box.createVerticalStrut(24))
        contentPanel.add(setupButton)
        contentPanel.add(Box.createVerticalGlue())

        panel.add(contentPanel, BorderLayout.CENTER)
        return panel
    }

    private fun updateView() {
        val settings = HexAiSettings.getInstance()
        
        if (settings.isConfigured()) {
            showGenerator()
            updateStatus()
        } else {
            cardLayout.show(cardPanel, CARD_NOT_CONFIGURED)
            statusLabel.text = ""
        }
    }

    private fun updateStatus() {
        val settings = HexAiSettings.getInstance()
        statusLabel.text = "🟢 ${settings.modelName}"
        statusLabel.foreground = JBUI.CurrentTheme.Label.foreground()
    }

    private fun showSetup() {
        cardLayout.show(cardPanel, CARD_SETUP)
    }

    private fun showGenerator() {
        generatorPanel.refresh()
        cardLayout.show(cardPanel, CARD_GENERATOR)
    }

    private fun onSettingsSaved() {
        updateView()
    }

    /**
     * Обновляет состояние панели (вызывается при фокусе на Tool Window).
     */
    fun refresh() {
        updateView()
        if (HexAiSettings.getInstance().isConfigured()) {
            generatorPanel.refresh()
        }
    }
}
