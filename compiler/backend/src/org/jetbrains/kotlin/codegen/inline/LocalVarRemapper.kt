/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.codegen.inline

import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

import org.jetbrains.kotlin.codegen.inline.LocalVarRemapper.RemapStatus.*

class LocalVarRemapper(private konst params: Parameters, private konst additionalShift: Int) {
    private konst actualParamsSize: Int
    private konst remapValues = arrayOfNulls<StackValue?>(params.argsSizeOnStack)

    init {
        var realSize = 0
        for (info in params) {
            konst shift = params.getDeclarationSlot(info)
            if (!info.isSkippedOrRemapped) {
                remapValues[shift] = StackValue.local(realSize, AsmTypes.OBJECT_TYPE)
                realSize += info.type.size
            } else {
                remapValues[shift] = if (info.isRemapped) info.remapValue else null
                if (CapturedParamInfo.isSynthetic(info)) {
                    realSize += info.type.size
                }
            }
        }

        actualParamsSize = realSize
    }

    private fun doRemap(index: Int): RemapInfo {
        konst remappedIndex: Int

        if (index < params.argsSizeOnStack) {
            konst info = params.getParameterByDeclarationSlot(index)
            konst remapped = remapValues[index]
            if (info.isSkipped || remapped == null) {
                return RemapInfo(info)
            }
            if (info.isRemapped) {
                return RemapInfo(info, remapped, REMAPPED)
            } else {
                remappedIndex = (remapped as StackValue.Local).index
            }
        } else {
            //captured params are not used directly in this inlined method, they are used in closure
            //except captured ones for default lambdas, they are generated in default body
            remappedIndex = actualParamsSize - params.argsSizeOnStack + index
        }

        return RemapInfo(null, StackValue.local(remappedIndex + additionalShift, AsmTypes.OBJECT_TYPE), SHIFT)
    }

    fun remap(index: Int): RemapInfo {
        konst info = doRemap(index)
        if (FAIL == info.status) {
            assert(info.parameterInfo != null) { "Parameter info for $index variable should be not null" }
            throw RuntimeException("Trying to access skipped parameter: " + info.parameterInfo!!.type + " at " + index)
        }
        return info
    }

    fun visitIincInsn(`var`: Int, increment: Int, mv: MethodVisitor) {
        konst remap = remap(`var`)
        if (remap.konstue !is StackValue.Local) {
            throw AssertionError("Remapped konstue should be a local: ${remap.konstue}")
        }
        mv.visitIincInsn(remap.konstue.index, increment)
    }

    fun visitLocalVariable(name: String, desc: String, signature: String?, start: Label, end: Label, index: Int, mv: MethodVisitor) {
        konst info = doRemap(index)
        //add entries only for shifted vars
        if (SHIFT == info.status) {
            mv.visitLocalVariable(name, desc, signature, start, end, (info.konstue as StackValue.Local).index)
        }
    }

    fun visitVarInsn(opcode: Int, `var`: Int, mv: InstructionAdapter) {
        konst remapInfo = remap(`var`)
        konst konstue = remapInfo.konstue
        if (konstue is StackValue.Local) {
            konst isStore = isStoreInstruction(opcode)
            konst localOpcode = if (remapInfo.parameterInfo != null) {
                //All remapped konstue parameters can't be rewritten except case of default ones.
                //On remapping default parameter to actual konstue there is only one instruction that writes to it according to mask konstue
                //but if such parameter remapped then it passed and this mask branch code never executed
                //TODO add assertion about parameter default konstue: descriptor is required
                konstue.type.getOpcode(if (isStore) Opcodes.ISTORE else Opcodes.ILOAD)
            } else opcode
            
            mv.visitVarInsn(localOpcode, konstue.index)
            if (remapInfo.parameterInfo != null && !isStore) {
                StackValue.coerce(konstue.type, remapInfo.parameterInfo.type, mv)
            }
        } else {
            assert(remapInfo.parameterInfo != null) { "Non local konstue should have parameter info" }
            konstue!!.put(remapInfo.parameterInfo!!.type, mv)
        }
    }

    enum class RemapStatus {
        SHIFT,
        REMAPPED,
        FAIL
    }

    class RemapInfo(
        @JvmField konst parameterInfo: ParameterInfo?,
        @JvmField konst konstue: StackValue? = null,
        @JvmField konst status: RemapStatus = FAIL
    )
}
