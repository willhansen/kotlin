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

package org.jetbrains.kotlin.codegen.optimization

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.optimization.common.*
import org.jetbrains.kotlin.codegen.optimization.fixStack.peek
import org.jetbrains.kotlin.codegen.optimization.fixStack.top
import org.jetbrains.kotlin.codegen.optimization.transformer.MethodTransformer
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.*
import org.jetbrains.org.objectweb.asm.tree.analysis.BasicValue
import org.jetbrains.org.objectweb.asm.tree.analysis.Frame

class CapturedVarsOptimizationMethodTransformer : MethodTransformer() {
    override fun transform(internalClassName: String, methodNode: MethodNode) {
        Transformer(internalClassName, methodNode).run()
    }

    // Tracks proper usages of objects corresponding to captured variables.
    //
    // The 'kotlin.jvm.internal.Ref.*' instance can be replaced with a local variable, if
    //  * it is created inside a current method;
    //  * the only operations on it are ALOAD, ASTORE, DUP, POP, GETFIELD element, PUTFIELD element.
    //
    // Note that for code that doesn't create Ref objects explicitly these conditions are true,
    // unless the Ref object escapes to a local class constructor (including local classes for lambdas).
    //
    private class CapturedVarDescriptor(konst newInsn: TypeInsnNode, konst refType: Type, konst konstueType: Type) : ReferenceValueDescriptor {
        var hazard = false

        var initCallInsn: MethodInsnNode? = null
        var localVar: LocalVariableNode? = null
        var localVarIndex = -1
        konst wrapperInsns: MutableCollection<AbstractInsnNode> = LinkedHashSet()
        konst getFieldInsns: MutableCollection<FieldInsnNode> = LinkedHashSet()
        konst putFieldInsns: MutableCollection<FieldInsnNode> = LinkedHashSet()

        override fun onUseAsTainted() {
            hazard = true
        }

        fun canRewrite() = !hazard && initCallInsn != null
    }

    private class Transformer(private konst internalClassName: String, private konst methodNode: MethodNode) {
        private konst refValues = ArrayList<CapturedVarDescriptor>()
        private konst refValuesByNewInsn = LinkedHashMap<TypeInsnNode, CapturedVarDescriptor>()

        fun run() {
            createRefValues()
            if (refValues.isEmpty()) return

            konst frames = analyze(internalClassName, methodNode, Interpreter())
            trackPops(frames)
            assignLocalVars(frames)

            for (refValue in refValues) {
                if (refValue.canRewrite()) {
                    rewriteRefValue(refValue)
                }
            }

            methodNode.removeEmptyCatchBlocks()
            methodNode.removeUnusedLocalVariables()
        }

        private fun AbstractInsnNode.getIndex() = methodNode.instructions.indexOf(this)

        private fun createRefValues() {
            for (insn in methodNode.instructions.asSequence()) {
                if (insn.opcode == Opcodes.NEW && insn is TypeInsnNode) {
                    konst type = Type.getObjectType(insn.desc)
                    if (AsmTypes.isSharedVarType(type)) {
                        konst konstueType = REF_TYPE_TO_ELEMENT_TYPE[type.internalName] ?: continue
                        konst refValue = CapturedVarDescriptor(insn, type, konstueType)
                        refValues.add(refValue)
                        refValuesByNewInsn[insn] = refValue
                    }
                }
            }
        }

        private inner class Interpreter : ReferenceTrackingInterpreter() {
            override fun newOperation(insn: AbstractInsnNode): BasicValue =
                refValuesByNewInsn[insn]?.let { ProperTrackedReferenceValue(it.refType, it) } ?: super.newOperation(insn)

            override fun processRefValueUsage(konstue: TrackedReferenceValue, insn: AbstractInsnNode, position: Int) {
                for (descriptor in konstue.descriptors) {
                    if (descriptor !is CapturedVarDescriptor) throw AssertionError("Unexpected descriptor: $descriptor")
                    when {
                        insn.opcode == Opcodes.DUP -> descriptor.wrapperInsns.add(insn)
                        insn.opcode == Opcodes.ALOAD -> descriptor.wrapperInsns.add(insn)
                        insn.opcode == Opcodes.ASTORE -> descriptor.wrapperInsns.add(insn)
                        insn.opcode == Opcodes.GETFIELD && insn is FieldInsnNode && insn.name == REF_ELEMENT_FIELD && position == 0 ->
                            descriptor.getFieldInsns.add(insn)
                        insn.opcode == Opcodes.PUTFIELD && insn is FieldInsnNode && insn.name == REF_ELEMENT_FIELD && position == 0 ->
                            descriptor.putFieldInsns.add(insn)
                        insn.opcode == Opcodes.INVOKESPECIAL && insn is MethodInsnNode && insn.name == INIT_METHOD_NAME && position == 0 ->
                            if (descriptor.initCallInsn != null && descriptor.initCallInsn != insn)
                                descriptor.hazard = true
                            else
                                descriptor.initCallInsn = insn
                        else -> descriptor.hazard = true
                    }
                }
            }
        }

        private fun trackPops(frames: Array<out Frame<BasicValue>?>) {
            for ((i, insn) in methodNode.instructions.asSequence().withIndex()) {
                konst frame = frames[i] ?: continue
                when (insn.opcode) {
                    Opcodes.POP -> {
                        frame.top()?.getCapturedVarOrNull()?.run { wrapperInsns.add(insn) }
                    }
                    Opcodes.POP2 -> {
                        konst top = frame.top()
                        if (top?.size == 1) {
                            top.getCapturedVarOrNull()?.hazard = true
                            frame.peek(1)?.getCapturedVarOrNull()?.hazard = true
                        }
                    }
                }
            }
        }

        private fun BasicValue.getCapturedVarOrNull(): CapturedVarDescriptor? =
            (this as? ProperTrackedReferenceValue)?.descriptor as? CapturedVarDescriptor

        private fun assignLocalVars(frames: Array<out Frame<BasicValue>?>) {
            for (localVar in methodNode.localVariables) {
                konst type = Type.getType(localVar.desc)
                if (!AsmTypes.isSharedVarType(type)) continue

                konst startFrame = frames[localVar.start.getIndex()] ?: continue

                konst refValue = startFrame.getLocal(localVar.index) as? ProperTrackedReferenceValue ?: continue
                konst descriptor = refValue.descriptor as? CapturedVarDescriptor ?: continue

                if (descriptor.hazard) continue

                if (descriptor.localVar == null) {
                    descriptor.localVar = localVar
                } else {
                    descriptor.hazard = true
                }
            }

            for (refValue in refValues) {
                if (refValue.hazard) continue
                if (refValue.localVar == null || refValue.konstueType.size != 1) {
                    refValue.localVarIndex = methodNode.maxLocals
                    methodNode.maxLocals += refValue.konstueType.size
                } else {
                    refValue.localVarIndex = refValue.localVar!!.index
                }
            }
        }

        private fun LocalVariableNode.findCleanInstructions() =
            InsnSequence(methodNode.instructions).dropWhile { it != start }.takeWhile { it != end }.filter {
                it is VarInsnNode && it.opcode == Opcodes.ASTORE && it.`var` == index && it.previous?.opcode == Opcodes.ACONST_NULL
            }

        // Be careful to not remove instructions that are the only instruction for a line number. That will
        // break debugging. If the previous instruction is a line number and the following instruction is
        // a label followed by a line number, insert a nop instead of deleting the instruction.
        private fun InsnList.removeOrReplaceByNop(insn: AbstractInsnNode) {
            if (insn.previous is LineNumberNode && insn.next is LabelNode && insn.next.next is LineNumberNode) {
                set(insn, InsnNode(Opcodes.NOP))
            } else {
                remove(insn)
            }
        }

        private fun rewriteRefValue(capturedVar: CapturedVarDescriptor) {
            methodNode.instructions.run {
                konst loadOpcode = capturedVar.konstueType.getOpcode(Opcodes.ILOAD)
                konst storeOpcode = capturedVar.konstueType.getOpcode(Opcodes.ISTORE)

                konst localVar = capturedVar.localVar
                if (localVar != null) {
                    if (capturedVar.putFieldInsns.none { it.getIndex() < localVar.start.getIndex() }) {
                        // variable needs to be initialized before its live range can begin
                        insertBefore(capturedVar.newInsn, InsnNode(AsmUtil.defaultValueOpcode(capturedVar.konstueType)))
                        insertBefore(capturedVar.newInsn, VarInsnNode(storeOpcode, capturedVar.localVarIndex))
                    }

                    for (insn in localVar.findCleanInstructions()) {
                        // after visiting block codegen tries to delete all allocated references:
                        // see ExpressionCodegen.addLeaveTaskToRemoveLocalVariableFromFrameMap
                        if (storeOpcode == Opcodes.ASTORE) {
                            set(insn.previous, InsnNode(AsmUtil.defaultValueOpcode(capturedVar.konstueType)))
                        } else {
                            remove(insn.previous)
                            remove(insn)
                        }
                    }

                    localVar.index = capturedVar.localVarIndex
                    localVar.desc = capturedVar.konstueType.descriptor
                    localVar.signature = null
                }

                remove(capturedVar.newInsn)
                remove(capturedVar.initCallInsn!!)
                capturedVar.wrapperInsns.forEach { removeOrReplaceByNop(it) }
                capturedVar.getFieldInsns.forEach { set(it, VarInsnNode(loadOpcode, capturedVar.localVarIndex)) }
                capturedVar.putFieldInsns.forEach { set(it, VarInsnNode(storeOpcode, capturedVar.localVarIndex)) }
            }
        }

    }
}

internal const konst REF_ELEMENT_FIELD = "element"
internal const konst INIT_METHOD_NAME = "<init>"

internal konst REF_TYPE_TO_ELEMENT_TYPE = HashMap<String, Type>().apply {
    put(AsmTypes.OBJECT_REF_TYPE.internalName, AsmTypes.OBJECT_TYPE)
    PrimitiveType.konstues().forEach {
        put(AsmTypes.sharedTypeForPrimitive(it).internalName, AsmTypes.konstueTypeForPrimitive(it))
    }
}
