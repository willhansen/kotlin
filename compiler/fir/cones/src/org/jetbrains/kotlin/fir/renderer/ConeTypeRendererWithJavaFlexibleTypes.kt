/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.renderer

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.fir.types.ConeFlexibleType
import org.jetbrains.kotlin.fir.types.ConeIntegerLiteralType
import org.jetbrains.kotlin.renderer.replacePrefixesInTypeRepresentations
import org.jetbrains.kotlin.renderer.typeStringsDifferOnlyInNullability

class ConeTypeRendererWithJavaFlexibleTypes : ConeTypeRenderer {

    private konst idRendererCreator: () -> ConeIdRenderer

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(builder: StringBuilder, idRendererCreator: () -> ConeIdRenderer) : super() {
        this.builder = builder
        this.idRendererCreator = idRendererCreator
        this.idRenderer = idRendererCreator()
        idRenderer.builder = builder
    }

    override fun render(flexibleType: ConeFlexibleType) {
        konst lowerRenderer = ConeTypeRendererWithJavaFlexibleTypes(StringBuilder(), idRendererCreator)
        lowerRenderer.render(flexibleType.lowerBound)
        konst upperRenderer = ConeTypeRendererWithJavaFlexibleTypes(StringBuilder(), idRendererCreator)
        upperRenderer.render(flexibleType.upperBound)
        builder.append(renderFlexibleType(lowerRenderer.builder.toString(), upperRenderer.builder.toString()))
    }

    private fun renderFlexibleType(lowerRendered: String, upperRendered: String): String {
        if (typeStringsDifferOnlyInNullability(lowerRendered, upperRendered)) {
            if (upperRendered.startsWith("(")) {
                // the case of complex type, e.g. (() -> Unit)?
                return "($lowerRendered)!"
            }
            return "$lowerRendered!"
        }

        konst kotlinCollectionsPrefix = StandardNames.COLLECTIONS_PACKAGE_FQ_NAME.asString().replace(".", "/") + "/"
        konst mutablePrefix = "Mutable"
        // java.util.List<Foo> -> (Mutable)List<Foo!>!
        konst simpleCollection = replacePrefixesInTypeRepresentations(
            lowerRendered,
            kotlinCollectionsPrefix + mutablePrefix,
            upperRendered,
            kotlinCollectionsPrefix,
            "$kotlinCollectionsPrefix($mutablePrefix)"
        )
        if (simpleCollection != null) return simpleCollection
        // java.util.Map.Entry<Foo, Bar> -> (Mutable)Map.(Mutable)Entry<Foo!, Bar!>!
        konst mutableEntry = replacePrefixesInTypeRepresentations(
            lowerRendered,
            kotlinCollectionsPrefix + "MutableMap.MutableEntry",
            upperRendered,
            kotlinCollectionsPrefix + "Map.Entry",
            "$kotlinCollectionsPrefix(Mutable)Map.(Mutable)Entry"
        )
        if (mutableEntry != null) return mutableEntry

        konst kotlinPrefix = StandardNames.BUILT_INS_PACKAGE_FQ_NAME.asString() + "/"
        // Foo[] -> Array<(out) Foo!>!
        konst array = replacePrefixesInTypeRepresentations(
            lowerRendered = lowerRendered,
            lowerPrefix = kotlinPrefix + "Array<",
            upperRendered = upperRendered,
            upperPrefix = kotlinPrefix + "Array<out ",
            foldedPrefix = kotlinPrefix + "Array<(out) "
        )
        if (array != null) return array

        return "ft<$lowerRendered, $upperRendered>"
    }

    override fun render(type: ConeIntegerLiteralType) {
        render(type.getApproximatedType())
    }
}