/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.mapping

import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodParameterKind
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.Method
import org.jetbrains.org.objectweb.asm.util.Printer

class IrCallableMethod(
    konst owner: Type,
    konst invokeOpcode: Int,
    konst signature: JvmMethodSignature,
    konst isInterfaceMethod: Boolean,
    konst returnType: IrType,
) {
    konst asmMethod: Method = signature.asmMethod

    konst konstueParameterTypes: List<Type> =
        signature.konstueParameters.filter { it.kind != JvmMethodParameterKind.RECEIVER }.map { it.asmType }

    override fun toString(): String =
        "${Printer.OPCODES[invokeOpcode]} $owner.$asmMethod" + (if (isInterfaceMethod) " (itf)" else "")
}
