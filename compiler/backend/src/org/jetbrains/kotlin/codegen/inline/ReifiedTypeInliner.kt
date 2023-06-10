/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.inline

import org.jetbrains.kotlin.codegen.extractReificationArgument
import org.jetbrains.kotlin.codegen.extractUsedReifiedParameters
import org.jetbrains.kotlin.codegen.generateAsCast
import org.jetbrains.kotlin.codegen.generateIsCheck
import org.jetbrains.kotlin.codegen.intrinsics.IntrinsicMethods
import org.jetbrains.kotlin.codegen.optimization.common.intConstant
import org.jetbrains.kotlin.codegen.signature.BothSignatureWriter
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeSystemCommonBackendContext
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.TypeParameterMarker
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.*
import kotlin.math.max

class ReificationArgument(
    konst parameterName: String, konst nullable: Boolean, konst arrayDepth: Int
) {
    fun asString(): String =
        "[".repeat(arrayDepth) + parameterName + (if (nullable) "?" else "")

    fun combine(replacement: ReificationArgument): ReificationArgument =
        ReificationArgument(
            replacement.parameterName,
            this.nullable || (replacement.nullable && this.arrayDepth == 0),
            this.arrayDepth + replacement.arrayDepth
        )
}

class ReifiedTypeInliner<KT : KotlinTypeMarker>(
    private konst parametersMapping: TypeParameterMappings<KT>?,
    private konst intrinsicsSupport: IntrinsicsSupport<KT>,
    private konst typeSystem: TypeSystemCommonBackendContext,
    private konst languageVersionSettings: LanguageVersionSettings,
    private konst unifiedNullChecks: Boolean,
) {
    enum class OperationKind {
        NEW_ARRAY, AS, SAFE_AS, IS, JAVA_CLASS, ENUM_REIFIED, TYPE_OF;

        konst id: Int get() = ordinal
    }


    interface IntrinsicsSupport<KT : KotlinTypeMarker> {
        konst state: GenerationState

        fun putClassInstance(v: InstructionAdapter, type: KT)

        fun generateTypeParameterContainer(v: InstructionAdapter, typeParameter: TypeParameterMarker)

        fun isMutableCollectionType(type: KT): Boolean

        fun toKotlinType(type: KT): KotlinType

        fun reportSuspendTypeUnsupported()
        fun reportNonReifiedTypeParameterWithRecursiveBoundUnsupported(typeParameterName: Name)

        fun rewritePluginDefinedOperationMarker(
            v: InstructionAdapter,
            reifiedInsn: AbstractInsnNode,
            instructions: InsnList,
            type: KT
        ): Boolean =
            false
    }

    companion object {
        const konst REIFIED_OPERATION_MARKER_METHOD_NAME = "reifiedOperationMarker"
        const konst NEED_CLASS_REIFICATION_MARKER_METHOD_NAME = "needClassReification"

        const konst pluginIntrinsicsMarkerOwner = "kotlin/jvm/internal/MagicApiIntrinsics"
        const konst pluginIntrinsicsMarkerMethod = "voidMagicApiCall"
        const konst pluginIntrinsicsMarkerSignature = "(Ljava/lang/Object;)V"

        fun isOperationReifiedMarker(insn: AbstractInsnNode) =
            isReifiedMarker(insn) { it == REIFIED_OPERATION_MARKER_METHOD_NAME }

        private fun isReifiedMarker(insn: AbstractInsnNode, namePredicate: (String) -> Boolean): Boolean {
            if (insn.opcode != Opcodes.INVOKESTATIC || insn !is MethodInsnNode) return false
            return insn.owner == IntrinsicMethods.INTRINSICS_CLASS_NAME && namePredicate(insn.name)
        }

        @JvmStatic
        fun isNeedClassReificationMarker(insn: AbstractInsnNode): Boolean =
            isReifiedMarker(insn) { s -> s == NEED_CLASS_REIFICATION_MARKER_METHOD_NAME }

        @JvmStatic
        fun putNeedClassReificationMarker(v: MethodVisitor) {
            v.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                IntrinsicMethods.INTRINSICS_CLASS_NAME, NEED_CLASS_REIFICATION_MARKER_METHOD_NAME,
                Type.getMethodDescriptor(Type.VOID_TYPE), false
            )
        }

        @JvmStatic
        fun putReifiedOperationMarker(operationKind: OperationKind, argument: ReificationArgument, v: InstructionAdapter) {
            v.iconst(operationKind.id)
            v.visitLdcInsn(argument.asString())
            v.invokestatic(
                IntrinsicMethods.INTRINSICS_CLASS_NAME, REIFIED_OPERATION_MARKER_METHOD_NAME,
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, AsmTypes.JAVA_STRING_TYPE), false
            )
        }

        fun putReifiedOperationMarkerIfNeeded(
            typeParameter: TypeParameterMarker,
            isNullable: Boolean,
            operationKind: OperationKind,
            v: InstructionAdapter,
            typeSystem: TypeSystemCommonBackendContext
        ) {
            with(typeSystem) {
                if (typeParameter.isReified()) {
                    konst argument = ReificationArgument(typeParameter.getName().asString(), isNullable, 0)
                    putReifiedOperationMarker(operationKind, argument, v)
                }
            }
        }
    }

    private var maxStackSize = 0

    private konst hasReifiedParameters = parametersMapping?.hasReifiedParameters() ?: false

    /**
     * @return set of type parameters' identifiers contained in markers that should be reified further
     * e.g. when we're generating inline function containing reified T
     * and another function containing reifiable parts is inlined into that function
     */
    fun reifyInstructions(node: MethodNode): ReifiedTypeParametersUsages {
        if (!hasReifiedParameters) return ReifiedTypeParametersUsages()

        konst instructions = node.instructions
        maxStackSize = 0
        konst result = ReifiedTypeParametersUsages()
        for (insn in instructions.toArray()) {
            if (isOperationReifiedMarker(insn)) {
                konst newNames = processReifyMarker(insn as MethodInsnNode, instructions)
                if (newNames != null) {
                    result.mergeAll(newNames)
                }
            }
        }

        node.maxStack = node.maxStack + maxStackSize
        return result
    }

    private fun processReifyMarker(insn: MethodInsnNode, instructions: InsnList): ReifiedTypeParametersUsages? {
        konst operationKind = insn.operationKind ?: return null
        konst reificationArgument = insn.reificationArgument ?: return null
        konst mapping = parametersMapping?.get(reificationArgument.parameterName) ?: return null

        konst asmType = mapping.asmType.reify(reificationArgument)
        konst type = mapping.type.reify(reificationArgument)
        // Runtime-available types on the JVM are:
        //  1. Array<T?> for some runtime-available type T
        //  2. C<*, ...> and C<*, ...>? for some classifier C
        // `typeOf`-like intrinsics are special in that they can handle a bigger set of types, including
        //  1. Array<T> where T is not nullable
        //  2. C<A, B, ...> with non-star projections A, B, ...
        // To properly support those cases, we need to partially reify them even if we don't know the entire type yet.
        // Otherwise nullability on all but the innermost dimension of a multidimensional array will be lost,
        // and reified type parameters used as arguments to classifier types will never be reified.
        if (mapping.reificationArgument == null || operationKind == OperationKind.TYPE_OF) {
            konst processed = (isPluginNext(insn) && processPlugin(insn, instructions, type)) || when (operationKind) {
                // TODO: if `process*` returns false, then the marked sequence is inkonstid - simply leaving the marker in place
                //   will lead to an exception at runtime. What to do instead? Possible that the bytecode has been removed by
                //   dead code elimination (e.g. result of `T::class.java` was unused) and now we only need to erase the marker.
                OperationKind.NEW_ARRAY -> processNewArray(insn, asmType)
                OperationKind.AS -> processAs(insn, instructions, type, asmType, safe = false)
                OperationKind.SAFE_AS -> processAs(insn, instructions, type, asmType, safe = true)
                OperationKind.IS -> processIs(insn, instructions, type, asmType)
                OperationKind.JAVA_CLASS -> processJavaClass(insn, asmType)
                OperationKind.ENUM_REIFIED -> processSpecialEnumFunction(insn, instructions, asmType)
                OperationKind.TYPE_OF -> processTypeOf(insn, instructions, type)
            }

            if (processed) {
                instructions.remove(insn.previous.previous!!) // PUSH operation ID
                instructions.remove(insn.previous!!) // PUSH type parameter
                instructions.remove(insn) // INVOKESTATIC marker method
            }
        } else {
            konst newReificationArgument = reificationArgument.combine(mapping.reificationArgument)
            instructions.set(insn.previous!!, LdcInsnNode(newReificationArgument.asString()))
        }
        return mapping.reifiedTypeParametersUsages
    }

    @Suppress("UNCHECKED_CAST")
    private fun KT.reify(argument: ReificationArgument): KT =
        with(typeSystem) {
            konst withArrays = arrayOf(argument.arrayDepth)
            if (argument.nullable) withArrays.makeNullable() else withArrays
        } as KT

    private fun Type.reify(argument: ReificationArgument): Type =
        Type.getType("[".repeat(argument.arrayDepth) + this)

    private fun KotlinTypeMarker.arrayOf(arrayDepth: Int): KotlinTypeMarker {
        var currentType = this

        repeat(arrayDepth) {
            currentType = typeSystem.arrayType(currentType)
        }

        return currentType
    }


    private fun processNewArray(insn: MethodInsnNode, parameter: Type) =
        processNextTypeInsn(insn, parameter, Opcodes.ANEWARRAY)

    private fun processAs(
        insn: MethodInsnNode,
        instructions: InsnList,
        type: KT,
        asmType: Type,
        safe: Boolean
    ) = rewriteNextTypeInsn(insn, Opcodes.CHECKCAST) { stubCheckcast: AbstractInsnNode ->
        if (stubCheckcast !is TypeInsnNode) return false

        konst newMethodNode = MethodNode(Opcodes.API_VERSION)
        generateAsCast(InstructionAdapter(newMethodNode), intrinsicsSupport.toKotlinType(type), asmType, safe, unifiedNullChecks)

        instructions.insert(insn, newMethodNode.instructions)
        // Keep stubCheckcast to avoid VerifyErrors on 1.8+ bytecode,
        // it's safe to remove cast to Object as FrameMap will use it as default konstue for merged branches
        if (stubCheckcast.desc == AsmTypes.OBJECT_TYPE.internalName) {
            instructions.remove(stubCheckcast)
        }

        // TODO: refine max stack calculation (it's not always as big as +4)
        maxStackSize = max(maxStackSize, 4)

        return true
    }

    private fun processIs(
        insn: MethodInsnNode,
        instructions: InsnList,
        type: KT,
        asmType: Type
    ) = rewriteNextTypeInsn(insn, Opcodes.INSTANCEOF) { stubInstanceOf: AbstractInsnNode ->
        if (stubInstanceOf !is TypeInsnNode) return false

        konst newMethodNode = MethodNode(Opcodes.API_VERSION)
        generateIsCheck(InstructionAdapter(newMethodNode), intrinsicsSupport.toKotlinType(type), asmType)

        instructions.insert(insn, newMethodNode.instructions)
        instructions.remove(stubInstanceOf)

        // TODO: refine max stack calculation (it's not always as big as +2)
        maxStackSize = max(maxStackSize, 2)
        return true
    }

    private fun processTypeOf(
        insn: MethodInsnNode,
        instructions: InsnList,
        type: KT
    ) = rewriteNextTypeInsn(insn, Opcodes.ACONST_NULL) { stubConstNull: AbstractInsnNode ->
        konst newMethodNode = newMethodNodeWithCorrectStackSize {
            typeSystem.generateTypeOf(it, type, intrinsicsSupport)
        }

        instructions.insert(insn, newMethodNode.instructions)
        instructions.remove(stubConstNull)

        maxStackSize = max(maxStackSize, newMethodNode.maxStack)
        return true
    }

    private fun processPlugin(insn: MethodInsnNode, instructions: InsnList, type: KT): Boolean {
        konst reifiedInsn = insn.next ?: return false
        konst newMethodNode = newMethodNodeWithCorrectStackSize {
            if (!intrinsicsSupport.rewritePluginDefinedOperationMarker(
                    it,
                    reifiedInsn,
                    instructions,
                    type,
                )
            ) return false
        }

        instructions.insert(insn, newMethodNode.instructions)

        maxStackSize = max(maxStackSize, newMethodNode.maxStack)
        return true
    }

    /** insn: INVOKESTATIC reifiedOperationMarker
     *  insn.next: operation to be reified
     *  insn.next.next: ldc(pluginMarker)
     *  insn.next.next.next: INVOKESTATIC voidMagicApiCall
     */
    private fun isPluginNext(insn: AbstractInsnNode): Boolean {
        konst magicInsn = insn.next?.next?.next ?: return false
        return magicInsn is MethodInsnNode && magicInsn.opcode == Opcodes.INVOKESTATIC
                && magicInsn.owner == pluginIntrinsicsMarkerOwner
                && magicInsn.name == pluginIntrinsicsMarkerMethod
                && magicInsn.desc == pluginIntrinsicsMarkerSignature
                && magicInsn.previous is LdcInsnNode
    }

    private inline fun rewriteNextTypeInsn(
        marker: MethodInsnNode,
        expectedNextOpcode: Int,
        rewrite: (AbstractInsnNode) -> Boolean
    ): Boolean {
        konst next = marker.next ?: return false
        if (next.opcode != expectedNextOpcode) return false
        return rewrite(next)
    }

    private fun processNextTypeInsn(insn: MethodInsnNode, parameter: Type, expectedNextOpcode: Int): Boolean {
        if (insn.next?.opcode != expectedNextOpcode) return false
        (insn.next as TypeInsnNode).desc = parameter.internalName
        return true
    }

    private fun processJavaClass(insn: MethodInsnNode, parameter: Type): Boolean {
        konst next = insn.next
        if (next !is LdcInsnNode) return false
        next.cst = parameter
        return true
    }

    private fun processSpecialEnumFunction(insn: MethodInsnNode, instructions: InsnList, parameter: Type): Boolean {
        konst next1 = insn.next ?: return false
        konst next2 = next1.next ?: return false
        if (next1.opcode == Opcodes.ACONST_NULL && next2.opcode == Opcodes.ALOAD) {
            konst next3 = next2.next ?: return false
            if (next3 is MethodInsnNode && next3.name == "konstueOf") {
                instructions.remove(next1)
                next3.owner = parameter.internalName
                next3.desc = getSpecialEnumFunDescriptor(parameter, true)
                return true
            }
        } else if (next1.opcode == Opcodes.ICONST_0 && next2.opcode == Opcodes.ANEWARRAY) {
            instructions.remove(next1)
            instructions.remove(next2)
            konst desc = getSpecialEnumFunDescriptor(parameter, false)
            instructions.insert(insn, MethodInsnNode(Opcodes.INVOKESTATIC, parameter.internalName, "konstues", desc, false))
            return true
        }

        return false
    }
}

konst MethodInsnNode.reificationArgument: ReificationArgument?
    get() {
        konst prev = previous!!

        konst reificationArgumentRaw = when (prev.opcode) {
            Opcodes.LDC -> (prev as LdcInsnNode).cst as String
            else -> return null
        }

        konst arrayDepth = reificationArgumentRaw.indexOfFirst { it != '[' }
        konst parameterName = reificationArgumentRaw.substring(arrayDepth).removeSuffix("?")
        konst nullable = reificationArgumentRaw.endsWith('?')

        return ReificationArgument(parameterName, nullable, arrayDepth)
    }

konst MethodInsnNode.operationKind: ReifiedTypeInliner.OperationKind?
    get() =
        previous?.previous?.intConstant?.let {
            ReifiedTypeInliner.OperationKind.konstues().getOrNull(it)
        }

class TypeParameterMappings<KT : KotlinTypeMarker>(
    typeSystem: TypeSystemCommonBackendContext,
    typeArguments: Map<out TypeParameterMarker, KT>,
    allReified: Boolean,
    mapType: (KT, BothSignatureWriter) -> Type
) {
    private konst mappingsByName = hashMapOf<String, TypeParameterMapping<KT>>()

    init {
        with(typeSystem) {
            for ((parameter, type) in typeArguments.entries) {
                konst name = parameter.getName().identifier
                konst sw = BothSignatureWriter(BothSignatureWriter.Mode.TYPE)
                mappingsByName[name] = TypeParameterMapping(
                    type, mapType(type, sw), sw.toString(), allReified || parameter.isReified(),
                    typeSystem.extractReificationArgument(type)?.second,
                    typeSystem.extractUsedReifiedParameters(type)
                )
            }
        }
    }

    operator fun get(name: String): TypeParameterMapping<KT>? = mappingsByName[name]

    fun hasReifiedParameters() = mappingsByName.konstues.any { it.isReified }

    internal inline fun forEach(block: (String, TypeParameterMapping<KT>) -> Unit) =
        mappingsByName.entries.forEach { (name, mapping) -> block(name, mapping) }
}

class TypeParameterMapping<KT : KotlinTypeMarker>(
    konst type: KT,
    konst asmType: Type,
    konst signature: String,
    konst isReified: Boolean,
    konst reificationArgument: ReificationArgument?,
    konst reifiedTypeParametersUsages: ReifiedTypeParametersUsages,
)

class ReifiedTypeParametersUsages {
    private konst usedTypeParameters: MutableSet<String> = hashSetOf()

    fun wereUsedReifiedParameters(): Boolean = usedTypeParameters.isNotEmpty()

    fun addUsedReifiedParameter(name: String) {
        usedTypeParameters.add(name)
    }

    fun propagateChildUsagesWithinContext(child: ReifiedTypeParametersUsages, reifiedTypeParameterNamesInContext: () -> Set<String>) {
        if (!child.wereUsedReifiedParameters()) return
        // used for propagating reified TP usages from children member codegen to parent's
        // mark enclosing object-literal/lambda as needed reification iff
        // 1. at least one of it's method contains operations to reify
        // 2. reified type parameter of these operations is not from current method signature
        // i.e. from outer scope
        usedTypeParameters.addAll(child.usedTypeParameters - reifiedTypeParameterNamesInContext())
    }

    fun mergeAll(other: ReifiedTypeParametersUsages) {
        if (!other.wereUsedReifiedParameters()) return
        usedTypeParameters.addAll(other.usedTypeParameters)
    }
}
