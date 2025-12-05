package org.sber.hexelementhints.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.sber.hexelementhints.utils.HexIcons
import org.sber.hexelementhints.utils.HexPsiUtils

/**
 * Умный автокомплит: при вводе текста ищет поля по имени из @Element(name="...")
 * 
 * Пример: userPage.пас[курсор] → предложит password, если есть @Element(name="Пароль")
 * 
 * Ключевые особенности:
 * 1. Создаём кастомный PrefixMatcher, который ищет по всем lookup strings (включая русские названия)
 * 2. Добавляем все элементы, фильтрация происходит через PrefixMatcher
 * 3. Приоритет выше обычного автодополнения для Hex элементов
 */
class HexElementNameCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(PsiReferenceExpression::class.java),
            HexElementNameCompletionProvider()
        )
    }
}

class HexElementNameCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val parent = position.parent
        
        // Проверяем, что мы в контексте обращения к полю класса (page.field)
        if (parent !is PsiReferenceExpression) return
        val qualifier = parent.qualifierExpression ?: return
        val qualifierType = qualifier.type as? PsiClassType ?: return
        val psiClass = qualifierType.resolve() ?: return
        
        // Проверяем, что это Page или Component
        if (!HexPsiUtils.isPageClass(psiClass) && !HexPsiUtils.isComponentClass(psiClass)) {
            return
        }
        
        // Получаем текст, который пользователь вводит (без учёта dummy identifier)
        val prefix = extractRealPrefix(parameters)
        
        // Создаём кастомный ResultSet с matcher'ом, который понимает русские названия
        val customMatcher = HexElementPrefixMatcher(prefix)
        val customResult = result.withPrefixMatcher(customMatcher)
        
        // Добавляем все Hex элементы - фильтрация произойдёт через PrefixMatcher
        psiClass.allFields
            .filter { HexPsiUtils.isHexElementField(it) }
            .forEach { field ->
                val elementName = HexPsiUtils.extractNameFromAnnotation(field)
                val javaDocName = HexPsiUtils.extractRussianNameFromJavaDoc(field)
                
                val lookupElement = createLookupElement(field, elementName, javaDocName)
                customResult.addElement(PrioritizedLookupElement.withPriority(lookupElement, 100.0))
            }
    }
    
    /**
     * Извлекает реальный prefix, который ввёл пользователь.
     * IntelliJ вставляет dummy identifier (IntellijIdeaRulezzz) в позицию курсора,
     * поэтому нужно его отфильтровать.
     */
    private fun extractRealPrefix(parameters: CompletionParameters): String {
        val position = parameters.position
        val text = position.text ?: return ""
        
        // Убираем dummy identifier
        val cleanText = text.replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "")
            .replace(CompletionUtilCore.DUMMY_IDENTIFIER, "")
            .trim()
        
        return cleanText
    }

    private fun createLookupElement(
        field: PsiField, 
        elementName: String?,
        javaDocName: String?
    ): LookupElementBuilder {
        val displayName = elementName ?: javaDocName
        val elementType = HexPsiUtils.getElementType(field)
        
        var builder = LookupElementBuilder.create(field, field.name)
            .withIcon(HexIcons.ELEMENT)
            .withTypeText(elementType, true)
            .bold()
        
        // Добавляем название как tail text для отображения
        if (displayName != null) {
            builder = builder.withTailText(" — $displayName", true)
        }
        
        // Добавляем lookup strings для поиска по русскому названию
        if (elementName != null) {
            builder = builder.withLookupString(elementName)
        }
        if (javaDocName != null && javaDocName != elementName) {
            builder = builder.withLookupString(javaDocName)
        }
        
        return builder
    }
}

/**
 * Кастомный PrefixMatcher, который ищет совпадения по всем lookup strings элемента,
 * включая русские названия из @Element(name="...") и JavaDoc.
 * 
 * Поддерживает:
 * - Поиск по имени поля (password)
 * - Поиск по русскому названию (Пароль)
 * - Case-insensitive поиск
 * - Поиск по подстроке (пар → Пароль)
 */
class HexElementPrefixMatcher(prefix: String) : PrefixMatcher(prefix) {
    
    private val lowercasePrefix = prefix.lowercase()
    
    override fun prefixMatches(name: String): Boolean {
        if (lowercasePrefix.isEmpty()) return true
        return name.lowercase().contains(lowercasePrefix)
    }
    
    override fun prefixMatches(element: com.intellij.codeInsight.lookup.LookupElement): Boolean {
        // Проверяем основной lookup string
        if (prefixMatches(element.lookupString)) return true
        
        // Проверяем все дополнительные lookup strings (включая русские названия)
        return element.allLookupStrings.any { prefixMatches(it) }
    }
    
    override fun cloneWithPrefix(prefix: String): PrefixMatcher {
        return HexElementPrefixMatcher(prefix)
    }
}