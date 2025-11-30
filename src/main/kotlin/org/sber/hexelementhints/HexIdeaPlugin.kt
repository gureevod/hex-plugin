package org.sber.hexelementhints

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

/**
 * Entry point для плагина
 */
class HexIdeaPlugin : StartupActivity {

    override fun runActivity(project: Project) {
        // Можно добавить инициализацию при старте проекта
        // Например, проверку наличия Hex Framework в зависимостях
    }
}