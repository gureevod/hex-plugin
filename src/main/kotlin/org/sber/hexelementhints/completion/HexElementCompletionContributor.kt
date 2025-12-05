package org.sber.hexelementhints.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.sber.hexelementhints.utils.HexIcons
import org.sber.hexelementhints.utils.HexPsiUtils

/**
 * Contributor для автодополнения Hex элементов с отображением русских названий.
 * 
 * Этот contributor показывает все Hex элементы при стандартном автодополнении
 * (page.[Ctrl+Space]) с отображением русских названий и локаторов.
 * 
 * Для поиска по русским названиям используется HexElementNameCompletionContributor.
 */
class HexElementCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(PsiIdentifier::class.java),
            HexElementCompletionProvider()
        )
    }
}

class HexElementCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val parent = position.parent

        // Проверяем, что мы в контексте вызова метода/обращения к полю
        if (parent !is PsiReferenceExpression) return

        val qualifier = parent.qualifierExpression ?: return
        val qualifierType = qualifier.type as? PsiClassType ?: return
        val psiClass = qualifierType.resolve() ?: return

        // Проверяем, что это Page или Component
        if (!HexPsiUtils.isPageClass(psiClass) && !HexPsiUtils.isComponentClass(psiClass)) {
            return
        }

        // Добавляем все поля с русскими названиями
        psiClass.allFields
            .filter { HexPsiUtils.isHexElementField(it) }
            .forEach { field ->
                val lookupElement = createLookupElement(field)
                // Повышаем приоритет Hex элементов
                result.addElement(PrioritizedLookupElement.withPriority(lookupElement, 50.0))
            }
    }

    private fun createLookupElement(field: PsiField): LookupElementBuilder {
        val fieldName = field.name
        val russianName = HexPsiUtils.extractElementName(field)
        val elementType = HexPsiUtils.getElementType(field)

        var builder = LookupElementBuilder.create(field, fieldName)
            .withIcon(HexIcons.ELEMENT)
            .withTypeText(elementType, true)
            .bold()

        // Добавляем русское название как tail text
        if (russianName != null) {
            builder = builder.withTailText(" — $russianName", true)
            // Добавляем lookup string для возможности поиска по русскому названию
            builder = builder.withLookupString(russianName)
        }

        // Добавляем информацию о локаторе
        val locator = extractLocator(field)
        if (locator != null) {
            builder = builder.withTypeText("$elementType • $locator", true)
        }

        return builder
    }

    private fun extractLocator(field: PsiField): String? {
        val annotation = field.getAnnotation(HexPsiUtils.ELEMENT_ANNOTATION)
            ?: field.getAnnotation(HexPsiUtils.ELEMENTS_ANNOTATION)
            ?: return null

        val xpath = annotation.findAttributeValue("xpath")
            ?.let { (it as? PsiLiteralExpression)?.value as? String }

        val css = annotation.findAttributeValue("css")
            ?.let { (it as? PsiLiteralExpression)?.value as? String }

        return xpath?.let { "xpath: ${it.take(50)}" }
            ?: css?.let { "css: ${it.take(50)}" }
    }
}