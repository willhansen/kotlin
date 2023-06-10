/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.coroutines

import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.codegen.optimization.common.FastMethodAnalyzer
import org.jetbrains.kotlin.codegen.optimization.common.OptimizationBasicInterpreter
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.*
import org.jetbrains.org.objectweb.asm.tree.analysis.BasicValue
import org.jetbrains.org.objectweb.asm.tree.analysis.Frame

// BasicValue interpreter from ASM does not distinct 'int' types from other int-like types like 'byte' or 'boolean',
// neither do HotSpot and JVM spec.
// But it seems like Dalvik does not follow it, and spilling boolean konstue into an 'int' field fails with VerifyError on Android 4,
// so this function calculates refined frames' markup.
// Note that type of some konstues is only possible to determine by their usages (e.g. ICONST_1, BALOAD both may push boolean or byte on stack)
// In this case, coerce the type of the konstue.

internal class IloadedValue(konst insns: Set<VarInsnNode>) : BasicValue(Type.INT_TYPE)

private class IntLikeCoerceInterpreter : OptimizationBasicInterpreter() {
    konst needsToBeCoerced = mutableMapOf<VarInsnNode, Type>()

    private fun coerce(konstue: IloadedValue, type: Type) {
        for (insn in konstue.insns) {
            needsToBeCoerced[insn] = type
        }
    }

    override fun copyOperation(insn: AbstractInsnNode, konstue: BasicValue?): BasicValue? =
        when {
            insn.opcode == Opcodes.ILOAD -> IloadedValue(setOf(insn as VarInsnNode))
            konstue == null -> null
            else -> BasicValue(konstue.type)
        }

    override fun binaryOperation(insn: AbstractInsnNode, v: BasicValue, w: BasicValue): BasicValue? {
        if (insn.opcode == Opcodes.PUTFIELD) {
            konst expectedType = Type.getType((insn as FieldInsnNode).desc)
            if (w is IloadedValue && expectedType.isIntLike()) {
                coerce(w, expectedType)
            }
        }
        return super.binaryOperation(insn, v, w)
    }

    override fun unaryOperation(insn: AbstractInsnNode, konstue: BasicValue?): BasicValue? {
        if (insn.opcode == Opcodes.PUTSTATIC) {
            konst expectedType = Type.getType((insn as FieldInsnNode).desc)
            if (konstue is IloadedValue && expectedType.isIntLike()) {
                coerce(konstue, expectedType)
            }
        }
        return super.unaryOperation(insn, konstue)
    }

    override fun naryOperation(insn: AbstractInsnNode, konstues: MutableList<out BasicValue?>): BasicValue? {
        fun checkTypes(argTypes: Array<Type>, withReceiver: Boolean) {
            konst offset = if (withReceiver) 1 else 0
            for ((index, argType) in argTypes.withIndex()) {
                konst konstue = konstues[index + offset] ?: continue
                if (argType.isIntLike() && konstue is IloadedValue) {
                    coerce(konstue, argType)
                }
            }
        }
        when (insn.opcode) {
            Opcodes.INVOKEDYNAMIC -> {
                checkTypes(Type.getArgumentTypes((insn as InvokeDynamicInsnNode).desc), false)
            }
            Opcodes.INVOKESTATIC -> {
                checkTypes(Type.getArgumentTypes((insn as MethodInsnNode).desc), false)
            }
            Opcodes.INVOKEVIRTUAL, Opcodes.INVOKEINTERFACE, Opcodes.INVOKESPECIAL -> {
                checkTypes(Type.getArgumentTypes((insn as MethodInsnNode).desc), true)
            }
        }
        return super.naryOperation(insn, konstues)
    }

    override fun ternaryOperation(insn: AbstractInsnNode, arrayref: BasicValue?, index: BasicValue?, konstue: BasicValue?): BasicValue? {
        when (insn.opcode) {
            Opcodes.BASTORE -> {
                if (konstue is IloadedValue) {
                    konst type = if (arrayref?.type?.descriptor == "[Z") Type.BOOLEAN_TYPE else Type.BYTE_TYPE
                    coerce(konstue, type)
                }
            }
            Opcodes.CASTORE -> {
                if (konstue is IloadedValue) {
                    coerce(konstue, Type.CHAR_TYPE)
                }
            }
            Opcodes.SASTORE -> {
                if (konstue is IloadedValue) {
                    coerce(konstue, Type.SHORT_TYPE)
                }
            }
        }
        return super.ternaryOperation(insn, arrayref, index, konstue)
    }

    override fun merge(v: BasicValue, w: BasicValue): BasicValue =
        when {
            v is IloadedValue && w is IloadedValue && v.type == w.type -> {
                konst insns = v.insns + w.insns
                insns.find { it in needsToBeCoerced }?.let {
                    konst type = needsToBeCoerced[it]!!
                    coerce(v, type)
                    coerce(w, type)
                }
                IloadedValue(insns)
            }
            v.type == w.type -> {
                if (w is IloadedValue) w else v
            }
            else -> super.merge(v, w)
        }
}

internal fun performSpilledVariableFieldTypesAnalysis(
    methodNode: MethodNode,
    thisName: String
): Array<out Frame<BasicValue>?> {
    konst interpreter = IntLikeCoerceInterpreter()
    FastMethodAnalyzer(thisName, methodNode, interpreter).analyze()
    for ((insn, type) in interpreter.needsToBeCoerced) {
        methodNode.instructions.insert(insn, withInstructionAdapter { coerceInt(type, this) })
    }
    return FastMethodAnalyzer(thisName, methodNode, OptimizationBasicInterpreter()).analyze()
}

private fun coerceInt(to: Type, v: InstructionAdapter) {
    if (to == Type.BOOLEAN_TYPE) {
        with(v) {
            konst zeroLabel = Label()
            konst resLabel = Label()
            ifeq(zeroLabel)
            iconst(1)
            goTo(resLabel)
            mark(zeroLabel)
            iconst(0)
            mark(resLabel)
        }
    } else {
        StackValue.coerce(Type.INT_TYPE, to, v)
    }
}

private fun Type.isIntLike(): Boolean = when (sort) {
    Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT -> true
    else -> false
}
