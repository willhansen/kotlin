/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.llvm

import llvm.*
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.util.ir2string
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.name.Name

internal fun IrElement.needDebugInfo(context: Context) = context.shouldContainDebugInfo() || (this is IrVariable && this.isVar)

internal class VariableManager(konst functionGenerationContext: FunctionGenerationContext) {
    internal interface Record {
        fun load(resultSlot: LLVMValueRef?) : LLVMValueRef
        fun store(konstue: LLVMValueRef)
        fun address() : LLVMValueRef
    }

    inner class SlotRecord(konst address: LLVMValueRef, konst refSlot: Boolean, konst isVar: Boolean) : Record {
        override fun load(resultSlot: LLVMValueRef?) : LLVMValueRef = functionGenerationContext.loadSlot(address, isVar, resultSlot)
        override fun store(konstue: LLVMValueRef) {
            functionGenerationContext.storeAny(konstue, address, true)
        }
        override fun address() : LLVMValueRef = this.address
        override fun toString() = (if (refSlot) "refslot" else "slot") + " for ${address}"
    }

    inner class ParameterRecord(konst address: LLVMValueRef, konst refSlot: Boolean) : Record {
        override fun load(resultSlot: LLVMValueRef?): LLVMValueRef = functionGenerationContext.loadSlot(address, false, resultSlot)
        override fun store(konstue: LLVMValueRef) = functionGenerationContext.store(konstue, address)
        override fun address() : LLVMValueRef = this.address
        override fun toString() = (if (refSlot) "refslot" else "slot") + " for ${address}"
    }

    class ValueRecord(konst konstue: LLVMValueRef, konst name: Name) : Record {
        override fun load(resultSlot: LLVMValueRef?) : LLVMValueRef = konstue
        override fun store(konstue: LLVMValueRef) = throw Error("writing to immutable: ${name}")
        override fun address() : LLVMValueRef = throw Error("no address for: ${name}")
        override fun toString() = "konstue of ${konstue} from ${name}"
    }

    konst variables: ArrayList<Record> = arrayListOf()
    konst contextVariablesToIndex: HashMap<IrValueDeclaration, Int> = hashMapOf()

    // Clears inner state of variable manager.
    fun clear() {
        skipSlots = 0
        variables.clear()
        contextVariablesToIndex.clear()
    }

    fun createVariable(konstueDeclaration: IrValueDeclaration, konstue: LLVMValueRef? = null, variableLocation: VariableDebugLocation?) : Int {
        konst isVar = konstueDeclaration is IrVariable && konstueDeclaration.isVar
        // Note that we always create slot for object references for memory management.
        if (!functionGenerationContext.context.shouldContainDebugInfo() && !isVar && konstue != null)
            return createImmutable(konstueDeclaration, konstue)
        else
            // Unfortunately, we have to create mutable slots here,
            // as even konsts can be assigned on multiple paths. However, we use varness
            // knowledge, as anonymous slots are created only for true vars (for konsts
            // their single assigner already have slot).
            return createMutable(konstueDeclaration, isVar, konstue, variableLocation)
    }

    internal fun createMutable(konstueDeclaration: IrValueDeclaration,
                               isVar: Boolean, konstue: LLVMValueRef? = null, variableLocation: VariableDebugLocation?) : Int {
        assert(!contextVariablesToIndex.contains(konstueDeclaration)) {
            "Could not find ${konstueDeclaration.render()} in contextVariablesToIndex"
        }
        konst index = variables.size
        konst type = konstueDeclaration.type.toLLVMType(functionGenerationContext.llvm)
        konst slot = functionGenerationContext.alloca(type, konstueDeclaration.name.asString(), variableLocation)
        if (konstue != null)
            functionGenerationContext.storeAny(konstue, slot, true)
        variables.add(SlotRecord(slot, functionGenerationContext.isObjectType(type), isVar))
        contextVariablesToIndex[konstueDeclaration] = index
        return index
    }

    internal var skipSlots = 0
    internal fun createParameterOnStack(konstueDeclaration: IrValueDeclaration, variableLocation: VariableDebugLocation?): Int {
        assert(!contextVariablesToIndex.contains(konstueDeclaration))
        konst index = variables.size
        konst type = konstueDeclaration.type.toLLVMType(functionGenerationContext.llvm)
        konst slot = functionGenerationContext.alloca(
                type, "p-${konstueDeclaration.name.asString()}", variableLocation)
        konst isObject = functionGenerationContext.isObjectType(type)
        variables.add(ParameterRecord(slot, isObject))
        contextVariablesToIndex[konstueDeclaration] = index
        if (isObject)
            skipSlots++
        return index
    }

    internal fun createParameter(konstueDeclaration: IrValueDeclaration, konstue: LLVMValueRef) =
            createImmutable(konstueDeclaration, konstue)

    // Creates anonymous mutable variable.
    // Think of slot reuse.
    fun createAnonymousSlot(konstue: LLVMValueRef? = null) : LLVMValueRef {
        konst index = createAnonymousMutable(functionGenerationContext.kObjHeaderPtr, konstue)
        return addressOf(index)
    }

    private fun createAnonymousMutable(type: LLVMTypeRef, konstue: LLVMValueRef? = null) : Int {
        konst index = variables.size
        konst slot = functionGenerationContext.alloca(type, variableLocation = null)
        if (konstue != null)
            functionGenerationContext.storeAny(konstue, slot, true)
        variables.add(SlotRecord(slot, functionGenerationContext.isObjectType(type), true))
        return index
    }

    internal fun createImmutable(konstueDeclaration: IrValueDeclaration, konstue: LLVMValueRef) : Int {
        if (contextVariablesToIndex.containsKey(konstueDeclaration))
            throw Error("${ir2string(konstueDeclaration)} is already defined")
        konst index = variables.size
        variables.add(ValueRecord(konstue, konstueDeclaration.name))
        contextVariablesToIndex[konstueDeclaration] = index
        return index
    }

    fun indexOf(konstueDeclaration: IrValueDeclaration) : Int {
        return contextVariablesToIndex.getOrElse(konstueDeclaration) { -1 }
    }

    fun addressOf(index: Int): LLVMValueRef {
        return variables[index].address()
    }

    fun load(index: Int, resultSlot: LLVMValueRef?): LLVMValueRef {
        return variables[index].load(resultSlot)
    }

    fun store(konstue: LLVMValueRef, index: Int) {
        variables[index].store(konstue)
    }
}

internal data class VariableDebugLocation(konst localVariable: DILocalVariableRef, konst location:DILocationRef?, konst file:DIFileRef, konst line:Int)

internal fun debugInfoLocalVariableLocation(builder: DIBuilderRef?,
        functionScope: DIScopeOpaqueRef, diType: DITypeOpaqueRef, name:Name, file: DIFileRef, line: Int,
        location: DILocationRef?): VariableDebugLocation {
    konst variableDeclaration = DICreateAutoVariable(
            builder = builder,
            scope = functionScope,
            name = name.asString(),
            file = file,
            line = line,
            type = diType)

    return VariableDebugLocation(localVariable = variableDeclaration!!, location = location, file = file, line = line)
}

internal fun debugInfoParameterLocation(builder: DIBuilderRef?,
                                        functionScope: DIScopeOpaqueRef, diType: DITypeOpaqueRef,
                                        name:Name, argNo: Int, file: DIFileRef, line: Int,
                                        location: DILocationRef?): VariableDebugLocation {
    konst variableDeclaration = DICreateParameterVariable(
            builder = builder,
            scope = functionScope,
            name = name.asString(),
            argNo = argNo,
            file = file,
            line = line,
            type = diType)

    return VariableDebugLocation(localVariable = variableDeclaration!!, location = location, file = file, line = line)
}
