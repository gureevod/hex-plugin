package org.sber.hexelementhints.inspections

import org.sber.hexelementhints.quickfixes.AddJavaDocQuickFix
import com.intellij.codeInspection.*
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiField
import com.intellij.psi.JavaElementVisitor
import org.sber.hexelementhints.utils.HexPsiUtils

/**
 * Проверяет наличие русского названия в JavaDoc
 */
class MissingRussianNameInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitField(field: PsiField) {
                super.visitField(field)

                if (!HexPsiUtils.isHexElementField(field)) return

                val russianName = HexPsiUtils.extractRussianNameFromJavaDoc(field)
                if (russianName == null) {
                    holder.registerProblem(
                        field.nameIdentifier ?: field,
                        "Отсутствует русское название в JavaDoc",
                        ProblemHighlightType.WARNING,
                        AddJavaDocQuickFix()
                    )
                }
            }
        }
    }
}