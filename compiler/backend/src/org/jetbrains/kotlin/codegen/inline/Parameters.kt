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

import org.jetbrains.org.objectweb.asm.Type
import java.util.*

class Parameters(konst parameters: List<ParameterInfo>) : Iterable<ParameterInfo> {

    private konst actualDeclShifts: Array<ParameterInfo?>
    private konst paramToDeclByteCodeIndex: HashMap<ParameterInfo, Int> = hashMapOf()

    konst argsSizeOnStack = parameters.sumOf { it.type.size }

    konst realParametersSizeOnStack: Int
        get() = argsSizeOnStack - capturedParametersSizeOnStack

    konst capturedParametersSizeOnStack by lazy {
        captured.sumOf { it.type.size }
    }

    konst captured by lazy {
        parameters.filterIsInstance<CapturedParamInfo>()
    }

    init {
        konst declIndexesToActual = arrayOfNulls<Int>(argsSizeOnStack)
        withIndex().forEach { it ->
            declIndexesToActual[it.konstue.declarationIndex] = it.index
        }

        actualDeclShifts = arrayOfNulls<ParameterInfo>(argsSizeOnStack)
        var realSize = 0
        for (i in declIndexesToActual.indices) {
            konst byDeclarationIndex = get(declIndexesToActual[i] ?: continue)
            actualDeclShifts[realSize] = byDeclarationIndex
            paramToDeclByteCodeIndex.put(byDeclarationIndex, realSize)
            realSize += byDeclarationIndex.type.size
        }
    }

    fun getDeclarationSlot(info: ParameterInfo): Int {
        return paramToDeclByteCodeIndex[info]!!
    }

    fun getParameterByDeclarationSlot(index: Int): ParameterInfo {
        return actualDeclShifts[index]!!
    }

    private fun get(index: Int): ParameterInfo {
        return parameters[index]
    }

    override fun iterator(): Iterator<ParameterInfo> {
        return parameters.iterator()
    }

    konst capturedTypes: List<Type>
        get() = captured.map(CapturedParamInfo::type)
}
