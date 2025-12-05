package org.sber.hexelementhints.ai

/**
 * Построитель промптов для генерации PageObject из HTML.
 */
object PromptBuilder {

    private val SYSTEM_PROMPT = """
        Ты — эксперт по созданию PageObject классов для UI тестирования с использованием Hex UI Framework.
        
        Hex UI Framework использует следующие аннотации:
        - @Page(url = "...", title = "...") — для классов страниц
        - @Component(name = "...") — для компонентов (переиспользуемых блоков)
        - @Element(name = "...", css = "..." или xpath = "...") — для элементов
        
        Типы элементов:
        - Button — кнопки
        - Input — текстовые поля, textarea
        - Checkbox — чекбоксы
        - Radio — радиокнопки
        - Select — выпадающие списки
        - Link — ссылки
        - Text — текстовые блоки, метки
        - Image — изображения
        - Table — таблицы
        - Container — контейнеры, div-ы с вложенностью
        
        Правила генерации:
        1. Имена полей должны быть на английском в camelCase
        2. Атрибут name в @Element — краткое описание на русском
        3. Предпочитай CSS селекторы вместо XPath когда возможно
        4. Используй data-testid, id, name атрибуты как основу для селекторов
        5. Добавляй JavaDoc с описанием элемента на русском
        6. Группируй элементы логически (поля формы, кнопки, навигация)
        7. Класс должен наследоваться от BasePage или BaseComponent
        
        Формат ответа: только Java код класса, без пояснений и markdown разметки.
    """.trimIndent()

    /**
     * Создаёт промпт для генерации PageObject из HTML.
     */
    fun buildPageObjectPrompt(html: String, additionalInstructions: String? = null): String {
        val userPrompt = StringBuilder()
        
        userPrompt.append("Сгенерируй PageObject класс для следующего HTML:\n\n")
        userPrompt.append("```html\n")
        userPrompt.append(html.take(15000)) // Ограничиваем размер HTML
        userPrompt.append("\n```\n")
        
        if (!additionalInstructions.isNullOrBlank()) {
            userPrompt.append("\nДополнительные требования:\n")
            userPrompt.append(additionalInstructions)
        }
        
        return userPrompt.toString()
    }

    /**
     * Возвращает системный промпт для генерации PageObject.
     */
    fun getSystemPrompt(): String = SYSTEM_PROMPT

    /**
     * Создаёт полный промпт с системной частью.
     */
    fun buildFullPrompt(html: String, additionalInstructions: String? = null): String {
        return """
            |$SYSTEM_PROMPT
            |
            |---
            |
            |${buildPageObjectPrompt(html, additionalInstructions)}
        """.trimMargin()
    }
}
