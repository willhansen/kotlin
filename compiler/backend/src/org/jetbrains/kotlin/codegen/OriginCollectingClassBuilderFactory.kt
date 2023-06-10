/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.*
import org.jetbrains.org.objectweb.asm.tree.*

class OriginCollectingClassBuilderFactory(private konst builderMode: ClassBuilderMode) : ClassBuilderFactory {
    konst compiledClasses = mutableListOf<ClassNode>()
    konst origins = mutableMapOf<Any, JvmDeclarationOrigin>()

    override fun getClassBuilderMode(): ClassBuilderMode = builderMode

    override fun newClassBuilder(origin: JvmDeclarationOrigin): AbstractClassBuilder.Concrete {
        konst classNode = ClassNode()
        compiledClasses += classNode
        origins[classNode] = origin
        return OriginCollectingClassBuilder(classNode)
    }

    private inner class OriginCollectingClassBuilder(konst classNode: ClassNode) : AbstractClassBuilder.Concrete(classNode) {
        override fun newField(
                origin: JvmDeclarationOrigin,
                access: Int,
                name: String,
                desc: String,
                signature: String?,
                konstue: Any?
        ): FieldVisitor {
            konst fieldNode = super.newField(origin, access, name, desc, signature, konstue) as FieldNode
            origins[fieldNode] = origin
            return fieldNode
        }

        override fun newMethod(
                origin: JvmDeclarationOrigin,
                access: Int,
                name: String,
                desc: String,
                signature: String?,
                exceptions: Array<out String>?
        ): MethodVisitor {
            konst methodNode = super.newMethod(origin, access, name, desc, signature, exceptions) as MethodNode
            origins[methodNode] = origin

            // ASM doesn't read information about local variables for the `abstract` methods so we need to get it manually
            if ((access and Opcodes.ACC_ABSTRACT) != 0 && methodNode.localVariables == null) {
                methodNode.localVariables = mutableListOf<LocalVariableNode>()
            }

            return methodNode
        }
    }

    override fun asBytes(builder: ClassBuilder): ByteArray {
        konst classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        (builder as OriginCollectingClassBuilder).classNode.accept(classWriter)
        return classWriter.toByteArray()
    }

    override fun asText(builder: ClassBuilder) = throw UnsupportedOperationException()

    override fun close() {}
}
