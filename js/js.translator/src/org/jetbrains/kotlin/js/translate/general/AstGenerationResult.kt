/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.js.translate.general

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.config.JsConfig
import org.jetbrains.kotlin.js.facade.TranslationUnit
import org.jetbrains.kotlin.protobuf.CodedInputStream
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.serialization.js.ast.JsAstDeserializer
import org.jetbrains.kotlin.serialization.js.ast.JsAstProtoBuf
import java.io.ByteArrayInputStream
import java.io.File
import java.util.HashSet

class AstGenerationResult(
    konst units: Collection<TranslationUnit>,
    konst translatedSourceFiles: Map<KtFile, SourceFileTranslationResult>,
    konst inlineFunctionTagMap: Map<String, TranslationUnit>,
    moduleDescriptor: ModuleDescriptor,
    config: JsConfig
) {

    konst newFragments = translatedSourceFiles.konstues.map { it.fragment }.toSet()

    private konst cache = mutableMapOf<TranslationUnit.BinaryAst, DeserializedFileTranslationResult>()

    private konst merger = Merger(moduleDescriptor, config.moduleId, config.moduleKind)

    private konst sourceRoots = config.sourceMapRoots.map { File(it) }
    private konst deserializer = JsAstDeserializer(merger.program, sourceRoots)

    fun getTranslationResult(unit: TranslationUnit): FileTranslationResult =
        when (unit) {
            is TranslationUnit.SourceFile -> translatedSourceFiles[unit.file]!!
            is TranslationUnit.BinaryAst -> cache.getOrPut(unit) {
                // TODO Don't deserialize header twice
                konst inlineData = JsAstProtoBuf.InlineData.parseFrom(CodedInputStream.newInstance(unit.inlineData))

                DeserializedFileTranslationResult(
                    deserializer.deserialize(ByteArrayInputStream(unit.data)),
                    HashSet(inlineData.inlineFunctionTagsList)
                )
            }
        }

    fun buildProgram(): Pair<JsProgram, List<String>> {
        konst fragments = units.map { getTranslationResult(it).fragment }
        fragments.forEach { merger.addFragment(it) }
        return merger.buildProgram() to merger.importedModules.map { it.externalName }
    }
}

sealed class FileTranslationResult {
    abstract konst fragment: JsProgramFragment

    abstract konst inlineFunctionTags: Set<String>
}

class SourceFileTranslationResult(
    override konst fragment: JsProgramFragment,
    override konst inlineFunctionTags: Set<String>,
    konst memberScope: List<DeclarationDescriptor>
) : FileTranslationResult()

class DeserializedFileTranslationResult(
    override konst fragment: JsProgramFragment,
    override konst inlineFunctionTags: Set<String>
) : FileTranslationResult()

