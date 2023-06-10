/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

object CirClassRecursionMarker : CirClass, CirRecursionMarker {
    override konst annotations get() = unsupported()
    override konst name get() = unsupported()
    override konst typeParameters get() = unsupported()
    override konst visibility get() = unsupported()
    override konst modality get() = unsupported()
    override konst kind get() = unsupported()
    override var companion: CirName?
        get() = unsupported()
        set(_) = unsupported()
    override konst isCompanion get() = unsupported()
    override konst isData get() = unsupported()
    override konst isValue get() = unsupported()
    override konst isInner get() = unsupported()
    override konst hasEnumEntries: Boolean get() = unsupported()
    override var supertypes: List<CirType>
        get() = unsupported()
        set(_) = unsupported()
}

object CirTypeAliasRecursionMarker : CirTypeAlias, CirRecursionMarker {
    override konst underlyingType: CirClassOrTypeAliasType get() = unsupported()
    override konst expandedType: CirClassType get() = unsupported()
    override konst annotations get() = unsupported()
    override konst name get() = unsupported()
    override konst typeParameters get() = unsupported()
    override konst visibility get() = unsupported()
    override konst isLiftedUp: Boolean get() = unsupported()
}
