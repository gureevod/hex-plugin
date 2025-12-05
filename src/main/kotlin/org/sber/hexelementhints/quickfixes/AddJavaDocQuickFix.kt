package org.sber.hexelementhints.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import org.sber.hexelementhints.utils.HexPsiUtils

/**
 * Quick fix для автоматического добавления JavaDoc
 */
class AddJavaDocQuickFix : LocalQuickFix {

    override fun getFamilyName(): String = "Add JavaDoc with Russian name"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val field = descriptor.psiElement as? PsiField ?: return

        // Получаем английское название из аннотации как основу
        val englishName = HexPsiUtils.extractNameFromAnnotation(field) ?: field.name

        // Создаем JavaDoc
        val factory = JavaPsiFacade.getElementFactory(project)
        val docComment = factory.createDocCommentFromText(
            """
            /**
             * TODO: Добавить русское название для '$englishName'
             */
            """.trimIndent()
        )

        // Добавляем перед полем
        val addedComment = field.parent.addBefore(docComment, field)

        // Автоматическое форматирование (включая переносы строк)
        val codeStyleManager = CodeStyleManager.getInstance(project)
        codeStyleManager.reformat(field.parent)
    }
}