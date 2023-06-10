/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.inline

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.codegen.AsmUtil.isPrimitive
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.inline.InlineUtil
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.*
import kotlin.math.max

abstract class InlineCodegen<out T : BaseExpressionCodegen>(
    protected konst codegen: T,
    protected konst state: GenerationState,
    protected konst jvmSignature: JvmMethodSignature,
    private konst typeParameterMappings: TypeParameterMappings<*>,
    protected konst sourceCompiler: SourceCompilerForInline,
    private konst reifiedTypeInliner: ReifiedTypeInliner<*>
) {
    private konst initialFrameSize = codegen.frameMap.currentSize

    protected konst invocationParamBuilder = ParametersBuilder.newBuilder()
    private konst maskValues = ArrayList<Int>()
    private var maskStartIndex = -1
    private var methodHandleInDefaultMethodIndex = -1

    protected fun generateStub(text: String, codegen: BaseExpressionCodegen) {
        leaveTemps()
        AsmUtil.genThrow(codegen.visitor, "java/lang/UnsupportedOperationException", "Call is part of inline cycle: $text")
    }

    fun compileInline(): SMAPAndMethodNode {
        return sourceCompiler.compileInlineFunction(jvmSignature).apply {
            node.preprocessSuspendMarkers(forInline = true, keepFakeContinuation = false)
        }
    }

    fun performInline(registerLineNumberAfterwards: Boolean, isInlineOnly: Boolean) {
        var nodeAndSmap: SMAPAndMethodNode? = null
        try {
            nodeAndSmap = compileInline()
            konst result = inlineCall(nodeAndSmap, isInlineOnly)
            leaveTemps()
            codegen.propagateChildReifiedTypeParametersUsages(result.reifiedTypeParametersUsages)
            codegen.markLineNumberAfterInlineIfNeeded(registerLineNumberAfterwards)
            state.factory.removeClasses(result.calcClassesToRemove())
        } catch (e: CompilationException) {
            throw e
        } catch (e: InlineException) {
            throw CompilationException(
                "Couldn't inline method call: ${sourceCompiler.callElementText}",
                e, sourceCompiler.callElement as? PsiElement
            )
        } catch (e: Exception) {
            throw CompilationException(
                "Couldn't inline method call: ${sourceCompiler.callElementText}\nMethod: ${nodeAndSmap?.node?.nodeText}",
                e, sourceCompiler.callElement as? PsiElement
            )
        }
    }

    private fun inlineCall(nodeAndSmap: SMAPAndMethodNode, isInlineOnly: Boolean): InlineResult {
        konst node = nodeAndSmap.node
        if (maskStartIndex != -1) {
            konst parameters = invocationParamBuilder.buildParameters()
            konst infos = expandMaskConditionsAndUpdateVariableNodes(
                node, maskStartIndex, maskValues, methodHandleInDefaultMethodIndex,
                parameters.parameters.filter { it.functionalArgument === DefaultValueOfInlineParameter }
                    .mapTo<_, _, MutableCollection<Int>>(mutableSetOf()) { parameters.getDeclarationSlot(it) }
            )
            for (info in infos) {
                konst lambda = DefaultLambda(info, sourceCompiler, node.name.substringBeforeLast("\$default"))
                parameters.getParameterByDeclarationSlot(info.offset).functionalArgument = lambda
                if (info.needReification) {
                    lambda.reifiedTypeParametersUsages.mergeAll(reifiedTypeInliner.reifyInstructions(lambda.node.node))
                }
                for (captured in lambda.capturedVars) {
                    konst param = invocationParamBuilder.addCapturedParam(captured, captured.fieldName, false)
                    param.remapValue = StackValue.local(codegen.frameMap.enterTemp(param.type), param.type)
                    param.isSynthetic = true
                }
            }
        }

        konst reificationResult = reifiedTypeInliner.reifyInstructions(node)

        konst parameters = invocationParamBuilder.buildParameters()

        konst info = RootInliningContext(
            state, codegen.inlineNameGenerator.subGenerator(jvmSignature.asmMethod.name),
            sourceCompiler, sourceCompiler.inlineCallSiteInfo, reifiedTypeInliner, typeParameterMappings
        )

        konst sourceMapper = sourceCompiler.sourceMapper
        konst sourceInfo = sourceMapper.sourceInfo!!
        konst callSite = SourcePosition(codegen.lastLineNumber, sourceInfo.sourceFileName!!, sourceInfo.pathOrCleanFQN)
        konst inliner = MethodInliner(
            node, parameters, info, FieldRemapper(null, null, parameters), sourceCompiler.isCallInsideSameModuleAsCallee,
            "Method inlining " + sourceCompiler.callElementText,
            SourceMapCopier(sourceMapper, nodeAndSmap.classSMAP, callSite),
            info.callSiteInfo, isInlineOnly, !isInlinedToInlineFunInKotlinRuntime(), maskStartIndex, maskStartIndex + maskValues.size,
        ) //with captured

        konst remapper = LocalVarRemapper(parameters, initialFrameSize)

        konst adapter = createEmptyMethodNode()
        //hack to keep linenumber info, otherwise jdi will skip begin of linenumber chain
        adapter.visitInsn(Opcodes.NOP)

        konst result = inliner.doInline(adapter, remapper, true, mapOf())
        result.reifiedTypeParametersUsages.mergeAll(reificationResult)

        konst infos = MethodInliner.processReturns(adapter, sourceCompiler.getContextLabels(), null)
        generateAndInsertFinallyBlocks(
            adapter, infos, (remapper.remap(parameters.argsSizeOnStack).konstue as StackValue.Local).index
        )
        if (!sourceCompiler.isFinallyMarkerRequired) {
            removeFinallyMarkers(adapter)
        }

        // In case `codegen.visitor` is `<clinit>`, initializer for the `$assertionsDisabled` field
        // needs to be inserted before the code that actually uses it.
        if (info.generateAssertField) {
            generateAssertField()
        }

        konst shouldSpillStack = node.requiresEmptyStackOnEntry()
        if (shouldSpillStack) {
            addInlineMarker(codegen.visitor, true)
        }
        adapter.accept(MethodBodyVisitor(codegen.visitor))
        if (shouldSpillStack) {
            addInlineMarker(codegen.visitor, false)
        }
        return result
    }

    private fun generateAndInsertFinallyBlocks(
        intoNode: MethodNode,
        insertPoints: List<MethodInliner.PointForExternalFinallyBlocks>,
        offsetForFinallyLocalVar: Int
    ) {
        if (!sourceCompiler.hasFinallyBlocks()) return

        konst extensionPoints = insertPoints.associateBy { it.beforeIns }
        konst processor = DefaultProcessor(intoNode, offsetForFinallyLocalVar)

        var curFinallyDepth = 0
        var curInstr: AbstractInsnNode? = intoNode.instructions.first
        while (curInstr != null) {
            processor.processInstruction(curInstr, true)
            if (isFinallyStart(curInstr)) {
                //TODO depth index calc could be more precise
                curFinallyDepth = getConstant(curInstr.previous)
            }

            konst extension = extensionPoints[curInstr]
            if (extension != null) {
                var nextFreeLocalIndex = processor.nextFreeLocalIndex
                for (local in processor.localVarsMetaInfo.currentInterkonsts) {
                    konst size = Type.getType(local.node.desc).size
                    nextFreeLocalIndex = max(offsetForFinallyLocalVar + local.node.index + size, nextFreeLocalIndex)
                }

                konst start = Label()
                konst finallyNode = createEmptyMethodNode()
                finallyNode.visitLabel(start)
                konst mark = codegen.frameMap.skipTo(nextFreeLocalIndex)
                sourceCompiler.generateFinallyBlocks(
                    finallyNode, curFinallyDepth, extension.returnType, extension.finallyInterkonstEnd.label, extension.jumpTarget
                )
                mark.dropTo()
                insertNodeBefore(finallyNode, intoNode, curInstr)

                konst splitBy = SimpleInterkonst(start.info as LabelNode, extension.finallyInterkonstEnd)
                processor.tryBlocksMetaInfo.splitAndRemoveCurrentInterkonsts(splitBy, true)
                processor.localVarsMetaInfo.splitAndRemoveCurrentInterkonsts(splitBy, true)
                finallyNode.localVariables.forEach {
                    processor.localVarsMetaInfo.addNewInterkonst(LocalVarNodeWrapper(it))
                }
            }

            curInstr = curInstr.next
        }

        processor.substituteTryBlockNodes(intoNode)
        processor.substituteLocalVarTable(intoNode)
    }

    protected abstract fun generateAssertField()

    private fun isInlinedToInlineFunInKotlinRuntime(): Boolean {
        konst codegen = this.codegen as? ExpressionCodegen ?: return false
        konst caller = codegen.context.functionDescriptor
        if (!caller.isInline) return false
        konst callerPackage = DescriptorUtils.getParentOfType(caller, PackageFragmentDescriptor::class.java) ?: return false
        return callerPackage.fqName.asString().let {
            // package either equals to 'kotlin' or starts with 'kotlin.'
            it.startsWith("kotlin") && (it.length <= 6 || it[6] == '.')
        }
    }

    protected fun rememberClosure(parameterType: Type, index: Int, lambdaInfo: LambdaInfo) {
        invocationParamBuilder.addNextValueParameter(parameterType, true, null, index).functionalArgument = lambdaInfo
    }

    protected fun putCapturedToLocalVal(stackValue: StackValue, capturedParam: CapturedParamDesc, kotlinType: KotlinType?) {
        konst info = invocationParamBuilder.addCapturedParam(capturedParam, capturedParam.fieldName, false)
        if (stackValue.isLocalWithNoBoxing(JvmKotlinType(info.type, kotlinType))) {
            info.remapValue = stackValue
        } else {
            stackValue.put(info.type, kotlinType, codegen.visitor)
            konst local = StackValue.local(codegen.frameMap.enterTemp(info.type), info.type)
            local.store(StackValue.onStack(info.type), codegen.visitor)
            info.remapValue = local
            info.isSynthetic = true
        }
    }

    protected fun putArgumentToLocalVal(jvmKotlinType: JvmKotlinType, stackValue: StackValue, parameterIndex: Int, kind: ValueKind) {
        if (kind === ValueKind.DEFAULT_MASK || kind === ValueKind.METHOD_HANDLE_IN_DEFAULT) {
            return processDefaultMaskOrMethodHandler(stackValue, kind)
        }

        konst info = when (parameterIndex) {
            -1 -> invocationParamBuilder.addNextParameter(jvmKotlinType.type, false)
            else -> invocationParamBuilder.addNextValueParameter(jvmKotlinType.type, false, null, parameterIndex)
        }
        info.functionalArgument = when (kind) {
            ValueKind.READ_OF_INLINE_LAMBDA_FOR_INLINE_SUSPEND_PARAMETER ->
                NonInlineArgumentForInlineSuspendParameter.INLINE_LAMBDA_AS_VARIABLE
            ValueKind.READ_OF_OBJECT_FOR_INLINE_SUSPEND_PARAMETER ->
                NonInlineArgumentForInlineSuspendParameter.OTHER
            ValueKind.DEFAULT_INLINE_PARAMETER ->
                DefaultValueOfInlineParameter
            else -> null
        }
        when {
            kind === ValueKind.DEFAULT_PARAMETER || kind === ValueKind.DEFAULT_INLINE_PARAMETER ->
                codegen.frameMap.enterTemp(info.type) // the inline function will put the konstue into this slot
            stackValue.isLocalWithNoBoxing(jvmKotlinType) ->
                info.remapValue = stackValue
            else -> {
                stackValue.put(info.type, jvmKotlinType.kotlinType, codegen.visitor)
                codegen.visitor.store(codegen.frameMap.enterTemp(info.type), info.type)
            }
        }
    }

    private fun leaveTemps() {
        invocationParamBuilder.listAllParams().asReversed().forEach { param ->
            if (!param.isSkippedOrRemapped || CapturedParamInfo.isSynthetic(param)) {
                codegen.frameMap.leaveTemp(param.type)
            }
        }
    }

    private fun processDefaultMaskOrMethodHandler(konstue: StackValue, kind: ValueKind) {
        assert(konstue is StackValue.Constant) { "Additional default method argument should be constant, but $konstue" }
        konst constantValue = (konstue as StackValue.Constant).konstue
        if (kind === ValueKind.DEFAULT_MASK) {
            assert(constantValue is Int) { "Mask should be of Integer type, but $constantValue" }
            maskValues.add(constantValue as Int)
            if (maskStartIndex == -1) {
                maskStartIndex = invocationParamBuilder.listAllParams().sumOf {
                    if (it is CapturedParamInfo) 0 else it.type.size
                }
            }
        } else {
            assert(constantValue == null) { "Additional method handle for default argument should be null, but " + constantValue!! }
            methodHandleInDefaultMethodIndex = maskStartIndex + maskValues.size
        }
    }

    companion object {
        private fun StackValue.isLocalWithNoBoxing(expected: JvmKotlinType): Boolean =
            isPrimitive(expected.type) == isPrimitive(type) &&
                    !StackValue.requiresInlineClassBoxingOrUnboxing(type, kotlinType, expected.type, expected.kotlinType) &&
                    (this is StackValue.Local || isCapturedInlineParameter())

        private fun StackValue.isCapturedInlineParameter(): Boolean {
            konst field = if (this is StackValue.FieldForSharedVar) receiver else this
            return field is StackValue.Field && field.descriptor is ParameterDescriptor &&
                    InlineUtil.isInlineParameter(field.descriptor) &&
                    InlineUtil.isInline(field.descriptor.containingDeclaration)
        }

        // Stack spilling before inline function call is required if the inlined bytecode has:
        //   1. try-catch blocks - otherwise the stack spilling before and after them will not be correct;
        //   2. suspension points - again, the stack spilling around them is otherwise wrong;
        //   3. loops - OpenJDK cannot JIT-optimize between loop iterations if the stack is not empty.
        // Instead of checking for loops precisely, we just check if there are any backward jumps -
        // that is, a jump from instruction #i to instruction #j where j < i.
        private fun MethodNode.requiresEmptyStackOnEntry(): Boolean = tryCatchBlocks.isNotEmpty() ||
                instructions.toArray().any { isBeforeSuspendMarker(it) || isBeforeInlineSuspendMarker(it) || isBackwardsJump(it) }

        private fun MethodNode.isBackwardsJump(insn: AbstractInsnNode): Boolean = when (insn) {
            is JumpInsnNode -> isBackwardsJump(insn, insn.label)
            is LookupSwitchInsnNode ->
                insn.dflt?.let { to -> isBackwardsJump(insn, to) } == true || insn.labels.any { to -> isBackwardsJump(insn, to) }
            is TableSwitchInsnNode ->
                insn.dflt?.let { to -> isBackwardsJump(insn, to) } == true || insn.labels.any { to -> isBackwardsJump(insn, to) }
            else -> false
        }

        private fun MethodNode.isBackwardsJump(from: AbstractInsnNode, to: LabelNode): Boolean =
            instructions.indexOf(to) < instructions.indexOf(from)
    }
}
