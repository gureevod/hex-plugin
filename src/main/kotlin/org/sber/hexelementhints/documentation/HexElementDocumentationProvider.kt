package org.sber.hexelementhints.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.Editor
import com.intellij.psi.*
import org.sber.hexelementhints.utils.HexPsiUtils

/**
 * Provides rich documentation для Hex элементов
 */
class HexElementDocumentationProvider : AbstractDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element !is PsiField) return null
        if (!HexPsiUtils.isHexElementField(element)) return null

        return buildDocumentation(element)
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element !is PsiField) return null
        if (!HexPsiUtils.isHexElementField(element)) return null

        val russianName = HexPsiUtils.extractElementName(element)
        val elementType = HexPsiUtils.getElementType(element)

        return buildString {
            append("<b>${element.name}</b>")
            if (russianName != null) {
                append(" — $russianName")
            }
            append(" : $elementType")
        }
    }

    private fun buildDocumentation(field: PsiField): String {
        return buildString {
            append(DocumentationMarkup.DEFINITION_START)
            append("<b>${field.name}</b> : ${HexPsiUtils.getElementType(field)}")
            append(DocumentationMarkup.DEFINITION_END)

            append(DocumentationMarkup.CONTENT_START)

            // Русское название
            val russianName = HexPsiUtils.extractRussianNameFromJavaDoc(field)
            if (russianName != null) {
                append("<p><b>Описание:</b> $russianName</p>")
            }

            // English name из аннотации
            val englishName = HexPsiUtils.extractNameFromAnnotation(field)
            if (englishName != null) {
                append("<p><b>Name:</b> $englishName</p>")
            }

            // Локатор
            val locatorInfo = getLocatorInfo(field)
            if (locatorInfo != null) {
                append("<p><b>Локатор:</b><br/>")
                append("<code>$locatorInfo</code></p>")
            }

            // Дополнительная информация из аннотации
            val additionalInfo = getAdditionalAnnotationInfo(field)
            if (additionalInfo.isNotEmpty()) {
                append("<p><b>Параметры:</b></p>")
                append("<ul>")
                additionalInfo.forEach { (key, value) ->
                    append("<li><b>$key:</b> $value</li>")
                }
                append("</ul>")
            }

            // Родительский класс
            val parentClass = HexPsiUtils.findParentPageOrComponent(field)
            if (parentClass != null) {
                val parentType = when {
                    HexPsiUtils.isPageClass(parentClass) -> "Page"
                    HexPsiUtils.isComponentClass(parentClass) -> "Component"
                    else -> "Class"
                }
                append("<p><b>$parentType:</b> ${parentClass.name}</p>")
            }

            append(DocumentationMarkup.CONTENT_END)
        }
    }

    private fun getLocatorInfo(field: PsiField): String? {
        val annotation = field.getAnnotation(HexPsiUtils.ELEMENT_ANNOTATION)
            ?: field.getAnnotation(HexPsiUtils.ELEMENTS_ANNOTATION)
            ?: return null

        val xpath = annotation.findAttributeValue("xpath")
            ?.let { (it as? PsiLiteralExpression)?.value as? String }

        val css = annotation.findAttributeValue("css")
            ?.let { (it as? PsiLiteralExpression)?.value as? String }

        return xpath?.let { "xpath = \"$it\"" }
            ?: css?.let { "css = \"$it\"" }
    }

    private fun getAdditionalAnnotationInfo(field: PsiField): Map<String, String> {
        val annotation = field.getAnnotation(HexPsiUtils.ELEMENT_ANNOTATION)
            ?: field.getAnnotation(HexPsiUtils.ELEMENTS_ANNOTATION)
            ?: return emptyMap()

        val info = mutableMapOf<String, String>()

        annotation.findAttributeValue("timeout")
            ?.let { (it as? PsiLiteralExpression)?.value }
            ?.let { info["timeout"] = "$it ms" }

        annotation.findAttributeValue("pollingInterval")
            ?.let { (it as? PsiLiteralExpression)?.value }
            ?.let { info["pollingInterval"] = "$it ms" }

        return info
    }
}