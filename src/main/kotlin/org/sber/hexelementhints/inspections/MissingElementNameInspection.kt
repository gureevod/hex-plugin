package org.sber.hexelementhints.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiField
import com.intellij.psi.JavaElementVisitor
import org.sber.hexelementhints.utils.HexPsiUtils

/**
 * Проверяет наличие атрибута name в аннотации @Element
 */
class MissingElementNameInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitField(field: PsiField) {
                super.visitField(field)

                val annotation = field.getAnnotation(HexPsiUtils.ELEMENT_ANNOTATION)
                    ?: field.getAnnotation(HexPsiUtils.ELEMENTS_ANNOTATION)
                    ?: return

                val nameAttribute = annotation.findAttributeValue("name")
                if (nameAttribute == null) {
                    holder.registerProblem(
                        annotation,
                        "Атрибут 'name' обязателен для Allure steps и логирования",
                        ProblemHighlightType.ERROR
                    )
                }
            }
        }
    }
}