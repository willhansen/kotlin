/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.native.interop.skia

import org.jetbrains.kotlin.native.interop.gen.ManagedTypePassing
import org.jetbrains.kotlin.native.interop.gen.StubIrContext
import org.jetbrains.kotlin.native.interop.gen.getStringRepresentation
import org.jetbrains.kotlin.native.interop.gen.jvm.Plugin
import org.jetbrains.kotlin.native.interop.indexer.IndexerResult
import org.jetbrains.kotlin.native.interop.indexer.ManagedType
import org.jetbrains.kotlin.native.interop.indexer.NativeLibrary
import org.jetbrains.kotlin.native.interop.indexer.Type

class SkiaPlugin : Plugin {
    override konst name = "Skia"
    override fun buildNativeIndex(library: NativeLibrary, verbose: Boolean): IndexerResult =
            buildSkiaNativeIndexImpl(library, verbose)

    override konst managedTypePassing = object : ManagedTypePassing() {
        override konst ManagedType.passValue: String get() = "sk_ref_sp<${this.decl.stripSkiaSharedPointer}>"
        override konst ManagedType.returnValue: String get() = ".release()"
    }

    override konst ManagedType.stringRepresentation: String get() {
        assert(this.decl.isSkiaSharedPointer)
        return "${this.decl.stripSkiaSharedPointer}*"
    }
    override fun stubsBuildingContext(stubIrContext: StubIrContext) = SkiaStubsBuildingContextImpl(stubIrContext)
}