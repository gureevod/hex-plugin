package org.sber.hexelementhints.ai.ui

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import org.sber.hexelementhints.ai.HexAiService
import org.sber.hexelementhints.ai.settings.HexAiSettings
import java.awt.*
import java.awt.datatransfer.StringSelection
import javax.swing.*

/**
 * Панель генерации PageObject из HTML.
 */
class GeneratorPanel(private val project: Project) : JPanel(BorderLayout()) {

    // Источник HTML
    private val sourceGroup = ButtonGroup()
    private val currentFileRadio = JRadioButton("Текущий файл")
    private val selectedTextRadio = JRadioButton("Выделенный текст")
    private val chooseFileRadio = JRadioButton("Выбрать файл...")
    private val fileChooser = TextFieldWithBrowseButton()

    // Дополнительные инструкции
    private val instructionsArea = JBTextArea(3, 40)

    // Кнопка генерации
    private val generateButton = JButton("Сгенерировать")

    // Результат
    private val resultArea = JBTextArea(15, 60)
    private val copyButton = JButton("📋 Копировать")
    private val saveButton = JButton("💾 Сохранить как файл")

    // Статус
    private val statusLabel = JBLabel("")

    init {
        setupUI()
        setupListeners()
        updateCurrentFileLabel()
    }

    private fun setupUI() {
        border = JBUI.Borders.empty(12)

        // Верхняя часть - выбор источника
        val sourcePanel = JPanel()
        sourcePanel.layout = BoxLayout(sourcePanel, BoxLayout.Y_AXIS)
        sourcePanel.border = BorderFactory.createTitledBorder("HTML источник")
        sourcePanel.alignmentX = Component.LEFT_ALIGNMENT

        sourceGroup.add(currentFileRadio)
        sourceGroup.add(selectedTextRadio)
        sourceGroup.add(chooseFileRadio)
        currentFileRadio.isSelected = true

        sourcePanel.add(currentFileRadio)
        sourcePanel.add(selectedTextRadio)

        val filePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        filePanel.add(chooseFileRadio)
        
        // Настройка выбора файла
        val fileDescriptor = FileChooserDescriptorFactory
            .createSingleFileDescriptor()
            .withFileFilter { file -> 
                file.extension?.lowercase() in listOf("html", "htm", "xhtml")
            }
            .withTitle("Выберите HTML файл")

        fileChooser.addBrowseFolderListener(
            "Выберите HTML файл",
            "HTML файл для генерации PageObject",
            project,
            fileDescriptor
        )
        fileChooser.isEnabled = false
        filePanel.add(fileChooser)
        
        sourcePanel.add(filePanel)

        // Инструкции
        val instructionsPanel = JPanel(BorderLayout())
        instructionsPanel.border = BorderFactory.createTitledBorder("Дополнительные инструкции (опционально)")
        instructionsArea.lineWrap = true
        instructionsArea.wrapStyleWord = true
        instructionsArea.emptyText.text = "Например: Добавь метод для невалидного логина"
        instructionsPanel.add(JBScrollPane(instructionsArea), BorderLayout.CENTER)

        // Кнопка генерации
        val generatePanel = JPanel(FlowLayout(FlowLayout.CENTER))
        generateButton.preferredSize = Dimension(200, 36)
        generatePanel.add(generateButton)

        // Результат
        val resultPanel = JPanel(BorderLayout())
        resultPanel.border = BorderFactory.createTitledBorder("Результат")
        
        resultArea.isEditable = false
        resultArea.font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        resultPanel.add(JBScrollPane(resultArea), BorderLayout.CENTER)

        // Кнопки для результата
        val resultButtonsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        resultButtonsPanel.add(copyButton)
        resultButtonsPanel.add(saveButton)
        copyButton.isEnabled = false
        saveButton.isEnabled = false
        resultPanel.add(resultButtonsPanel, BorderLayout.SOUTH)

        // Статус
        statusLabel.border = JBUI.Borders.empty(8, 0, 0, 0)

        // Компоновка
        val topPanel = JPanel()
        topPanel.layout = BoxLayout(topPanel, BoxLayout.Y_AXIS)
        topPanel.add(sourcePanel)
        topPanel.add(Box.createVerticalStrut(8))
        topPanel.add(instructionsPanel)
        topPanel.add(Box.createVerticalStrut(8))
        topPanel.add(generatePanel)

        add(topPanel, BorderLayout.NORTH)
        add(resultPanel, BorderLayout.CENTER)
        add(statusLabel, BorderLayout.SOUTH)
    }

    private fun setupListeners() {
        // Переключение источника
        chooseFileRadio.addActionListener {
            fileChooser.isEnabled = chooseFileRadio.isSelected
        }
        currentFileRadio.addActionListener {
            fileChooser.isEnabled = false
        }
        selectedTextRadio.addActionListener {
            fileChooser.isEnabled = false
        }

        // Генерация
        generateButton.addActionListener {
            generate()
        }

        // Копировать
        copyButton.addActionListener {
            val selection = StringSelection(resultArea.text)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
            statusLabel.text = "✓ Скопировано в буфер обмена"
        }

        // Сохранить
        saveButton.addActionListener {
            saveResult()
        }
    }

    private fun updateCurrentFileLabel() {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        val fileName = editor?.virtualFile?.name ?: "нет открытого файла"
        currentFileRadio.text = "Текущий файл ($fileName)"
    }

    private fun generate() {
        val html = getHtmlContent()
        if (html.isNullOrBlank()) {
            statusLabel.text = "✗ Не удалось получить HTML контент"
            statusLabel.foreground = JBUI.CurrentTheme.Label.foreground()
            return
        }

        val instructions = instructionsArea.text.takeIf { it.isNotBlank() }

        statusLabel.text = "Генерация..."
        statusLabel.foreground = JBUI.CurrentTheme.Label.foreground()
        generateButton.isEnabled = false
        resultArea.text = ""
        copyButton.isEnabled = false
        saveButton.isEnabled = false

        HexAiService.getInstance(project).generatePageObject(
            html = html,
            additionalInstructions = instructions,
            onSuccess = { code ->
                resultArea.text = code
                statusLabel.text = "✓ Генерация завершена"
                statusLabel.foreground = JBUI.CurrentTheme.Label.foreground()
                generateButton.isEnabled = true
                copyButton.isEnabled = true
                saveButton.isEnabled = true
            },
            onError = { error ->
                statusLabel.text = "✗ Ошибка: ${error.message}"
                statusLabel.foreground = JBUI.CurrentTheme.Label.foreground()
                generateButton.isEnabled = true
            }
        )
    }

    private fun getHtmlContent(): String? {
        return when {
            currentFileRadio.isSelected -> getCurrentFileContent()
            selectedTextRadio.isSelected -> getSelectedText()
            chooseFileRadio.isSelected -> getFileContent(fileChooser.text)
            else -> null
        }
    }

    private fun getCurrentFileContent(): String? {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        return editor?.document?.text
    }

    private fun getSelectedText(): String? {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        return editor?.selectionModel?.selectedText
    }

    private fun getFileContent(path: String): String? {
        if (path.isBlank()) return null
        return try {
            java.io.File(path).readText()
        } catch (e: Exception) {
            null
        }
    }

    private fun saveResult() {
        val code = resultArea.text
        if (code.isBlank()) return

        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Сохранить PageObject"
        fileChooser.fileFilter = javax.swing.filechooser.FileNameExtensionFilter("Java файлы", "java")
        
        // Предлагаем имя на основе сгенерированного класса
        val className = org.sber.hexelementhints.ai.CodeExtractor.extractClassName(code)
        if (className != null) {
            fileChooser.selectedFile = java.io.File("$className.java")
        }

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                var file = fileChooser.selectedFile
                if (!file.name.endsWith(".java")) {
                    file = java.io.File(file.absolutePath + ".java")
                }
                file.writeText(code)
                statusLabel.text = "✓ Сохранено: ${file.name}"
                statusLabel.foreground = JBUI.CurrentTheme.Label.foreground()
            } catch (e: Exception) {
                statusLabel.text = "✗ Ошибка сохранения: ${e.message}"
                statusLabel.foreground = JBUI.CurrentTheme.Label.foreground()
            }
        }
    }

    /**
     * Устанавливает HTML контент для генерации.
     * Используется при вызове из контекстного меню.
     */
    fun setHtmlContent(html: String, source: String) {
        selectedTextRadio.isSelected = true
        instructionsArea.text = ""
        // HTML будет использован при нажатии на кнопку генерации
        // Для упрощения просто запускаем генерацию сразу
        statusLabel.text = "Источник: $source"
    }

    /**
     * Обновляет состояние панели (например, при открытии).
     */
    fun refresh() {
        updateCurrentFileLabel()
    }
}
