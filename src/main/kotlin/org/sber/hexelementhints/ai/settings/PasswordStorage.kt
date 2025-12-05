package org.sber.hexelementhints.ai.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

/**
 * Безопасное хранение пароля от сертификата через IntelliJ PasswordSafe.
 */
object PasswordStorage {
    private const val SUBSYSTEM = "HexAiPlugin"
    private const val KEY = "certPassword"

    private fun createCredentialAttributes(): CredentialAttributes {
        return CredentialAttributes(
            generateServiceName(SUBSYSTEM, KEY)
        )
    }

    /**
     * Сохраняет пароль в защищённое хранилище
     */
    fun savePassword(password: String) {
        val credentialAttributes = createCredentialAttributes()
        val credentials = Credentials(KEY, password)
        PasswordSafe.instance.set(credentialAttributes, credentials)
    }

    /**
     * Получает пароль из защищённого хранилища
     */
    fun getPassword(): String? {
        val credentialAttributes = createCredentialAttributes()
        return PasswordSafe.instance.getPassword(credentialAttributes)
    }

    /**
     * Удаляет пароль из хранилища
     */
    fun clearPassword() {
        val credentialAttributes = createCredentialAttributes()
        PasswordSafe.instance.set(credentialAttributes, null)
    }

    /**
     * Проверяет, сохранён ли пароль
     */
    fun hasPassword(): Boolean {
        return getPassword() != null
    }
}
