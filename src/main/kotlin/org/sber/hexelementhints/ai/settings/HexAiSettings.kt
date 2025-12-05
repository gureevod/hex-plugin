package org.sber.hexelementhints.ai.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*

/**
 * Application-level настройки для AI интеграции.
 * Хранит путь к сертификату, endpoint API и имя модели.
 */
@State(
    name = "HexAiSettings",
    storages = [Storage("hex-ai.xml")]
)
class HexAiSettings : PersistentStateComponent<HexAiSettings.State> {

    data class State(
        var certPath: String = "",
        var apiEndpoint: String = "https://api.openai.com/v1",
        var modelName: String = "gpt-4",
        var savePassword: Boolean = true
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var certPath: String
        get() = myState.certPath
        set(value) { myState.certPath = value }

    var apiEndpoint: String
        get() = myState.apiEndpoint
        set(value) { myState.apiEndpoint = value }

    var modelName: String
        get() = myState.modelName
        set(value) { myState.modelName = value }

    var savePassword: Boolean
        get() = myState.savePassword
        set(value) { myState.savePassword = value }

    /**
     * Проверяет, настроено ли подключение (есть путь к сертификату)
     */
    fun isConfigured(): Boolean {
        return certPath.isNotBlank()
    }

    companion object {
        fun getInstance(): HexAiSettings {
            return ApplicationManager.getApplication()
                .getService(HexAiSettings::class.java)
        }
    }
}
