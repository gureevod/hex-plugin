package org.sber.hexelementhints.ai.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.components.JBScrollPane
import javax.swing.ScrollPaneConstants

/**
 * Фабрика для создания Tool Window "Hex AI".
 */
class HexAiToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val hexAiToolWindow = HexAiToolWindow(project)
        
        // Оборачиваем в ScrollPane для корректного отображения
        val scrollPane = JBScrollPane(
            hexAiToolWindow,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        )
        
        val content = ContentFactory.getInstance().createContent(scrollPane, "", false)
        content.isCloseable = false
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        return true
    }
}
