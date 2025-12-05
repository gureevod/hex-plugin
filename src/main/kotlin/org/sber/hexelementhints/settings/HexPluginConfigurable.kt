package org.sber.hexelementhints.settings

import com.intellij.openapi.options.Configurable
import javax.swing.*
import javax.swing.border.TitledBorder

/**
 * UI для настроек плагина
 */
class HexPluginConfigurable : Configurable {

    private var settingsPanel: JPanel? = null
    private var inlayHintsCheckbox: JCheckBox? = null
    private var lineMarkersCheckbox: JCheckBox? = null
    private var inspectionsCheckbox: JCheckBox? = null
    
    // Новые компоненты для настроек содержимого
    private var hintShowElementNameCheckbox: JCheckBox? = null
    private var hintShowJavaDocCheckbox: JCheckBox? = null
    private var hintShowLocatorCheckbox: JCheckBox? = null
    private var hintShowFieldTypeCheckbox: JCheckBox? = null
    private var locatorLengthSpinner: JSpinner? = null

    override fun getDisplayName(): String = "Hex Framework"

    override fun createComponent(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Основные переключатели
        panel.add(JLabel("Основные настройки:"))
        inlayHintsCheckbox = JCheckBox("Показывать inlay hints")
        lineMarkersCheckbox = JCheckBox("Показывать маркеры в gutter")
        inspectionsCheckbox = JCheckBox("Включить инспекции кода")
        
        panel.add(inlayHintsCheckbox)
        panel.add(lineMarkersCheckbox)
        panel.add(inspectionsCheckbox)
        
        panel.add(Box.createVerticalStrut(16))
        panel.add(JSeparator())
        panel.add(Box.createVerticalStrut(8))
        
        // Настройки содержимого hints
        panel.add(JLabel("Содержимое подсказок:"))
        hintShowElementNameCheckbox = JCheckBox("Имя из @Element(name)")
        hintShowJavaDocCheckbox = JCheckBox("Описание из JavaDoc")
        hintShowLocatorCheckbox = JCheckBox("Локатор (xpath/css)")
        hintShowFieldTypeCheckbox = JCheckBox("Тип элемента")
        
        panel.add(hintShowElementNameCheckbox)
        panel.add(hintShowJavaDocCheckbox)
        panel.add(hintShowLocatorCheckbox)
        panel.add(hintShowFieldTypeCheckbox)
        
        // Дополнительные настройки
        panel.add(Box.createVerticalStrut(8))
        panel.add(JLabel("Максимальная длина локатора:"))
        locatorLengthSpinner = JSpinner(SpinnerNumberModel(50, 10, 200, 10))
        panel.add(locatorLengthSpinner)

        settingsPanel = panel
        return panel
    }

    override fun isModified(): Boolean {
        val settings = HexPluginSettings.getInstance()
        return inlayHintsCheckbox?.isSelected != settings.showInlayHints ||
                lineMarkersCheckbox?.isSelected != settings.showLineMarkers ||
                inspectionsCheckbox?.isSelected != settings.enableInspections ||
                hintShowElementNameCheckbox?.isSelected != settings.hintShowElementName ||
                hintShowJavaDocCheckbox?.isSelected != settings.hintShowJavaDoc ||
                hintShowLocatorCheckbox?.isSelected != settings.hintShowLocator ||
                hintShowFieldTypeCheckbox?.isSelected != settings.hintShowFieldType ||
                (locatorLengthSpinner?.value as? Int) != settings.hintMaxLocatorLength
    }

    override fun apply() {
        val settings = HexPluginSettings.getInstance()
        inlayHintsCheckbox?.isSelected?.let { settings.showInlayHints = it }
        lineMarkersCheckbox?.isSelected?.let { settings.showLineMarkers = it }
        inspectionsCheckbox?.isSelected?.let { settings.enableInspections = it }
        hintShowElementNameCheckbox?.isSelected?.let { settings.hintShowElementName = it }
        hintShowJavaDocCheckbox?.isSelected?.let { settings.hintShowJavaDoc = it }
        hintShowLocatorCheckbox?.isSelected?.let { settings.hintShowLocator = it }
        hintShowFieldTypeCheckbox?.isSelected?.let { settings.hintShowFieldType = it }
        (locatorLengthSpinner?.value as? Int)?.let { settings.hintMaxLocatorLength = it }
    }

    override fun reset() {
        val settings = HexPluginSettings.getInstance()
        inlayHintsCheckbox?.isSelected = settings.showInlayHints
        lineMarkersCheckbox?.isSelected = settings.showLineMarkers
        inspectionsCheckbox?.isSelected = settings.enableInspections
        hintShowElementNameCheckbox?.isSelected = settings.hintShowElementName
        hintShowJavaDocCheckbox?.isSelected = settings.hintShowJavaDoc
        hintShowLocatorCheckbox?.isSelected = settings.hintShowLocator
        hintShowFieldTypeCheckbox?.isSelected = settings.hintShowFieldType
        locatorLengthSpinner?.value = settings.hintMaxLocatorLength
    }
}