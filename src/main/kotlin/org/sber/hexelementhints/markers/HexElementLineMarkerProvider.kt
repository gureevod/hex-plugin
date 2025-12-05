package org.sber.hexelementhints.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import org.sber.hexelementhints.utils.HexIcons
import org.sber.hexelementhints.utils.HexPsiUtils

/**
 * Line markers для визуального выделения Hex элементов
 */
class HexElementLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiField) return null
        if (!HexPsiUtils.isHexElementField(element)) return null

        val identifier = element.nameIdentifier ?: return null
        val russianName = HexPsiUtils.extractElementName(element) ?: element.name
        val elementType = HexPsiUtils.getElementType(element)

        return LineMarkerInfo(
            identifier,
            identifier.textRange,
            HexIcons.ELEMENT,
            { "$elementType: $russianName" },
            null,
            GutterIconRenderer.Alignment.LEFT,
            { "$elementType: $russianName" }
        )
    }
}