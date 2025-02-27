/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.symbols

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.model.TypeConstructorMarker

abstract class ConeClassifierLookupTag : TypeConstructorMarker {
    abstract konst name: Name

    override fun toString(): String {
        return name.asString()
    }
}

abstract class ConeClassLikeLookupTag : ConeClassifierLookupTag() {
    abstract konst classId: ClassId

    override konst name: Name
        get() = classId.shortClassName
}

