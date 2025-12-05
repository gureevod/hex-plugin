package org.sber.hexelementhints.ai.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Фабрика для создания Tool Window "Hex AI".
 */
class HexAiToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val hexAiToolWindow = HexAiToolWindow(project)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(hexAiToolWindow, "", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        return true
    }
}
