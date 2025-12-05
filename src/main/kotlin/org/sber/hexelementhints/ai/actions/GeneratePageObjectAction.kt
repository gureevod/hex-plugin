package org.sber.hexelementhints.ai.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager
import org.sber.hexelementhints.ai.settings.HexAiSettings

/**
 * Action для генерации PageObject из HTML файла или выделенного текста.
 * Доступен в контекстном меню редактора и Project View.
 */
class GeneratePageObjectAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        // Открываем Tool Window
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Hex AI")
        toolWindow?.show {
            // После открытия можно передать контекст
            // Пока просто открываем окно
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val editor = e.getData(CommonDataKeys.EDITOR)

        // Показываем action для HTML файлов или если есть выделенный текст
        val isHtmlFile = file?.extension?.lowercase() in listOf("html", "htm", "xhtml")
        val hasSelection = editor?.selectionModel?.hasSelection() == true

        e.presentation.isEnabledAndVisible = project != null && (isHtmlFile || hasSelection)
    }
}
