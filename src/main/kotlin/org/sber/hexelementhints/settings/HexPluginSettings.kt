package org.sber.hexelementhints.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*

/**
 * Persistent настройки плагина
 */
@State(
    name = "HexPluginSettings",
    storages = [Storage("hexPlugin.xml")]
)
class HexPluginSettings : PersistentStateComponent<HexPluginSettings.State> {

    data class State(
        var showInlayHints: Boolean = true,
        var showLineMarkers: Boolean = true,
        var enableInspections: Boolean = true,
        
        // Новые настройки для содержимого hint-ов
        var hintShowElementName: Boolean = true,      // @Element(name = "...")
        var hintShowLocator: Boolean = false,         // xpath или css
        var hintShowFieldType: Boolean = false,       // Button, Input, etc.
        var hintShowJavaDoc: Boolean = true,          // Описание из JavaDoc
        
        // Настройки формата отображения
        var hintMaxLocatorLength: Int = 50,           // Макс. длина локатора
        var hintSeparator: String = " • "             // Разделитель между частями
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var showInlayHints: Boolean
        get() = myState.showInlayHints
        set(value) { myState.showInlayHints = value }

    var showLineMarkers: Boolean
        get() = myState.showLineMarkers
        set(value) { myState.showLineMarkers = value }

    var enableInspections: Boolean
        get() = myState.enableInspections
        set(value) { myState.enableInspections = value }

    var hintShowElementName: Boolean
        get() = myState.hintShowElementName
        set(value) { myState.hintShowElementName = value }

    var hintShowLocator: Boolean
        get() = myState.hintShowLocator
        set(value) { myState.hintShowLocator = value }

    var hintShowFieldType: Boolean
        get() = myState.hintShowFieldType
        set(value) { myState.hintShowFieldType = value }

    var hintShowJavaDoc: Boolean
        get() = myState.hintShowJavaDoc
        set(value) { myState.hintShowJavaDoc = value }

    var hintMaxLocatorLength: Int
        get() = myState.hintMaxLocatorLength
        set(value) { myState.hintMaxLocatorLength = value }

    var hintSeparator: String
        get() = myState.hintSeparator
        set(value) { myState.hintSeparator = value }

    companion object {
        fun getInstance(): HexPluginSettings {
            return ApplicationManager.getApplication()
                .getService(HexPluginSettings::class.java)
        }
    }
}