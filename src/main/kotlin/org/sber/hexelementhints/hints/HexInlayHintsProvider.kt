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
        // Существующая логика для полей
        if (element is PsiField && HexPsiUtils.isHexElementField(element)) {
            handleFieldHint(element, sink)
        }
        
        // Новая логика для вызовов методов
        /*if (element is PsiMethodCallExpression) {
            handleMethodCallHint(element, sink)
        }*/
        
        // Новая логика для обращений к полям в коде
        if (element is PsiReferenceExpression) {
            handleReferenceHint(element, sink)
        }
        
        return true
    }

    private fun buildHintText(field: PsiField): String {
        val pluginSettings = HexPluginSettings.getInstance()
        val parts = mutableListOf<String>()

        if (pluginSettings.hintShowJavaDoc) {
            HexPsiUtils.extractRussianNameFromJavaDoc(field)?.let {
                parts.add(it)
            }
        }

        if (pluginSettings.hintShowElementName) {
            HexPsiUtils.extractNameFromAnnotation(field)?.let {
                parts.add(it)
            }
        }

        if (pluginSettings.hintShowFieldType) {
            val elementType = HexPsiUtils.getElementType(field)
            parts.add(elementType)
        }

        if (pluginSettings.hintShowLocator) {
            extractShortLocator(field)?.let {
                parts.add(it)
            }
        }

        return parts.joinToString(pluginSettings.hintSeparator)
    }

    private fun createHintPresentation(text: String): InlayPresentation {
        return factory.smallText("  // $text")
    }

    private fun extractShortLocator(field: PsiField): String? {
        val pluginSettings = HexPluginSettings.getInstance()
        val annotation = field.getAnnotation(HexPsiUtils.ELEMENT_ANNOTATION)
            ?: field.getAnnotation(HexPsiUtils.ELEMENTS_ANNOTATION)
            ?: return null

        val xpath = annotation.findAttributeValue("xpath")
            ?.let { (it as? PsiLiteralExpression)?.value as? String }

        val css = annotation.findAttributeValue("css")
            ?.let { (it as? PsiLiteralExpression)?.value as? String }

        val maxLen = pluginSettings.hintMaxLocatorLength
        return xpath?.take(maxLen)?.let { "xpath: $it${if (xpath.length > maxLen) "..." else ""}" }
            ?: css?.take(maxLen)?.let { "css: $it${if (css.length > maxLen) "..." else ""}" }
    }
    
    private fun handleFieldHint(field: PsiField, sink: InlayHintsSink) {
        val pluginSettings = HexPluginSettings.getInstance()
        if (!pluginSettings.showInlayHints) return
        
        val hints = buildHintText(field)
        if (hints.isNotEmpty()) {
            val presentation = createHintPresentation(hints)
            val offset = field.nameIdentifier?.textRange?.endOffset ?: return
            
            sink.addInlineElement(
                offset,
                relatesToPrecedingText = true,
                presentation = presentation,
                placeAtTheEndOfLine = false
            )
        }
    }
    
    private fun handleMethodCallHint(methodCall: PsiMethodCallExpression, sink: InlayHintsSink) {
        val pluginSettings = HexPluginSettings.getInstance()
        if (!pluginSettings.showInlayHints) return
        
        // Получаем qualifier (например, userPage.identityDoc)
        val qualifier = methodCall.methodExpression.qualifierExpression ?: return
        
        // Ищем PsiField, на который ссылается qualifier
        val field = resolveToHexField(qualifier) ?: return
        
        // Получаем информацию из @Element
        val elementName = HexPsiUtils.extractNameFromAnnotation(field)
        val locator = extractShortLocator(field)
        
        if (elementName != null || locator != null) {
            val hintParts = mutableListOf<String>()
            elementName?.let { hintParts.add(it) }
            
            val hint = hintParts.joinToString(" ")
            if (hint.isNotEmpty()) {
                val presentation = factory.smallText("  // $hint")
                val offset = methodCall.textRange.endOffset
                
                sink.addInlineElement(
                    offset,
                    relatesToPrecedingText = true,
                    presentation = presentation,
                    placeAtTheEndOfLine = true
                )
            }
        }
    }
    
    private fun handleReferenceHint(ref: PsiReferenceExpression, sink: InlayHintsSink) {
        val pluginSettings = HexPluginSettings.getInstance()
        if (!pluginSettings.showInlayHints) return
        
        // Проверяем, что это обращение к полю элемента (не само определение)
        val resolved = ref.resolve() as? PsiField ?: return
        if (!HexPsiUtils.isHexElementField(resolved)) return
        
        // Проверяем, что это не определение поля (PsiDeclarationStatement)
        if (ref.parent is PsiField) return
        
        val elementName = HexPsiUtils.extractNameFromAnnotation(resolved)
            ?: HexPsiUtils.extractRussianNameFromJavaDoc(resolved)
            ?: return
        
        // Показываем hint только если это chain call (userPage.field.method())
        val parent = ref.parent
        if (parent is PsiReferenceExpression && parent.qualifierExpression == ref) {
            val presentation = factory.smallText(" /* $elementName */")
            val offset = ref.textRange.endOffset
            
            sink.addInlineElement(
                offset,
                relatesToPrecedingText = true,
                presentation = presentation,
                placeAtTheEndOfLine = false
            )
        }
    }
    
    private fun resolveToHexField(expr: PsiExpression): PsiField? {
        return when (expr) {
            is PsiReferenceExpression -> {
                val resolved = expr.resolve()
                if (resolved is PsiField && HexPsiUtils.isHexElementField(resolved)) {
                    resolved
                } else null
            }
            else -> null
        }
    }
}