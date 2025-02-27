/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.range.comparison

import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class UnsignedIntegerComparisonGenerator(
    override konst comparedType: Type,
    private konst compareMethodName: String
) : ComparisonGenerator {

    private konst compareMethodDescriptor = Type.getMethodDescriptor(Type.INT_TYPE, comparedType, comparedType)

    override fun jumpIfGreaterOrEqual(v: InstructionAdapter, label: Label) {
        compareAndJump(v, label, Opcodes.IFGE)
    }

    override fun jumpIfLessOrEqual(v: InstructionAdapter, label: Label) {
        compareAndJump(v, label, Opcodes.IFLE)
    }

    override fun jumpIfGreater(v: InstructionAdapter, label: Label) {
        compareAndJump(v, label, Opcodes.IFGT)
    }

    override fun jumpIfLess(v: InstructionAdapter, label: Label) {
        compareAndJump(v, label, Opcodes.IFLT)
    }

    private fun compareAndJump(v: InstructionAdapter, label: Label, opcode: Int) {
        v.invokestatic("kotlin/UnsignedKt", compareMethodName, compareMethodDescriptor, false)
        v.visitJumpInsn(opcode, label)
    }
}

konst UIntComparisonGenerator = UnsignedIntegerComparisonGenerator(Type.INT_TYPE, "uintCompare")
konst ULongComparisonGenerator = UnsignedIntegerComparisonGenerator(Type.LONG_TYPE, "ulongCompare")