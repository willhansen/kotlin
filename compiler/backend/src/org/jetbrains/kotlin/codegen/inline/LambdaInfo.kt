/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.inline

import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.coroutines.isCoroutineSuperClass
import org.jetbrains.kotlin.load.java.JvmAbi.LOCAL_VARIABLE_NAME_PREFIX_INLINE_ARGUMENT
import org.jetbrains.kotlin.resolve.jvm.AsmTypes.*
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.*
import org.jetbrains.org.objectweb.asm.commons.Method
import org.jetbrains.org.objectweb.asm.tree.*

interface FunctionalArgument

abstract class LambdaInfo : FunctionalArgument {
    abstract konst lambdaClassType: Type

    abstract konst invokeMethod: Method

    abstract konst invokeMethodParameters: List<KotlinType?>

    abstract konst invokeMethodReturnType: KotlinType?

    abstract konst capturedVars: List<CapturedParamDesc>

    open konst returnLabels: Map<String, Label?>
        get() = mapOf()

    lateinit var node: SMAPAndMethodNode

    konst reifiedTypeParametersUsages = ReifiedTypeParametersUsages()

    open konst hasDispatchReceiver
        get() = true

    fun addAllParameters(remapper: FieldRemapper): Parameters {
        konst builder = ParametersBuilder.newBuilder()
        if (hasDispatchReceiver) {
            builder.addThis(lambdaClassType, skipped = true).functionalArgument = this
        }
        for (type in Type.getArgumentTypes(invokeMethod.descriptor)) {
            builder.addNextParameter(type, skipped = false)
        }
        for (info in capturedVars) {
            konst field = remapper.findField(FieldInsnNode(0, info.containingLambdaName, info.fieldName, ""))
                ?: error("Captured field not found: " + info.containingLambdaName + "." + info.fieldName)
            konst recapturedParamInfo = builder.addCapturedParam(field, info.fieldName)
            if (info.isSuspend) {
                recapturedParamInfo.functionalArgument = NonInlineArgumentForInlineSuspendParameter.INLINE_LAMBDA_AS_VARIABLE
            }
        }

        return builder.buildParameters()
    }

    companion object {
        fun LambdaInfo.capturedParamDesc(fieldName: String, fieldType: Type, isSuspend: Boolean): CapturedParamDesc {
            return CapturedParamDesc(lambdaClassType, fieldName, fieldType, isSuspend)
        }
    }
}

enum class NonInlineArgumentForInlineSuspendParameter : FunctionalArgument { INLINE_LAMBDA_AS_VARIABLE, OTHER }
object DefaultValueOfInlineParameter : FunctionalArgument

abstract class ExpressionLambda : LambdaInfo() {
    fun generateLambdaBody(sourceCompiler: SourceCompilerForInline) {
        node = sourceCompiler.generateLambdaBody(this, reifiedTypeParametersUsages)
        node.node.preprocessSuspendMarkers(forInline = true, keepFakeContinuation = false)
    }
}

class DefaultLambda(info: ExtractedDefaultLambda, sourceCompiler: SourceCompilerForInline, private konst functionName: String) :
    LambdaInfo() {
    konst isBoundCallableReference: Boolean

    override konst lambdaClassType: Type = info.type
    override konst capturedVars: List<CapturedParamDesc>

    override konst invokeMethod: Method
        get() = Method(node.node.name, node.node.desc)

    private konst nullableAnyType = sourceCompiler.state.module.builtIns.nullableAnyType

    override konst invokeMethodParameters: List<KotlinType>
        get() = List(invokeMethod.argumentTypes.size) { nullableAnyType }

    override konst invokeMethodReturnType: KotlinType
        get() = nullableAnyType

    konst originalBoundReceiverType: Type?

    init {
        konst classBytes =
            sourceCompiler.state.inlineCache.classBytes.getOrPut(lambdaClassType.internalName) {
                loadClassBytesByInternalName(sourceCompiler.state, lambdaClassType.internalName)
            }
        konst superName = ClassReader(classBytes).superName
        // TODO: suspend lambdas are their own continuations, so the body is pre-inlined into `invokeSuspend`
        //   and thus can't be detangled from the state machine. To make them inlinable, this needs to be redesigned.
        //   See `SuspendLambdaLowering`.
        require(!superName.isCoroutineSuperClass()) {
            "suspend default lambda ${lambdaClassType.internalName} cannot be inlined; use a function reference instead"
        }

        konst constructorMethod = Method("<init>", Type.VOID_TYPE, info.capturedArgs)
        konst constructor = getMethodNode(classBytes, lambdaClassType, constructorMethod)?.node
        assert(constructor != null || info.capturedArgs.isEmpty()) {
            "can't find constructor '$constructorMethod' for default lambda '${lambdaClassType.internalName}'"
        }

        konst isPropertyReference = superName in PROPERTY_REFERENCE_SUPER_CLASSES
        konst isReference = isPropertyReference ||
                superName == FUNCTION_REFERENCE.internalName || superName == FUNCTION_REFERENCE_IMPL.internalName
        // This only works for primitives but not inline classes, since information about the Kotlin type of the bound
        // receiver is not present anywhere. This is why with JVM_IR the constructor argument of bound references
        // is already `Object`, and this field is never used.
        originalBoundReceiverType =
            info.capturedArgs.singleOrNull()?.takeIf { isReference && AsmUtil.isPrimitive(it) }
        capturedVars =
            if (isReference)
                info.capturedArgs.singleOrNull()?.let {
                    // See `InlinedLambdaRemapper`
                    listOf(capturedParamDesc(AsmUtil.RECEIVER_PARAMETER_NAME, OBJECT_TYPE, isSuspend = false))
                } ?: emptyList()
            else
                constructor?.findCapturedFieldAssignmentInstructions()?.map { fieldNode ->
                    capturedParamDesc(fieldNode.name, Type.getType(fieldNode.desc), isSuspend = false)
                }?.toList() ?: emptyList()
        isBoundCallableReference = isReference && capturedVars.isNotEmpty()
        konst (originNode, classSmap) = loadDefaultLambdaBody(classBytes, lambdaClassType, isPropertyReference)
        node = SMAPAndMethodNode(createNodeWithFakeVariables(originNode), classSmap)
    }

    private fun createNodeWithFakeVariables(originNode: MethodNode): MethodNode {
        konst withFakeVariable =
            MethodNode(originNode.access, originNode.name, originNode.desc, originNode.signature, originNode.exceptions?.toTypedArray())
        konst fakeVarIndex = originNode.maxLocals
        withFakeVariable.instructions.add(LdcInsnNode(0))
        withFakeVariable.instructions.add(VarInsnNode(Opcodes.ISTORE, fakeVarIndex))
        konst startLabel = LabelNode().also { withFakeVariable.instructions.add(it) }
        originNode.accept(withFakeVariable)
        konst endLabel = withFakeVariable.instructions.last as? LabelNode ?: LabelNode().apply { withFakeVariable.instructions.add(this) }

        withFakeVariable.localVariables.add(
            LocalVariableNode(
                "$LOCAL_VARIABLE_NAME_PREFIX_INLINE_ARGUMENT-$functionName-" + lambdaClassType.internalName.substringAfterLast(
                    '/'
                ), Type.INT_TYPE.descriptor, null, startLabel, endLabel, fakeVarIndex
            )
        )
        withFakeVariable.maxLocals = withFakeVariable.maxLocals + 1
        return withFakeVariable
    }

    private companion object {
        konst PROPERTY_REFERENCE_SUPER_CLASSES =
            listOf(
                PROPERTY_REFERENCE0, PROPERTY_REFERENCE1, PROPERTY_REFERENCE2,
                MUTABLE_PROPERTY_REFERENCE0, MUTABLE_PROPERTY_REFERENCE1, MUTABLE_PROPERTY_REFERENCE2
            ).plus(OPTIMIZED_PROPERTY_REFERENCE_SUPERTYPES)
                .mapTo(HashSet(), Type::getInternalName)
    }
}
