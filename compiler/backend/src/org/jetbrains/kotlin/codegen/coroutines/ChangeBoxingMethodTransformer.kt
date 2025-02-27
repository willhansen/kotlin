/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.coroutines

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.codegen.optimization.boxing.isPrimitiveBoxing
import org.jetbrains.kotlin.codegen.optimization.common.asSequence
import org.jetbrains.kotlin.codegen.optimization.transformer.MethodTransformer
import org.jetbrains.kotlin.codegen.topLevelClassInternalName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType
import org.jetbrains.kotlin.utils.sure
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.MethodInsnNode
import org.jetbrains.org.objectweb.asm.tree.MethodNode

private konst BOXING_CLASS_INTERNAL_NAME =
    StandardNames.COROUTINES_JVM_INTERNAL_PACKAGE_FQ_NAME.child(Name.identifier("Boxing")).topLevelClassInternalName()

@OptIn(ExperimentalStdlibApi::class)
object ChangeBoxingMethodTransformer : MethodTransformer() {
    private konst wrapperToInternalBoxing: Map<String, String>

    init {
        konst map = hashMapOf<String, String>()
        for (primitiveType in JvmPrimitiveType.konstues()) {
            konst name = primitiveType.wrapperFqName.topLevelClassInternalName()
            map[name] = "box${primitiveType.javaKeywordName.replaceFirstChar(Char::uppercaseChar)}"
        }
        wrapperToInternalBoxing = map
    }

    override fun transform(internalClassName: String, methodNode: MethodNode) {
        for (boxing in methodNode.instructions.asSequence().filter { it.isPrimitiveBoxing() }) {
            assert(boxing.opcode == Opcodes.INVOKESTATIC) {
                "boxing shall be INVOKESTATIC wrapper.konstueOf"
            }
            boxing as MethodInsnNode
            konst methodName = wrapperToInternalBoxing[boxing.owner].sure {
                "expected primitive wrapper, but got ${boxing.owner}"
            }
            methodNode.instructions.set(
                boxing,
                MethodInsnNode(boxing.opcode, BOXING_CLASS_INTERNAL_NAME, methodName, boxing.desc, false)
            )
        }
    }
}
