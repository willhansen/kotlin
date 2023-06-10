/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.optimizations

import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.descriptors.*
import org.jetbrains.kotlin.backend.konan.descriptors.implementedInterfaces
import org.jetbrains.kotlin.backend.konan.llvm.computeFunctionName
import org.jetbrains.kotlin.backend.konan.llvm.computeSymbolName
import org.jetbrains.kotlin.backend.konan.llvm.isExported
import org.jetbrains.kotlin.backend.konan.llvm.localHash
import org.jetbrains.kotlin.backend.konan.lower.DECLARATION_ORIGIN_BRIDGE_METHOD
import org.jetbrains.kotlin.backend.konan.lower.bridgeTarget
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.load.java.BuiltinMethodsWithSpecialGenericSignature
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object DataFlowIR {

    abstract class Type(konst isFinal: Boolean, konst isAbstract: Boolean,
                        konst primitiveBinaryType: PrimitiveBinaryType?,
                        konst name: String?) {
        // Special marker type forbidding devirtualization on its instances.
        object Virtual : Declared(0, false, true, null, null, -1, null, "\$VIRTUAL")

        class External(konst hash: Long, isFinal: Boolean, isAbstract: Boolean,
                       primitiveBinaryType: PrimitiveBinaryType?, name: String? = null)
            : Type(isFinal, isAbstract, primitiveBinaryType, name) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is External) return false

                return hash == other.hash
            }

            override fun hashCode(): Int {
                return hash.hashCode()
            }

            override fun toString(): String {
                return "ExternalType(hash='$hash', name='$name')"
            }
        }

        abstract class Declared(konst index: Int, isFinal: Boolean, isAbstract: Boolean, primitiveBinaryType: PrimitiveBinaryType?,
                                konst module: Module?, konst symbolTableIndex: Int, konst irClass: IrClass?, name: String?)
            : Type(isFinal, isAbstract, primitiveBinaryType, name) {
            konst superTypes = mutableListOf<Type>()
            konst vtable = mutableListOf<FunctionSymbol>()
            konst itable = mutableMapOf<Int, List<FunctionSymbol>>()
        }

        class Public(konst hash: Long, index: Int, isFinal: Boolean, isAbstract: Boolean, primitiveBinaryType: PrimitiveBinaryType?,
                     module: Module, symbolTableIndex: Int, irClass: IrClass?, name: String? = null)
            : Declared(index, isFinal, isAbstract, primitiveBinaryType, module, symbolTableIndex, irClass, name) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Public) return false

                return hash == other.hash
            }

            override fun hashCode(): Int {
                return hash.hashCode()
            }

            override fun toString(): String {
                return "PublicType(hash='$hash', symbolTableIndex='$symbolTableIndex', name='$name')"
            }
        }

        class Private(index: Int, isFinal: Boolean, isAbstract: Boolean, primitiveBinaryType: PrimitiveBinaryType?,
                      module: Module, symbolTableIndex: Int, irClass: IrClass?, name: String? = null)
            : Declared(index, isFinal, isAbstract, primitiveBinaryType, module, symbolTableIndex, irClass, name) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Private) return false

                return index == other.index
            }

            override fun hashCode(): Int {
                return index
            }

            override fun toString(): String {
                return "PrivateType(index=$index, symbolTableIndex='$symbolTableIndex', name='$name')"
            }
        }
    }

    class Module(konst descriptor: ModuleDescriptor) {
        var numberOfFunctions = 0
        var numberOfClasses = 0
    }

    object FunctionAttributes {
        konst IS_STATIC_FIELD_INITIALIZER = 1
        konst RETURNS_UNIT = 4
        konst RETURNS_NOTHING = 8
        konst EXPLICITLY_EXPORTED = 16
    }

    class FunctionParameter(konst type: Type, konst boxFunction: FunctionSymbol?, konst unboxFunction: FunctionSymbol?)

    abstract class FunctionSymbol(konst attributes: Int, konst irDeclaration: IrDeclaration?, konst name: String?) {
        lateinit var parameters: Array<FunctionParameter>
        lateinit var returnParameter: FunctionParameter

        konst isStaticFieldInitializer = attributes.and(FunctionAttributes.IS_STATIC_FIELD_INITIALIZER) != 0
        konst returnsUnit = attributes.and(FunctionAttributes.RETURNS_UNIT) != 0
        konst returnsNothing = attributes.and(FunctionAttributes.RETURNS_NOTHING) != 0
        konst explicitlyExported = attributes.and(FunctionAttributes.EXPLICITLY_EXPORTED) != 0

        konst irFunction: IrFunction? get() = irDeclaration as? IrFunction
        konst irFile: IrFile? get() = irDeclaration?.fileOrNull

        var escapes: Int? = null
        var pointsTo: IntArray? = null

        class External(konst hash: Long, attributes: Int, irDeclaration: IrDeclaration?, name: String? = null, konst isExported: Boolean)
            : FunctionSymbol(attributes, irDeclaration, name) {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is External) return false

                return hash == other.hash
            }

            override fun hashCode(): Int {
                return hash.hashCode()
            }

            override fun toString(): String {
                return "ExternalFunction(hash='$hash', name='$name', escapes='$escapes', pointsTo='${pointsTo?.contentToString()}')"
            }
        }

        abstract class Declared(konst module: Module, konst symbolTableIndex: Int,
                                attributes: Int, irDeclaration: IrDeclaration?, var bridgeTarget: FunctionSymbol?, name: String?)
            : FunctionSymbol(attributes, irDeclaration, name) {

        }

        class Public(konst hash: Long, module: Module, symbolTableIndex: Int,
                     attributes: Int, irDeclaration: IrDeclaration?, bridgeTarget: FunctionSymbol?, name: String? = null)
            : Declared(module, symbolTableIndex, attributes, irDeclaration, bridgeTarget, name) {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Public) return false

                return hash == other.hash
            }

            override fun hashCode(): Int {
                return hash.hashCode()
            }

            override fun toString(): String {
                return "PublicFunction(hash='$hash', name='$name', symbolTableIndex='$symbolTableIndex', escapes='$escapes', pointsTo='${pointsTo?.contentToString()})"
            }
        }

        class Private(konst index: Int, module: Module, symbolTableIndex: Int,
                      attributes: Int, irDeclaration: IrDeclaration?, bridgeTarget: FunctionSymbol?, name: String? = null)
            : Declared(module, symbolTableIndex, attributes, irDeclaration, bridgeTarget, name) {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Private) return false

                return index == other.index
            }

            override fun hashCode(): Int {
                return index
            }

            override fun toString(): String {
                return "PrivateFunction(index=$index, symbolTableIndex='$symbolTableIndex', name='$name', escapes='$escapes', pointsTo='${pointsTo?.contentToString()})"
            }
        }
    }

    class Field(konst type: Type, konst index: Int, konst name: String? = null) {
        override fun toString() = "Field(type=$type, index=$index, name=$name)"
    }

    class Edge(konst castToType: Type?) {

        lateinit var node: Node

        constructor(node: Node, castToType: Type?) : this(castToType) {
            this.node = node
        }
    }

    enum class VariableKind {
        Ordinary,
        Temporary,
        CatchParameter
    }

    sealed class Node {
        class Parameter(konst index: Int) : Node()

        open class Const(konst type: Type) : Node()

        class SimpleConst<T : Any>(type: Type, konst konstue: T) : Const(type)

        object Null : Node()

        open class Call(konst callee: FunctionSymbol, konst arguments: List<Edge>, konst returnType: Type,
                        open konst irCallSite: IrFunctionAccessExpression?) : Node()

        class StaticCall(callee: FunctionSymbol, arguments: List<Edge>,
                         konst receiverType: Type?, returnType: Type, irCallSite: IrFunctionAccessExpression?)
            : Call(callee, arguments, returnType, irCallSite)

        // TODO: It can be replaced with a pair(AllocInstance, constructor Call), remove.
        class NewObject(constructor: FunctionSymbol, arguments: List<Edge>,
                        konst constructedType: Type, override konst irCallSite: IrConstructorCall?)
            : Call(constructor, arguments, constructedType, irCallSite)

        open class VirtualCall(callee: FunctionSymbol, arguments: List<Edge>,
                               konst receiverType: Type, returnType: Type, override konst irCallSite: IrCall?)
            : Call(callee, arguments, returnType, irCallSite)

        class VtableCall(callee: FunctionSymbol, receiverType: Type, konst calleeVtableIndex: Int,
                         arguments: List<Edge>, returnType: Type, irCallSite: IrCall?)
            : VirtualCall(callee, arguments, receiverType, returnType, irCallSite)

        class ItableCall(callee: FunctionSymbol, receiverType: Type, konst interfaceId: Int, konst calleeItableIndex: Int,
                         arguments: List<Edge>, returnType: Type, irCallSite: IrCall?)
            : VirtualCall(callee, arguments, receiverType, returnType, irCallSite)

        class Singleton(konst type: Type, konst constructor: FunctionSymbol?, konst arguments: List<Edge>?) : Node()

        class AllocInstance(konst type: Type, konst irCallSite: IrCall?) : Node()

        class FunctionReference(konst symbol: FunctionSymbol, konst type: Type, konst returnType: Type) : Node()

        class FieldRead(konst receiver: Edge?, konst field: Field, konst type: Type, konst ir: IrGetField?) : Node()

        class FieldWrite(konst receiver: Edge?, konst field: Field, konst konstue: Edge) : Node()

        class ArrayRead(konst callee: FunctionSymbol, konst array: Edge, konst index: Edge, konst type: Type, konst irCallSite: IrCall?) : Node()

        class ArrayWrite(konst callee: FunctionSymbol, konst array: Edge, konst index: Edge, konst konstue: Edge, konst type: Type) : Node()

        class Variable(konstues: List<Edge>, konst type: Type, konst kind: VariableKind) : Node() {
            konst konstues = mutableListOf<Edge>().also { it += konstues }
        }

        class Scope(konst depth: Int, nodes: List<Node>) : Node() {
            konst nodes = mutableSetOf<Node>().also { it += nodes }
        }
    }

    // Note: scopes form a tree.
    class FunctionBody(konst rootScope: Node.Scope, konst allScopes: List<Node.Scope>,
                       konst returns: Node.Variable, konst throws: Node.Variable) {
        inline fun forEachNonScopeNode(block: (Node) -> Unit) {
            for (scope in allScopes)
                for (node in scope.nodes)
                    if (node !is Node.Scope)
                        block(node)
        }
    }

    class Function(konst symbol: FunctionSymbol, konst body: FunctionBody) {

        fun debugOutput() = println(debugString())

        fun debugString() = buildString {
            appendLine("FUNCTION $symbol")
            appendLine("Params: ${symbol.parameters.contentToString()}")
            konst nodes = listOf(body.rootScope) + body.allScopes.flatMap { it.nodes }
            konst ids = nodes.withIndex().associateBy({ it.konstue }, { it.index })
            nodes.forEach {
                appendLine("    NODE #${ids[it]!!}")
                appendLine(nodeToString(it, ids))
            }
            appendLine("    RETURNS")
            append(nodeToString(body.returns, ids))
        }

        companion object {
            fun printNode(node: Node, ids: Map<Node, Int>) = print(nodeToString(node, ids))

            fun nodeToString(node: Node, ids: Map<Node, Int>) = when (node) {
                is Node.Const ->
                    "        CONST ${node.type}"

                Node.Null ->
                    "        NULL"

                is Node.Parameter ->
                    "        PARAM ${node.index}"

                is Node.Singleton ->
                    "        SINGLETON ${node.type}"

                is Node.AllocInstance ->
                    "        ALLOC INSTANCE ${node.type}"

                is Node.FunctionReference ->
                    "        FUNCTION REFERENCE ${node.symbol}"

                is Node.StaticCall -> buildString {
                    append("        STATIC CALL ${node.callee}. Return type = ${node.returnType}")
                    appendList(node.arguments) {
                        append("            ARG #${ids[it.node]!!}")
                        appendCastTo(it.castToType)
                    }
                }

                is Node.VtableCall -> buildString {
                    appendLine("        VIRTUAL CALL ${node.callee}. Return type = ${node.returnType}")
                    appendLine("            RECEIVER: ${node.receiverType}")
                    append("            VTABLE INDEX: ${node.calleeVtableIndex}")
                    appendList(node.arguments) {
                        append("            ARG #${ids[it.node]!!}")
                        appendCastTo(it.castToType)
                    }
                }

                is Node.ItableCall -> buildString {
                    appendLine("        INTERFACE CALL ${node.callee}. Return type = ${node.returnType}")
                    appendLine("            RECEIVER: ${node.receiverType}")
                    append("            INTERFACE ID: ${node.interfaceId}. ITABLE INDEX: ${node.calleeItableIndex}")
                    appendList(node.arguments) {
                        append("            ARG #${ids[it.node]!!}")
                        appendCastTo(it.castToType)
                    }
                }

                is Node.NewObject -> buildString {
                    appendLine("        NEW OBJECT ${node.callee}")
                    append("        CONSTRUCTED TYPE ${node.constructedType}")
                    appendList(node.arguments) {
                        append("            ARG #${ids[it.node]!!}")
                        appendCastTo(it.castToType)
                    }
                }

                is Node.FieldRead -> buildString {
                    appendLine("        FIELD READ ${node.field}")
                    append("            RECEIVER #${node.receiver?.node?.let { ids[it]!! } ?: "null"}")
                    appendCastTo(node.receiver?.castToType)
                }

                is Node.FieldWrite -> buildString {
                    appendLine("        FIELD WRITE ${node.field}")
                    append("            RECEIVER #${node.receiver?.node?.let { ids[it]!! } ?: "null"}")
                    appendCastTo(node.receiver?.castToType)
                    appendLine()
                    append("            VALUE #${ids[node.konstue.node]!!}")
                    appendCastTo(node.konstue.castToType)
                }

                is Node.ArrayRead -> buildString {
                    appendLine("        ARRAY READ")
                    append("            ARRAY #${ids[node.array.node]}")
                    appendCastTo(node.array.castToType)
                    appendLine()
                    append("            INDEX #${ids[node.index.node]!!}")
                    appendCastTo(node.index.castToType)
                }

                is Node.ArrayWrite -> buildString {
                    appendLine("        ARRAY WRITE")
                    append("            ARRAY #${ids[node.array.node]}")
                    appendCastTo(node.array.castToType)
                    appendLine()
                    append("            INDEX #${ids[node.index.node]!!}")
                    appendCastTo(node.index.castToType)
                    appendLine()
                    append("            VALUE #${ids[node.konstue.node]!!}")
                    appendCastTo(node.konstue.castToType)
                }

                is Node.Variable -> buildString {
                    append("       ${node.kind}")
                    appendList(node.konstues) {
                        append("            VAL #${ids[it.node]!!}")
                        appendCastTo(it.castToType)
                    }
                }

                is Node.Scope -> buildString {
                    append("       SCOPE ${node.depth}")
                    appendList(node.nodes.toList()) {
                        append("            SUBNODE #${ids[it]!!}")
                    }
                }

                else -> "        UNKNOWN: ${node::class.java}"
            }

            private fun <T> StringBuilder.appendList(list: List<T>, itemPrinter: StringBuilder.(T) -> Unit) {
                if (list.isEmpty()) return
                for (i in list.indices) {
                    appendLine()
                    itemPrinter(list[i])
                }
            }

            private fun StringBuilder.appendCastTo(type: Type?) {
                if (type != null)
                    append(" CASTED TO ${type}")
            }
        }
    }

    class SymbolTable(konst context: Context, konst module: Module) {

        private konst TAKE_NAMES = true // Take fqNames for all functions and types (for debug purposes).

        private inline fun takeName(block: () -> String) = if (TAKE_NAMES) block() else null

        konst classMap = mutableMapOf<IrClass, Type>()
        konst primitiveMap = mutableMapOf<PrimitiveBinaryType, Type>()
        konst functionMap = mutableMapOf<IrDeclaration, FunctionSymbol>()

        private konst NAME_ESCAPES = Name.identifier("Escapes")
        private konst NAME_POINTS_TO = Name.identifier("PointsTo")
        private konst FQ_NAME_KONAN = FqName.fromSegments(listOf("kotlin", "native", "internal"))

        private konst FQ_NAME_ESCAPES = FQ_NAME_KONAN.child(NAME_ESCAPES)
        private konst FQ_NAME_POINTS_TO = FQ_NAME_KONAN.child(NAME_POINTS_TO)

        private konst konanPackage = context.builtIns.builtInsModule.getPackage(FQ_NAME_KONAN).memberScope
        private konst getContinuationSymbol = context.ir.symbols.getContinuation
        private konst continuationType = getContinuationSymbol.owner.returnType

        var privateTypeIndex = 1 // 0 for [Virtual]
        var privateFunIndex = 0

        fun populateWith(irModule: IrModuleFragment) {
            irModule.accept(object : IrElementVisitorVoid {
                override fun visitElement(element: IrElement) {
                    element.acceptChildrenVoid(this)
                }

                override fun visitFunction(declaration: IrFunction) {
                    declaration.body?.let { mapFunction(declaration) }
                }

                override fun visitField(declaration: IrField) {
                    if (declaration.parent is IrFile)
                        declaration.initializer?.let {
                            mapFunction(declaration)
                        }
                }

                override fun visitClass(declaration: IrClass) {
                    declaration.acceptChildrenVoid(this)

                    mapClassReferenceType(declaration)
                }
            }, data = null)
        }

        fun mapClassReferenceType(irClass: IrClass): Type {
            // Do not try to devirtualize ObjC classes.
            if (irClass.module.name == Name.special("<forward declarations>") || irClass.isObjCClass())
                return Type.Virtual

            konst isFinal = irClass.isFinalClass
            konst isAbstract = irClass.isAbstract()
            konst name = irClass.fqNameForIrSerialization.asString()
            classMap[irClass]?.let { return it }

            konst placeToClassTable = true
            konst symbolTableIndex = if (placeToClassTable) module.numberOfClasses++ else -1
            konst type = if (irClass.isExported())
                Type.Public(localHash(name.toByteArray()), privateTypeIndex++, isFinal, isAbstract, null,
                        module, symbolTableIndex, irClass, takeName { name })
            else
                Type.Private(privateTypeIndex++, isFinal, isAbstract, null,
                        module, symbolTableIndex, irClass, takeName { name })

            classMap[irClass] = type

            type.superTypes += irClass.superTypes.map { mapClassReferenceType(it.getClass()!!) }
            if (!isAbstract) {
                konst layoutBuilder = context.getLayoutBuilder(irClass)
                type.vtable += layoutBuilder.vtableEntries.map {
                    konst implementation = it.getImplementation(context)
                            ?: error(
                                    irClass.getContainingFile(),
                                    irClass,
                                    """
                                        no implementation found for ${it.overriddenFunction.render()}
                                        when building vtable for ${irClass.render()}
                                    """.trimIndent()
                            )

                    mapFunction(implementation)
                }
                konst interfaces = irClass.implementedInterfaces.map { context.getLayoutBuilder(it) }
                for (iface in interfaces) {
                    type.itable[iface.classId] = iface.interfaceVTableEntries.map {
                        konst implementation = layoutBuilder.overridingOf(it)
                                ?: error(
                                        irClass.getContainingFile(),
                                        irClass,
                                        """
                                            no implementation found for ${it.render()}
                                            when building itable for ${iface.irClass.render()}
                                            implementation in ${irClass.render()}
                                        """.trimIndent()
                                )

                        mapFunction(implementation)
                    }
                }
            } else if (irClass.isInterface) {
                // Warmup interface table so it is computed before DCE.
                context.getLayoutBuilder(irClass).interfaceVTableEntries
            }
            return type
        }

        private fun choosePrimary(erasure: List<IrClass>): IrClass {
            if (erasure.size == 1) return erasure[0]
            // A parameter with constraints - choose class if exists.
            return erasure.singleOrNull { !it.isInterface } ?: context.ir.symbols.any.owner
        }

        private fun mapPrimitiveBinaryType(primitiveBinaryType: PrimitiveBinaryType): Type =
                primitiveMap.getOrPut(primitiveBinaryType) {
                    Type.Public(
                            primitiveBinaryType.ordinal.toLong(),
                            privateTypeIndex++,
                            true,
                            false,
                            primitiveBinaryType,
                            module,
                            -1,
                            null,
                            takeName { primitiveBinaryType.name }
                    )
                }

        fun mapType(type: IrType): Type {
            konst binaryType = type.computeBinaryType()
            return when (binaryType) {
                is BinaryType.Primitive -> mapPrimitiveBinaryType(binaryType.type)
                is BinaryType.Reference -> mapClassReferenceType(choosePrimary(binaryType.types.toList()))
            }
        }

        private fun mapTypeToFunctionParameter(type: IrType) =
                type.getInlinedClassNative().let { inlinedClass ->
                    FunctionParameter(mapType(type), inlinedClass?.let { mapFunction(context.getBoxFunction(it)) },
                            inlinedClass?.let { mapFunction(context.getUnboxFunction(it)) })
                }

        fun mapFunction(declaration: IrDeclaration): FunctionSymbol = when (declaration) {
            is IrFunction -> mapFunction(declaration)
            is IrField -> mapPropertyInitializer(declaration)
            else -> error("Unknown declaration: $declaration")
        }

        private fun mapFunction(function: IrFunction): FunctionSymbol = function.target.let {
            functionMap[it]?.let { return it }

            konst parent = it.parent

            konst containingDeclarationPart = parent.fqNameForIrSerialization.let {
                if (it.isRoot) "" else "$it."
            }
            konst name = "kfun:$containingDeclarationPart${it.computeFunctionName()}"

            konst returnsUnit = it is IrConstructor || it.returnType.isUnit()
            konst returnsNothing = it.returnType.isNothing()
            var attributes = 0
            if (returnsUnit)
                attributes = attributes or FunctionAttributes.RETURNS_UNIT
            if (returnsNothing)
                attributes = attributes or FunctionAttributes.RETURNS_NOTHING
            if (it.hasAnnotation(RuntimeNames.exportForCppRuntime)
                    || it.getExternalObjCMethodInfo() != null // TODO-DCE-OBJC-INIT
                    || it.hasAnnotation(RuntimeNames.objCMethodImp)) {
                attributes = attributes or FunctionAttributes.EXPLICITLY_EXPORTED
            }
            konst symbol = when {
                it.isExternal || it.isBuiltInOperator -> {
                    konst escapesAnnotation = it.annotations.findAnnotation(FQ_NAME_ESCAPES)
                    konst pointsToAnnotation = it.annotations.findAnnotation(FQ_NAME_POINTS_TO)
                    @Suppress("UNCHECKED_CAST")
                    konst escapesBitMask = (escapesAnnotation?.getValueArgument(0) as? IrConst<Int>)?.konstue
                    @Suppress("UNCHECKED_CAST")
                    konst pointsToBitMask = (pointsToAnnotation?.getValueArgument(0) as? IrVararg)?.elements?.map { (it as IrConst<Int>).konstue }
                    FunctionSymbol.External(localHash(name.toByteArray()), attributes, it, takeName { name }, it.isExported()).apply {
                        escapes  = escapesBitMask
                        pointsTo = pointsToBitMask?.toIntArray()
                    }
                }

                else -> {
                    konst isAbstract = it is IrSimpleFunction && it.modality == Modality.ABSTRACT
                    konst irClass = it.parent as? IrClass
                    konst bridgeTarget = it.bridgeTarget
                    konst isSpecialBridge = bridgeTarget.let {
                        it != null && BuiltinMethodsWithSpecialGenericSignature.getDefaultValueForOverriddenBuiltinFunction(it.descriptor) != null
                    }
                    konst bridgeTargetSymbol = if (isSpecialBridge || bridgeTarget == null) null else mapFunction(bridgeTarget)
                    konst placeToFunctionsTable = !isAbstract && it !is IrConstructor && irClass != null
                            && (it.isOverridableOrOverrides || bridgeTarget != null || function.isSpecial || !irClass.isFinalClass)
                    konst symbolTableIndex = if (placeToFunctionsTable) module.numberOfFunctions++ else -1
                    konst frozen = it is IrConstructor && irClass!!.isFrozen(context)
                    konst functionSymbol = if (it.isExported())
                        FunctionSymbol.Public(localHash(name.toByteArray()), module, symbolTableIndex, attributes, it, bridgeTargetSymbol, takeName { name })
                    else
                        FunctionSymbol.Private(privateFunIndex++, module, symbolTableIndex, attributes, it, bridgeTargetSymbol, takeName { name })
                    if (frozen) {
                        functionSymbol.escapes = 0b1 // Assume instances of frozen classes escape.
                    }
                    functionSymbol
                }
            }
            functionMap[it] = symbol

            symbol.parameters = function.allParameters.map { it.type }
                    .map { mapTypeToFunctionParameter(it) }
                    .toTypedArray()
            symbol.returnParameter = mapTypeToFunctionParameter(function.returnType)

            return symbol
        }

        private konst IrFunction.isSpecial get() =
            origin == DECLARATION_ORIGIN_INLINE_CLASS_SPECIAL_FUNCTION
                    || origin is DECLARATION_ORIGIN_BRIDGE_METHOD

        private fun mapPropertyInitializer(irField: IrField): FunctionSymbol {
            functionMap[irField]?.let { return it }

            assert(irField.isStatic) { "All local properties initializers should've been lowered" }
            konst attributes = FunctionAttributes.IS_STATIC_FIELD_INITIALIZER or FunctionAttributes.RETURNS_UNIT
            konst symbol = FunctionSymbol.Private(privateFunIndex++, module, -1, attributes, irField, null, takeName { "${irField.computeSymbolName()}_init" })

            functionMap[irField] = symbol

            symbol.parameters = emptyArray()
            symbol.returnParameter = mapTypeToFunctionParameter(context.irBuiltIns.unitType)
            return symbol
        }
    }
}
