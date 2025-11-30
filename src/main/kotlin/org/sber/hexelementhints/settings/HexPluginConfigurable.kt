package org.sber.hexelementhints.settings

import com.intellij.openapi.options.Configurable
import javax.swing.*

/**
 * UI для настроек плагина
 */
class HexPluginConfigurable : Configurable {

    private var settingsPanel: JPanel? = null
    private var inlayHintsCheckbox: JCheckBox? = null
    private var lineMarkersCheckbox: JCheckBox? = null
    private var inspectionsCheckbox: JCheckBox? = null

    override fun getDisplayName(): String = "Hex Framework"

    override fun createComponent(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        inlayHintsCheckbox = JCheckBox("Show inlay hints with Russian names")
        lineMarkersCheckbox = JCheckBox("Show line markers for Hex elements")
        inspectionsCheckbox = JCheckBox("Enable code inspections")

        panel.add(inlayHintsCheckbox)
        panel.add(lineMarkersCheckbox)
        panel.add(inspectionsCheckbox)

        settingsPanel = panel
        return panel
    }

    override fun isModified(): Boolean {
        val settings = HexPluginSettings.getInstance()
        return inlayHintsCheckbox?.isSelected != settings.showInlayHints ||
                lineMarkersCheckbox?.isSelected != settings.showLineMarkers ||
                inspectionsCheckbox?.isSelected != settings.enableInspections
    }

    override fun apply() {
        val settings = HexPluginSettings.getInstance()
        inlayHintsCheckbox?.isSelected?.let { settings.showInlayHints = it }
        lineMarkersCheckbox?.isSelected?.let { settings.showLineMarkers = it }
        inspectionsCheckbox?.isSelected?.let { settings.enableInspections = it }
    }

    override fun reset() {
        val settings = HexPluginSettings.getInstance()
        inlayHintsCheckbox?.isSelected = settings.showInlayHints
        lineMarkersCheckbox?.isSelected = settings.showLineMarkers
        inspectionsCheckbox?.isSelected = settings.enableInspections
    }
}