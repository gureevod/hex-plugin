package org.sber.hexelementhints.utils

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * Иконки для Hex Framework
 */
object HexIcons {

    @JvmStatic
    val HEX = load("/icons/hex-icon.svg")

    @JvmStatic
    val ELEMENT = load("/icons/element-icon.svg")

    @JvmStatic
    val COMPONENT = load("/icons/component-icon.svg")

    @JvmStatic
    val PAGE = load("/icons/page-icon.svg")

    private fun load(path: String): Icon {
        return IconLoader.getIcon(path, HexIcons::class.java)
    }
}