package org.sber.hexelementhints.ai

/**
 * Извлекает код из ответа LLM, очищая от markdown разметки.
 */
object CodeExtractor {

    private val CODE_BLOCK_REGEX = Regex(
        """```(?:java|kotlin)?\s*\n?(.*?)```""",
        RegexOption.DOT_MATCHES_ALL
    )

    private val CLEAN_CODE_MARKERS = listOf(
        "```java",
        "```kotlin",
        "```",
    )

    /**
     * Извлекает Java/Kotlin код из ответа LLM.
     * Удаляет markdown разметку и лишние пробелы.
     */
    fun extractCode(response: String): String {
        // Пробуем найти блок кода в markdown
        val match = CODE_BLOCK_REGEX.find(response)
        if (match != null) {
            return match.groupValues[1].trim()
        }

        // Если нет markdown блока, очищаем от маркеров вручную
        var code = response.trim()
        for (marker in CLEAN_CODE_MARKERS) {
            code = code.removePrefix(marker).removeSuffix(marker)
        }

        return code.trim()
    }

    /**
     * Проверяет, похож ли текст на Java класс.
     */
    fun isValidJavaClass(code: String): Boolean {
        val hasClassDeclaration = code.contains(Regex("""(public\s+)?class\s+\w+"""))
        val hasPackageOrImport = code.contains("package ") || code.contains("import ")
        val hasAnnotation = code.contains("@Page") || code.contains("@Component") || code.contains("@Element")
        
        return hasClassDeclaration && (hasPackageOrImport || hasAnnotation)
    }

    /**
     * Извлекает имя класса из кода.
     */
    fun extractClassName(code: String): String? {
        val classMatch = Regex("""class\s+(\w+)""").find(code)
        return classMatch?.groupValues?.get(1)
    }

    /**
     * Добавляет package declaration если отсутствует.
     */
    fun ensurePackage(code: String, packageName: String): String {
        if (code.trimStart().startsWith("package ")) {
            return code
        }
        return "package $packageName;\n\n$code"
    }

    /**
     * Форматирует код для отображения (добавляет отступы).
     */
    fun formatForDisplay(code: String): String {
        // Базовое форматирование - можно расширить
        return code.lines()
            .joinToString("\n") { it.trimEnd() }
            .trim()
    }
}
