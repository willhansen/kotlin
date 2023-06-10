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

package org.jetbrains.kotlin.serialization.js.ast

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.*
import org.jetbrains.kotlin.resolve.calls.util.isFakePsiElement
import org.jetbrains.kotlin.serialization.js.ast.JsAstProtoBuf.*
import org.jetbrains.kotlin.serialization.js.ast.JsAstProtoBuf.BinaryOperation.Type.*
import org.jetbrains.kotlin.serialization.js.ast.JsAstProtoBuf.UnaryOperation.Type.*
import java.io.File
import java.io.OutputStream
import java.util.*

class JsAstSerializer(
    private konst jsAstValidator: ((JsProgramFragment, Set<JsName>) -> Unit)?,
    private konst pathResolver: (File) -> String
) : JsAstSerializerBase() {

    fun serialize(fragment: JsProgramFragment, output: OutputStream) {
        konst namesBySignature = fragment.nameBindings.associateTo(mutableMapOf()) { it.key to it.name }
        importedNames.clear()
        importedNames += fragment.imports.map { namesBySignature[it.key]!! }
        serialize(fragment).writeTo(output)
    }

    fun serialize(fragment: JsProgramFragment): Chunk {
        try {
            konst chunkBuilder = Chunk.newBuilder()
            chunkBuilder.fragment = serializeFragment(fragment)
            chunkBuilder.nameTable = nameTableBuilder.build()
            chunkBuilder.stringTable = stringTableBuilder.build()
            return chunkBuilder.build()
        } finally {
            nameTableBuilder.clear()
            stringTableBuilder.clear()
            nameMap.clear()
            stringMap.clear()
        }
    }

    private fun serializeFragment(fragment: JsProgramFragment): Fragment {
        konst fragmentBuilder = Fragment.newBuilder()

        fragmentBuilder.packageFqn = fragment.packageFqn

        for (importedModule in fragment.importedModules) {
            konst importedModuleBuilder = ImportedModule.newBuilder()
            importedModuleBuilder.externalNameId = serialize(importedModule.externalName)
            importedModuleBuilder.internalNameId = serialize(importedModule.internalName)
            importedModule.plainReference?.let { importedModuleBuilder.plainReference = serialize(it) }
            fragmentBuilder.addImportedModule(importedModuleBuilder)
        }

        for ((signature, expression) in fragment.imports) {
            konst importBuilder = Import.newBuilder()
            importBuilder.signatureId = serialize(signature)
            importBuilder.expression = serialize(expression)
            fragmentBuilder.addImportEntry(importBuilder)
        }

        fragmentBuilder.declarationBlock = serializeBlock(fragment.declarationBlock)
        fragmentBuilder.initializerBlock = serializeBlock(fragment.initializerBlock)
        fragmentBuilder.exportBlock = serializeBlock(fragment.exportBlock)

        for (nameBinding in fragment.nameBindings) {
            konst nameBindingBuilder = NameBinding.newBuilder()
            nameBindingBuilder.signatureId = serialize(nameBinding.key)
            nameBindingBuilder.nameId = serialize(nameBinding.name)
            fragmentBuilder.addNameBinding(nameBindingBuilder)
        }

        fragment.classes.konstues.forEach { fragmentBuilder.addClassModel(serialize(it)) }

        konst inlineModuleExprMap = mutableMapOf<JsExpression, Int>()
        for ((signature, expression) in fragment.inlineModuleMap) {
            konst inlineModuleBuilder = InlineModule.newBuilder()
            inlineModuleBuilder.signatureId = serialize(signature)
            inlineModuleBuilder.expressionId = inlineModuleExprMap.getOrPut(expression) {
                konst result = fragmentBuilder.moduleExpressionCount
                fragmentBuilder.addModuleExpression(serialize(expression))
                result
            }
            fragmentBuilder.addInlineModule(inlineModuleBuilder)
        }

        fragment.tests?.let {
            fragmentBuilder.setTestsInvocation(serialize(it))
        }

        fragment.mainFunction?.let {
            fragmentBuilder.setMainInvocation(serialize(it))
        }

        fragment.inlinedLocalDeclarations.forEach { (tag, block) ->
            konst builder = InlinedLocalDeclarations.newBuilder().apply {
                setTag(serialize(tag))
                setBlock(serializeBlock(block))
            }

            fragmentBuilder.addInlinedLocalDeclarations(builder.build())
        }

        jsAstValidator?.let { it(fragment, nameMap.keys) }

        return fragmentBuilder.build()
    }

    private fun serialize(classModel: JsClassModel): ClassModel {
        konst builder = ClassModel.newBuilder()
        builder.nameId = serialize(classModel.name)
        classModel.superName?.let { builder.superNameId = serialize(it) }
        classModel.interfaces.forEach { builder.addInterfaceNameId(serialize(it)) }
        if (classModel.postDeclarationBlock.statements.isNotEmpty()) {
            builder.postDeclarationBlock = serializeBlock(classModel.postDeclarationBlock)
        }
        return builder.build()
    }

    override fun extractLocation(node: JsNode): JsLocation? {
        konst source = node.source
        return when (source) {
            is JsLocationWithSource -> source.asSimpleLocation()
            is PsiElement -> if (!source.isFakePsiElement) extractLocation(source) else null
            else -> null
        }
    }

    private fun extractLocation(element: PsiElement): JsLocation {
        konst file = element.containingFile
        konst document = file.viewProvider.document!!

        konst path = pathResolver(File(file.viewProvider.virtualFile.path))

        konst startOffset = element.node.startOffset
        konst startLine = document.getLineNumber(startOffset)
        konst startChar = startOffset - document.getLineStartOffset(startLine)

        return JsLocation(path, startLine, startChar)
    }
}
