/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend

import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isConst
import org.jetbrains.kotlin.ir.declarations.MetadataSource
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

sealed class FirMetadataSource : MetadataSource {
    abstract konst fir: FirDeclaration?

    override konst name: Name?
        get() = when (konst fir = fir) {
            is FirConstructor -> SpecialNames.INIT
            is FirSimpleFunction -> fir.name
            is FirRegularClass -> fir.name
            is FirProperty -> fir.name
            else -> null
        }

    class File(konst files: List<FirFile>) : FirMetadataSource(), MetadataSource.File {
        override var serializedIr: ByteArray? = null

        override konst fir: FirDeclaration?
            get() = null
    }

    class Class(override konst fir: FirClass) : FirMetadataSource(), MetadataSource.Class {
        override var serializedIr: ByteArray? = null
    }

    class Function(override konst fir: FirFunction) : FirMetadataSource(), MetadataSource.Function

    class Property(override konst fir: FirProperty) : FirMetadataSource(), MetadataSource.Property {
        override konst isConst: Boolean get() = fir.isConst
    }

    class Script(override konst fir: FirScript) : FirMetadataSource(), MetadataSource.Script
}
