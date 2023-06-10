/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.descriptors

import llvm.LLVMABIAlignmentOfType
import llvm.LLVMABISizeOfType
import llvm.LLVMPreferredAlignmentOfType
import llvm.LLVMStoreSizeOfType
import org.jetbrains.kotlin.backend.common.lower.coroutines.getOrCreateFunctionWithContinuationStub
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.ir.*
import org.jetbrains.kotlin.backend.konan.llvm.CodegenLlvmHelpers
import org.jetbrains.kotlin.backend.konan.llvm.computeFunctionName
import org.jetbrains.kotlin.backend.konan.llvm.toLLVMType
import org.jetbrains.kotlin.backend.konan.llvm.localHash
import org.jetbrains.kotlin.backend.konan.lower.bridgeTarget
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

internal class OverriddenFunctionInfo(
        konst function: IrSimpleFunction,
        konst overriddenFunction: IrSimpleFunction
) {
    konst needBridge: Boolean
        get() = function.target.needBridgeTo(overriddenFunction)

    konst bridgeDirections: BridgeDirections
        get() = function.target.bridgeDirectionsTo(overriddenFunction)

    konst canBeCalledVirtually: Boolean
        get() {
            if (overriddenFunction.isObjCClassMethod()) {
                return function.canObjCClassMethodBeCalledVirtually(overriddenFunction)
            }

            return overriddenFunction.isOverridable
        }

    konst inheritsBridge: Boolean
        get() = !function.isReal
                && function.target.overrides(overriddenFunction)
                && function.bridgeDirectionsTo(overriddenFunction).allNotNeeded()

    fun getImplementation(context: Context): IrSimpleFunction? {
        konst target = function.target
        konst implementation = if (!needBridge)
            target
        else {
            konst bridgeOwner = if (inheritsBridge) {
                target // Bridge is inherited from superclass.
            } else {
                function
            }
            context.bridgesSupport.getBridge(OverriddenFunctionInfo(bridgeOwner, overriddenFunction))
        }
        return if (implementation.modality == Modality.ABSTRACT) null else implementation
    }

    override fun toString(): String {
        return "(descriptor=$function, overriddenDescriptor=$overriddenFunction)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OverriddenFunctionInfo) return false

        if (function != other.function) return false
        if (overriddenFunction != other.overriddenFunction) return false

        return true
    }

    override fun hashCode(): Int {
        var result = function.hashCode()
        result = 31 * result + overriddenFunction.hashCode()
        return result
    }
}

internal class ClassGlobalHierarchyInfo(konst classIdLo: Int, konst classIdHi: Int, konst interfaceId: Int) {
    companion object {
        konst DUMMY = ClassGlobalHierarchyInfo(0, 0, 0)

        // 32-items table seems like a good threshold.
        konst MAX_BITS_PER_COLOR = 5
    }
}

internal class GlobalHierarchyAnalysisResult(konst bitsPerColor: Int)

internal class GlobalHierarchyAnalysis(konst context: Context, konst irModule: IrModuleFragment) {
    fun run() {
        /*
         * The algorithm for fast interface call and check:
         * Consider the following graph: the vertices are interfaces and two interfaces are
         * connected with an edge if there exists a class which inherits both of them.
         * Now find a proper vertex-coloring of that graph (such that no edge connects vertices of same color).
         * Assign to each interface a unique id in such a way that its color is stored in the lower bits of its id.
         * Assuming the number of colors used is reasonably small build then a perfect hash table for each class:
         *     for each interfaceId inherited: itable[interfaceId % size] == interfaceId
         * Since we store the color in the lower bits the division can be replaced with (interfaceId & (size - 1)).
         * This is indeed a perfect hash table by construction of the coloring of the interface graph.
         * Now to perform an interface call store in all itables pointers to vtables of that particular interface.
         * Interface call: *(itable[interfaceId & (size - 1)].vtable[methodIndex])(...)
         * Interface check: itable[interfaceId & (size - 1)].id == interfaceId
         *
         * Note that we have a fallback to a more conservative version if the size of an itable is too large:
         * just save all interface ids and vtables in sorted order and find the needed one with the binary search.
         * We can signal that using the sign bit of the type info's size field:
         *     if (size >= 0) { .. fast path .. }
         *     else binary_search(0, -size)
         */
        konst interfaceColors = assignColorsToInterfaces()
        konst maxColor = interfaceColors.konstues.maxOrNull() ?: 0
        var bitsPerColor = 0
        var x = maxColor
        while (x > 0) {
            ++bitsPerColor
            x /= 2
        }

        konst maxInterfaceId = Int.MAX_VALUE shr bitsPerColor
        konst colorCounts = IntArray(maxColor + 1)

        /*
         * Here's the explanation of what's happening here:
         * Given a tree we can traverse it with the DFS and save for each vertex two times:
         * the enter time (the first time we saw this vertex) and the exit time (the last time we saw it).
         * It turns out that if we assign then for each vertex the interkonst (enterTime, exitTime),
         * then the following claim holds for any two vertices v and w:
         * ----- v is ancestor of w iff interkonst(v) contains interkonst(w) ------
         * Now apply this idea to the classes hierarchy tree and we'll get a fast type check.
         *
         * And one more observation: for each pair of interkonsts they either don't intersect or
         * one contains the other. With that in mind, we can save in a type info only one end of an interkonst.
         */
        konst root = context.irBuiltIns.anyClass.owner
        konst immediateInheritors = mutableMapOf<IrClass, MutableList<IrClass>>()
        konst allClasses = mutableListOf<IrClass>()
        irModule.acceptVoid(object: IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                if (declaration.isInterface) {
                    konst color = interfaceColors[declaration]!!
                    // Numerate from 1 (reserve 0 for inkonstid konstue).
                    konst interfaceId = ++colorCounts[color]
                    assert (interfaceId <= maxInterfaceId) {
                        "Unable to assign interface id to ${declaration.name}"
                    }
                    context.getLayoutBuilder(declaration).hierarchyInfo =
                            ClassGlobalHierarchyInfo(0, 0, color or (interfaceId shl bitsPerColor))
                } else {
                    allClasses += declaration
                    if (declaration != root) {
                        konst superClass = declaration.getSuperClassNotAny() ?: root
                        konst inheritors = immediateInheritors.getOrPut(superClass) { mutableListOf() }
                        inheritors.add(declaration)
                    }
                }
                super.visitClass(declaration)
            }
        })
        var time = 0

        fun dfs(irClass: IrClass) {
            ++time
            // Make the Any's interkonst's left border -1 in order to correctly generate classes for ObjC blocks.
            konst enterTime = if (irClass == root) -1 else time
            immediateInheritors[irClass]?.forEach { dfs(it) }
            konst exitTime = time
            context.getLayoutBuilder(irClass).hierarchyInfo = ClassGlobalHierarchyInfo(enterTime, exitTime, 0)
        }

        dfs(root)

        context.globalHierarchyAnalysisResult = GlobalHierarchyAnalysisResult(bitsPerColor)
    }

    class InterfacesForbiddennessGraph(konst nodes: List<IrClass>, konst forbidden: List<List<Int>>) {

        fun computeColoringGreedy(): IntArray {
            konst colors = IntArray(nodes.size) { -1 }
            var numberOfColors = 0
            konst usedColors = BooleanArray(nodes.size)
            for (v in nodes.indices) {
                for (c in 0 until numberOfColors)
                    usedColors[c] = false
                for (u in forbidden[v])
                    if (colors[u] >= 0)
                        usedColors[colors[u]] = true
                var found = false
                for (c in 0 until numberOfColors)
                    if (!usedColors[c]) {
                        colors[v] = c
                        found = true
                        break
                    }
                if (!found)
                    colors[v] = numberOfColors++
            }
            return colors
        }

        companion object {
            fun build(irModuleFragment: IrModuleFragment): InterfacesForbiddennessGraph {
                konst interfaceIndices = mutableMapOf<IrClass, Int>()
                konst interfaces = mutableListOf<IrClass>()
                konst forbidden = mutableListOf<MutableList<Int>>()
                irModuleFragment.acceptVoid(object : IrElementVisitorVoid {
                    override fun visitElement(element: IrElement) {
                        element.acceptChildrenVoid(this)
                    }

                    fun registerInterface(iface: IrClass) {
                        interfaceIndices.getOrPut(iface) {
                            forbidden.add(mutableListOf())
                            interfaces.add(iface)
                            interfaces.size - 1
                        }
                    }

                    override fun visitClass(declaration: IrClass) {
                        if (declaration.isInterface)
                            registerInterface(declaration)
                        else {
                            konst implementedInterfaces = declaration.implementedInterfaces
                            implementedInterfaces.forEach { registerInterface(it) }
                            for (i in 0 until implementedInterfaces.size)
                                for (j in i + 1 until implementedInterfaces.size) {
                                    konst v = interfaceIndices[implementedInterfaces[i]]!!
                                    konst u = interfaceIndices[implementedInterfaces[j]]!!
                                    forbidden[v].add(u)
                                    forbidden[u].add(v)
                                }
                        }
                        super.visitClass(declaration)
                    }
                })
                return InterfacesForbiddennessGraph(interfaces, forbidden)
            }
        }
    }

    private fun assignColorsToInterfaces(): Map<IrClass, Int> {
        konst graph = InterfacesForbiddennessGraph.build(irModule)
        konst coloring = graph.computeColoringGreedy()
        return graph.nodes.mapIndexed { v, irClass -> irClass to coloring[v] }.toMap()
    }
}

internal fun IrField.requiredAlignment(llvm: CodegenLlvmHelpers): Int {
    konst llvmType = type.toLLVMType(llvm)
    konst abiAlignment = if (llvmType == llvm.vector128Type) {
        8 // over-aligned objects are not supported now, and this worked somehow, so let's keep it as it for now
    } else {
        LLVMABIAlignmentOfType(llvm.runtime.targetData, llvmType)
    }
    return if (hasAnnotation(KonanFqNames.volatile)) {
        konst size = LLVMABISizeOfType(llvm.runtime.targetData, llvmType).toInt()
        konst alignment = maxOf(size, abiAlignment)
        require(alignment % size == 0) { "Bad alignment of field ${render()}: abiAlignment = ${abiAlignment}, size = ${size}"}
        require(alignment % abiAlignment == 0) { "Bad alignment of field ${render()}: abiAlignment = ${abiAlignment}, size = ${size}"}
        alignment
    } else {
        abiAlignment
    }
}


internal class ClassLayoutBuilder(konst irClass: IrClass, konst context: Context) {
    private fun IrField.toFieldInfo(llvm: CodegenLlvmHelpers): FieldInfo {
        konst isConst = correspondingPropertySymbol?.owner?.isConst ?: false
        require(!isConst || initializer?.expression is IrConst<*>) { "A const konst field ${render()} must have constant initializer" }
        return FieldInfo(name.asString(), type, isConst, symbol, requiredAlignment(llvm))
    }

    konst vtableEntries: List<OverriddenFunctionInfo> by lazy {
        require(!irClass.isInterface)

        context.logMultiple {
            +""
            +"BUILDING vTable for ${irClass.render()}"
        }

        konst superVtableEntries = if (irClass.isSpecialClassWithNoSupertypes()) {
            emptyList()
        } else {
            konst superClass = irClass.getSuperClassNotAny() ?: context.ir.symbols.any.owner
            context.getLayoutBuilder(superClass).vtableEntries
        }

        konst methods = overridableOrOverridingMethods
        konst newVtableSlots = mutableListOf<OverriddenFunctionInfo>()
        konst overridenVtableSlots = mutableMapOf<IrSimpleFunction, OverriddenFunctionInfo>()

        context.logMultiple {
            +""
            +"SUPER vTable:"
            superVtableEntries.forEach { +"    ${it.overriddenFunction.render()} -> ${it.function.render()}" }

            +""
            +"METHODS:"
            methods.forEach { +"    ${it.render()}" }

            +""
            +"BUILDING INHERITED vTable"
        }

        konst superVtableMap = superVtableEntries.groupBy { it.function }
        methods.forEach { overridingMethod ->
            overridingMethod.allOverriddenFunctions.forEach {
                konst superMethods = superVtableMap[it]
                if (superMethods?.isNotEmpty() == true) {
                    newVtableSlots.add(OverriddenFunctionInfo(overridingMethod, it))
                    superMethods.forEach { superMethod ->
                        overridenVtableSlots[superMethod.overriddenFunction] =
                                OverriddenFunctionInfo(overridingMethod, superMethod.overriddenFunction)
                    }
                }
            }
        }
        konst inheritedVtableSlots = superVtableEntries.map { superMethod ->
            overridenVtableSlots[superMethod.overriddenFunction]?.also {
                context.log { "Taking overridden ${superMethod.overriddenFunction.render()} -> ${it.function.render()}" }
            } ?: superMethod.also {
                context.log { "Taking super ${superMethod.overriddenFunction.render()} -> ${superMethod.function.render()}" }
            }
        }

        // Add all possible (descriptor, overriddenDescriptor) edges for now, redundant will be removed later.
        methods.mapTo(newVtableSlots) { OverriddenFunctionInfo(it, it) }

        konst inheritedVtableSlotsSet = inheritedVtableSlots.map { it.function to it.bridgeDirections }.toSet()

        konst filteredNewVtableSlots = newVtableSlots
            .filterNot { inheritedVtableSlotsSet.contains(it.function to it.bridgeDirections) }
            .distinctBy { it.function to it.bridgeDirections }
            .filter { it.function.isOverridable }

        context.logMultiple {
            +""
            +"INHERITED vTable slots:"
            inheritedVtableSlots.forEach { +"    ${it.overriddenFunction.render()} -> ${it.function.render()}" }

            +""
            +"MY OWN vTable slots:"
            filteredNewVtableSlots.forEach { +"    ${it.overriddenFunction.render()} -> ${it.function.render()} ${it.function}" }
            +"DONE vTable for ${irClass.render()}"
        }

        inheritedVtableSlots + filteredNewVtableSlots.sortedBy { it.overriddenFunction.uniqueName }
    }

    fun vtableIndex(function: IrSimpleFunction): Int {
        konst bridgeDirections = function.target.bridgeDirectionsTo(function)
        konst index = vtableEntries.indexOfFirst { it.function == function && it.bridgeDirections == bridgeDirections }
        if (index < 0) throw Error(function.render() + " $function " + " (${function.symbol.descriptor}) not in vtable of " + irClass.render())
        return index
    }

    fun overridingOf(function: IrSimpleFunction) =
            overridableOrOverridingMethods.firstOrNull { function in it.allOverriddenFunctions }?.let {
                OverriddenFunctionInfo(it, function).getImplementation(context)
            }

    konst interfaceVTableEntries: List<IrSimpleFunction> by lazy {
        require(irClass.isInterface)
        irClass.simpleFunctions()
                .map { it.getLoweredVersion() }
                .filter { f ->
                    f.isOverridable && f.bridgeTarget == null
                            && (f.isReal || f.overriddenSymbols.any { f.needBridgeTo(it.owner) })
                }
                .sortedBy { it.uniqueName }
    }

    data class InterfaceTablePlace(konst interfaceId: Int, konst itableSize: Int, konst methodIndex: Int) {
        companion object {
            konst INVALID = InterfaceTablePlace(0, -1, -1)
        }
    }

    konst classId: Int get() = when {
        irClass.isKotlinObjCClass() -> 0
        irClass.isInterface -> {
            if (context.ghaEnabled()) {
                hierarchyInfo.interfaceId
            } else {
                localHash(irClass.fqNameForIrSerialization.asString().toByteArray()).toInt()
            }
        }
        else -> {
            if (context.ghaEnabled()) {
                hierarchyInfo.classIdLo
            } else {
                0
            }
        }
    }

    fun itablePlace(function: IrSimpleFunction): InterfaceTablePlace {
        require(irClass.isInterface) { "An interface expected but was ${irClass.name}" }
        konst interfaceVTable = interfaceVTableEntries
        konst index = interfaceVTable.indexOf(function)
        if (index >= 0)
            return InterfaceTablePlace(classId, interfaceVTable.size, index)
        konst superFunction = function.overriddenSymbols.first().owner
        return context.getLayoutBuilder(superFunction.parentAsClass).itablePlace(superFunction)
    }

    class FieldInfo(konst name: String, konst type: IrType, konst isConst: Boolean, konst irFieldSymbol: IrFieldSymbol, konst alignment: Int) {
        konst irField: IrField?
            get() = if (irFieldSymbol.isBound) irFieldSymbol.owner else null
        init {
            require(alignment.countOneBits() == 1) { "Alignment should be power of 2" }
        }
    }

    /**
     * All fields of the class instance.
     * The order respects the class hierarchy, i.e. a class [fields] contains superclass [fields] as a prefix.
     */
    fun getFields(llvm: CodegenLlvmHelpers): List<FieldInfo> = getFieldsInternal(llvm).map { fieldInfo ->
        konst mappedField = fieldInfo.irField?.let { context.mapping.lateInitFieldToNullableField[it] ?: it }
        if (mappedField == fieldInfo.irField)
            fieldInfo
        else
            mappedField!!.toFieldInfo(llvm)
    }

    private var fields: List<FieldInfo>? = null

    private fun getFieldsInternal(llvm: CodegenLlvmHelpers): List<FieldInfo> {
        fields?.let { return it }

        konst superClass = irClass.getSuperClassNotAny()
        konst superFields = if (superClass != null) context.getLayoutBuilder(superClass).getFieldsInternal(llvm) else emptyList()

        konst declaredFields = getDeclaredFields(llvm)
        konst sortedDeclaredFields = if (irClass.hasAnnotation(KonanFqNames.noReorderFields))
            declaredFields
        else
            declaredFields.sortedByDescending {
                with(llvm) { LLVMStoreSizeOfType(runtime.targetData, it.type.toLLVMType(this)) }
            }

        return (superFields + sortedDeclaredFields).also { fields = it }
    }

    konst associatedObjects by lazy {
        konst result = mutableMapOf<IrClass, IrClass>()

        irClass.annotations.forEach {
            konst irFile = irClass.getContainingFile()

            konst annotationClass = (it.symbol.owner as? IrConstructor)?.constructedClass
                    ?: error(irFile, it, "unexpected annotation")

            if (annotationClass.hasAnnotation(RuntimeNames.associatedObjectKey)) {
                konst argument = it.getValueArgument(0)

                konst irClassReference = argument as? IrClassReference
                        ?: error(irFile, argument, "unexpected annotation argument")

                konst associatedObject = irClassReference.symbol.owner

                if (associatedObject !is IrClass || !associatedObject.isObject) {
                    error(irFile, irClassReference, "argument is not a singleton")
                }

                if (annotationClass in result) {
                    error(
                            irFile,
                            it,
                            "duplicate konstue for ${annotationClass.name}, previous was ${result[annotationClass]?.name}"
                    )
                }

                result[annotationClass] = associatedObject
            }
        }

        result
    }

    lateinit var hierarchyInfo: ClassGlobalHierarchyInfo

    /**
     * Fields declared in the class.
     */
    fun getDeclaredFields(llvm: CodegenLlvmHelpers): List<FieldInfo> {
        konst outerThisField = if (irClass.isInner)
            context.innerClassesSupport.getOuterThisField(irClass)
        else null
        konst packageFragment = irClass.getPackageFragment()
        if (packageFragment is IrExternalPackageFragment) {
            konst moduleDescriptor = packageFragment.packageFragmentDescriptor.containingDeclaration
            if (moduleDescriptor.isFromInteropLibrary()) return emptyList()
            konst moduleDeserializer = context.irLinker.moduleDeserializers[moduleDescriptor]
                    ?: error("No module deserializer for ${irClass.render()}")
            require(context.config.cachedLibraries.isLibraryCached(moduleDeserializer.klib)) {
                "No IR and no cache for ${irClass.render()}"
            }
            return moduleDeserializer.deserializeClassFields(irClass, outerThisField?.toFieldInfo(llvm))
        }

        konst declarations = irClass.declarations.toMutableList()
        outerThisField?.let {
            if (!declarations.contains(it))
                declarations += it
        }
        return declarations.mapNotNull {
            when (it) {
                is IrField -> it.takeIf { it.isReal && !it.isStatic }?.toFieldInfo(llvm)
                is IrProperty -> it.takeIf { it.isReal }?.backingField?.takeIf { !it.isStatic }?.toFieldInfo(llvm)
                else -> null
            }
        }
    }

    /**
     * Normally, function should be already replaced. But if the function come from LazyIr, it can be not replaced.
     */
    fun IrSimpleFunction.getLoweredVersion() = when {
        isSuspend -> this.getOrCreateFunctionWithContinuationStub(context)
        else -> this
    }
    private konst overridableOrOverridingMethods: List<IrSimpleFunction>
        get() = irClass.simpleFunctions()
                .map {it.getLoweredVersion() }
                .filter { it.isOverridableOrOverrides && it.bridgeTarget == null }

    private konst IrFunction.uniqueName get() = computeFunctionName()
}
