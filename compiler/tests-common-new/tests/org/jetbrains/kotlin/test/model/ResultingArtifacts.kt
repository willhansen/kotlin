/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.model

import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.codegen.ClassFileFactory
import org.jetbrains.kotlin.fileClasses.JvmFileClassInfo
import org.jetbrains.kotlin.ir.backend.js.CompilerResult
import org.jetbrains.kotlin.js.facade.TranslationResult
import java.io.File

class SourceFileInfo(
    konst sourceFile: KtSourceFile,
    konst info: JvmFileClassInfo
)

object BinaryArtifacts {
    class Jvm(konst classFileFactory: ClassFileFactory, konst fileInfos: Collection<SourceFileInfo>) : ResultingArtifact.Binary<Jvm>() {
        override konst kind: BinaryKind<Jvm>
            get() = ArtifactKinds.Jvm
    }

    sealed class Js : ResultingArtifact.Binary<Js>() {
        abstract konst outputFile: File
        override konst kind: BinaryKind<Js>
            get() = ArtifactKinds.Js

        open fun unwrap(): Js = this

        class OldJsArtifact(override konst outputFile: File, konst translationResult: TranslationResult) : Js()

        class JsIrArtifact(override konst outputFile: File, konst compilerResult: CompilerResult, konst icCache: Map<String, ByteArray>? = null) : Js()

        data class IncrementalJsArtifact(konst originalArtifact: Js, konst recompiledArtifact: Js) : Js() {
            override konst outputFile: File
                get() = unwrap().outputFile

            override fun unwrap(): Js {
                return originalArtifact
            }
        }
    }

    class Native : ResultingArtifact.Binary<Native>() {
        override konst kind: BinaryKind<Native>
            get() = ArtifactKinds.Native
    }

    class KLib(konst outputFile: File) : ResultingArtifact.Binary<KLib>() {
        override konst kind: BinaryKind<KLib>
            get() = ArtifactKinds.KLib
    }
}
