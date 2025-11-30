package org.sber.hexelementhints.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.*
import org.sber.hexelementhints.settings.HexPluginSettings
import org.sber.hexelementhints.utils.HexPsiUtils
import javax.swing.JPanel

/**
 * Inlay hints с русскими названиями рядом с полями
 */
@Suppress("UnstableApiUsage")
class HexInlayHintsProvider : InlayHintsProvider<HexInlayHintsProvider.Settings> {

    override val key: SettingsKey<Settings> = SettingsKey("hex.element.hints")

    override val name: String = "Hex Element Names"

    override val previewText: String = """
        @Page(url = "/login", title = "Login Page")
        public class LoginPage extends BasePage {
            @Element(name = "Username", xpath = "//input[@id='username']")
            Input username;
            
            @Element(name = "Login Button", xpath = "//button[@type='submit']")
            Button loginButton;
        }
    """.trimIndent()

    override fun createSettings(): Settings = Settings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: Settings,
        sink: InlayHintsSink
    ): InlayHintsCollector {
        return HexInlayHintsCollector(editor, settings)
    }

    override fun createConfigurable(settings: Settings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JPanel {
                return JPanel() // Простая панель настроек
            }
        }
    }

    data class Settings(
        var showRussianNames: Boolean = true,
        var showEnglishNames: Boolean = false,
        var showLocators: Boolean = false
    )
}

@Suppress("UnstableApiUsage")
class HexInlayHintsCollector(
    editor: Editor,
    private val settings: HexInlayHintsProvider.Settings
) : FactoryInlayHintsCollector(editor) {

    override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
        if (element !is PsiField) return true
        if (!HexPsiUtils.isHexElementField(element)) return true

        // Проверяем настройки плагина
        val pluginSettings = HexPluginSettings.getInstance()
        if (!pluginSettings.showInlayHints) return true

        val hints = buildHintText(element)
        if (hints.isNotEmpty()) {
            val presentation = createHintPresentation(hints)
            val offset = element.nameIdentifier?.textRange?.endOffset ?: return true

            sink.addInlineElement(
                offset,
                relatesToPrecedingText = true,
                presentation = presentation,
                placeAtTheEndOfLine = false
            )
        }

        return true
    }

    private fun buildHintText(field: PsiField): String {
        val parts = mutableListOf<String>()

        if (settings.showRussianNames) {
            HexPsiUtils.extractRussianNameFromJavaDoc(field)?.let {
                parts.add(it)
            }
        }

        if (settings.showEnglishNames) {
            HexPsiUtils.extractNameFromAnnotation(field)?.let {
                parts.add(it)
            }
        }

        if (settings.showLocators) {
            extractShortLocator(field)?.let {
                parts.add(it)
            }
        }

        return parts.joinToString(" • ")
    }

    private fun createHintPresentation(text: String): InlayPresentation {
        return factory.smallText("  // $text")
    }

    private fun extractShortLocator(field: PsiField): String? {
        val annotation = field.getAnnotation(HexPsiUtils.ELEMENT_ANNOTATION)
            ?: field.getAnnotation(HexPsiUtils.ELEMENTS_ANNOTATION)
            ?: return null

        val xpath = annotation.findAttributeValue("xpath")
            ?.let { (it as? PsiLiteralExpression)?.value as? String }

        val css = annotation.findAttributeValue("css")
            ?.let { (it as? PsiLiteralExpression)?.value as? String }

        return xpath?.take(30)?.let { "xpath: $it..." }
            ?: css?.take(30)?.let { "css: $it..." }
    }
}