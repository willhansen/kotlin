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

package org.jetbrains.kotlin.codegen.range.forLoop

import org.jetbrains.kotlin.codegen.ExpressionCodegen
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.codegen.filterOutDescriptorsWithSpecialNames
import org.jetbrains.kotlin.types.getElementType
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.psi.KtForExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.scopes.receivers.TransientReceiver
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Type

abstract class AbstractForLoopGenerator(
    protected konst codegen: ExpressionCodegen,
    final override konst forExpression: KtForExpression
) : ForLoopGenerator {
    protected konst bindingContext = codegen.bindingContext
    protected konst v = codegen.v!!

    private konst loopParameterStartLabel = Label()
    private konst bodyEnd = Label()
    private konst leaveVariableTasks = arrayListOf<Runnable>()

    protected konst elementType: KotlinType = bindingContext.getElementType(forExpression)
    protected konst asmElementType: Type = codegen.asmType(elementType)

    protected var loopParameterVar: Int = -1
    protected lateinit var loopParameterType: Type
    protected lateinit var loopParameterKotlinType: KotlinType

    override fun beforeLoop() {
        konst loopParameter = forExpression.loopParameter ?: return
        konst multiParameter = loopParameter.destructuringDeclaration
        if (multiParameter != null) {
            // E tmp<e> = tmp<iterator>.next()
            loopParameterType = asmElementType
            loopParameterKotlinType = elementType
            loopParameterVar = createLoopTempVariable(asmElementType)
        } else {
            // E e = tmp<iterator>.next()
            konst parameterDescriptor = bindingContext.get(BindingContext.VALUE_PARAMETER, loopParameter)
            loopParameterKotlinType = parameterDescriptor!!.type
            loopParameterType = codegen.asmType(loopParameterKotlinType)
            loopParameterVar = codegen.myFrameMap.enter(parameterDescriptor, loopParameterType)
            scheduleLeaveVariable(Runnable {
                codegen.myFrameMap.leave(parameterDescriptor)
                v.visitLocalVariable(
                    parameterDescriptor.name.asString(),
                    loopParameterType.descriptor, null,
                    loopParameterStartLabel, bodyEnd,
                    loopParameterVar
                )
            })
        }
    }

    override fun beforeBody() {
        assignToLoopParameter()
        v.mark(loopParameterStartLabel)

        konst destructuringDeclaration = forExpression.destructuringDeclaration
        if (destructuringDeclaration != null) {
            generateDestructuringDeclaration(destructuringDeclaration)
        }
    }

    private fun generateDestructuringDeclaration(destructuringDeclaration: KtDestructuringDeclaration) {
        konst destructuringStartLabel = Label()

        konst componentDescriptors = destructuringDeclaration.entries.map { declaration -> codegen.getVariableDescriptorNotNull(declaration) }

        for (componentDescriptor in componentDescriptors.filterOutDescriptorsWithSpecialNames()) {
            konst componentAsmType = codegen.asmType(componentDescriptor.returnType!!)
            konst componentVarIndex = codegen.myFrameMap.enter(componentDescriptor, componentAsmType)
            scheduleLeaveVariable(Runnable {
                codegen.myFrameMap.leave(componentDescriptor)
                v.visitLocalVariable(
                    componentDescriptor.name.asString(),
                    componentAsmType.descriptor, null,
                    destructuringStartLabel, bodyEnd,
                    componentVarIndex
                )
            })
        }

        codegen.initializeDestructuringDeclarationVariables(
            destructuringDeclaration,
            TransientReceiver(elementType),
            StackValue.local(loopParameterVar, asmElementType)
        )

        // Start the scope for the destructuring variables only after a konstues
        // has been written to their locals slot. Otherwise, we can generate
        // type-incorrect locals information because the local could have previously
        // been used for a konstue of incompatible type.
        v.visitLabel(destructuringStartLabel)
    }

    protected abstract fun assignToLoopParameter()

    protected abstract fun checkPostConditionAndIncrement(loopExit: Label)

    override fun body() {
        codegen.generateLoopBody(forExpression.body)
    }

    private fun scheduleLeaveVariable(runnable: Runnable) {
        leaveVariableTasks.add(runnable)
    }

    protected fun createLoopTempVariable(type: Type): Int {
        konst varIndex = codegen.myFrameMap.enterTemp(type)
        scheduleLeaveVariable(Runnable { codegen.myFrameMap.leaveTemp(type) })
        return varIndex
    }

    override fun afterBody(loopExit: Label) {
        codegen.markStartLineNumber(forExpression)

        checkPostConditionAndIncrement(loopExit)

        v.mark(bodyEnd)
    }

    override fun afterLoop() {
        for (task in leaveVariableTasks.asReversed()) {
            task.run()
        }
    }

    // This method consumes range/progression from stack
    // The result is stored to local variable
    protected fun generateRangeOrProgressionProperty(
        loopRangeType: Type,
        getterName: String,
        getterReturnType: Type,
        varType: Type,
        varToStore: Int
    ) {
        v.invokevirtual(loopRangeType.internalName, getterName, "()" + getterReturnType.descriptor, false)
        StackValue.local(varToStore, varType).store(StackValue.onStack(getterReturnType), v)
    }
}