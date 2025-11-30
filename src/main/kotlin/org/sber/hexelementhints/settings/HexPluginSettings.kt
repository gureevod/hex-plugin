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
        var enableInspections: Boolean = true
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

    companion object {
        fun getInstance(): HexPluginSettings {
            return ApplicationManager.getApplication()
                .getService(HexPluginSettings::class.java)
        }
    }
}