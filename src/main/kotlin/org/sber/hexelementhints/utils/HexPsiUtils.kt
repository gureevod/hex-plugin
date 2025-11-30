package org.sber.hexelementhints.utils

import com.intellij.psi.*
import com.intellij.psi.javadoc.PsiDocToken
import com.intellij.psi.util.PsiTreeUtil

/**
 * Утилиты для работы с PSI элементами Hex Framework
 */
object HexPsiUtils {

    const val ELEMENT_ANNOTATION = "com.company.hex.ui.annotations.Element"
    const val ELEMENTS_ANNOTATION = "com.company.hex.ui.annotations.Elements"
    const val COMPONENT_ANNOTATION = "com.company.hex.ui.annotations.Component"
    const val PAGE_ANNOTATION = "com.company.hex.ui.annotations.Page"

    const val BASE_PAGE_CLASS = "com.company.hex.ui.pages.BasePage"
    const val BASE_COMPONENT_CLASS = "com.company.hex.ui.components.BaseComponent"
    const val BASE_ELEMENT_CLASS = "com.company.hex.ui.elements.BaseElement"

    /**
     * Проверяет, является ли класс Page Object
     */
    fun isPageClass(psiClass: PsiClass): Boolean {
        return psiClass.hasAnnotation(PAGE_ANNOTATION) ||
                psiClass.isInheritor(BASE_PAGE_CLASS)
    }

    /**
     * Проверяет, является ли класс Component
     */
    fun isComponentClass(psiClass: PsiClass): Boolean {
        return psiClass.hasAnnotation(COMPONENT_ANNOTATION) ||
                psiClass.isInheritor(BASE_COMPONENT_CLASS)
    }

    /**
     * Проверяет, является ли поле элементом Hex
     */
    fun isHexElementField(field: PsiField): Boolean {
        return field.hasAnnotation(ELEMENT_ANNOTATION) ||
                field.hasAnnotation(ELEMENTS_ANNOTATION) ||
                field.type.isHexElement()
    }

    /**
     * Извлекает русское название из JavaDoc
     */
    fun extractRussianNameFromJavaDoc(field: PsiField): String? {
        val docComment = field.docComment ?: return null

        // Берем первую непустую строку описания
        val description = docComment.descriptionElements
            .filterIsInstance<PsiDocToken>()
            .firstOrNull { it.text.trim().isNotEmpty() }
            ?.text
            ?.trim()

        return description?.takeIf { it.isNotBlank() }
    }

    /**
     * Извлекает значение атрибута name из аннотации @Element
     */
    fun extractNameFromAnnotation(field: PsiField): String? {
        val annotation = field.getAnnotation(ELEMENT_ANNOTATION)
            ?: field.getAnnotation(ELEMENTS_ANNOTATION)
            ?: return null

        return annotation.findAttributeValue("name")
            ?.let { it as? PsiLiteralExpression }
            ?.value as? String
    }

    /**
     * Извлекает название из builder API (.withName("..."))
     */
    fun extractNameFromBuilder(field: PsiField): String? {
        val initializer = field.initializer as? PsiMethodCallExpression ?: return null
        return findWithNameCall(initializer)
    }

    private fun findWithNameCall(expr: PsiMethodCallExpression?): String? {
        var current = expr
        while (current != null) {
            if (current.methodExpression.referenceName == "withName") {
                val arg = current.argumentList.expressions.firstOrNull()
                return (arg as? PsiLiteralExpression)?.value as? String
            }
            current = current.methodExpression.qualifierExpression as? PsiMethodCallExpression
        }
        return null
    }

    /**
     * Извлекает любое доступное название элемента
     * Приоритет: JavaDoc → @Element(name) → .withName()
     */
    fun extractElementName(field: PsiField): String? {
        return extractRussianNameFromJavaDoc(field)
            ?: extractNameFromAnnotation(field)
            ?: extractNameFromBuilder(field)
    }

    /**
     * Определяет тип элемента (Button, Input, etc.)
     */
    fun getElementType(field: PsiField): String {
        val type = field.type
        return when {
            type.canonicalText.contains("Button") -> "Button"
            type.canonicalText.contains("Input") -> "Input"
            type.canonicalText.contains("Checkbox") -> "Checkbox"
            type.canonicalText.contains("Select") -> "Select"
            type.canonicalText.contains("TextElement") -> "TextElement"
            type.canonicalText.contains("ElementList") -> "ElementList"
            else -> "Element"
        }
    }

    /**
     * Находит родительский Page или Component класс
     */
    fun findParentPageOrComponent(element: PsiElement): PsiClass? {
        return PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
            ?.takeIf { isPageClass(it) || isComponentClass(it) }
    }

    /**
     * Проверяет, наследуется ли класс от указанного базового класса
     */
    private fun PsiClass.isInheritor(baseClassName: String): Boolean {
        return this.superClass?.qualifiedName == baseClassName ||
                this.superClass?.isInheritor(baseClassName) == true
    }

    /**
     * Проверяет, является ли тип элементом Hex
     */
    private fun PsiType.isHexElement(): Boolean {
        val canonical = this.canonicalText
        return canonical.startsWith("com.company.hex.ui.elements.") ||
                canonical.contains("ElementList")
    }

    private fun PsiModifierListOwner.hasAnnotation(fqn: String): Boolean {
        return this.getAnnotation(fqn) != null
    }
}