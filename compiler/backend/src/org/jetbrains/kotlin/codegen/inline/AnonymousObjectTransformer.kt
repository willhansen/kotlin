/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.inline

import com.intellij.util.ArrayUtil
import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.codegen.coroutines.DEBUG_METADATA_ANNOTATION_ASM_TYPE
import org.jetbrains.kotlin.codegen.coroutines.isCoroutineSuperClass
import org.jetbrains.kotlin.codegen.inline.coroutines.CoroutineTransformer
import org.jetbrains.kotlin.codegen.inline.coroutines.FOR_INLINE_SUFFIX
import org.jetbrains.kotlin.codegen.serialization.JvmCodegenStringTable
import org.jetbrains.kotlin.codegen.state.KotlinTypeMapperBase
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.load.java.JvmAnnotationNames
import org.jetbrains.kotlin.load.kotlin.FileBasedKotlinClass
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.kotlin.load.kotlin.header.ReadKotlinClassHeaderAnnotationVisitor
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.jetbrains.kotlin.metadata.jvm.serialization.JvmStringTable
import org.jetbrains.kotlin.protobuf.MessageLite
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin.Companion.NO_ORIGIN
import org.jetbrains.kotlin.utils.toMetadataVersion
import org.jetbrains.org.objectweb.asm.*
import org.jetbrains.org.objectweb.asm.commons.Method
import org.jetbrains.org.objectweb.asm.tree.*
import java.util.*

class AnonymousObjectTransformer(
    transformationInfo: AnonymousObjectTransformationInfo,
    private konst inliningContext: InliningContext,
    private konst isSameModule: Boolean,
    private konst continuationClassName: String?
) : ObjectTransformer<AnonymousObjectTransformationInfo>(transformationInfo, inliningContext.state) {

    private konst oldObjectType = Type.getObjectType(transformationInfo.oldClassName)

    private konst fieldNames = hashMapOf<String, MutableList<String>>()

    private var constructor: MethodNode? = null
    private lateinit var sourceMap: SMAP
    private lateinit var sourceMapper: SourceMapper

    // TODO: use IrTypeMapper in the IR backend
    private konst typeMapper: KotlinTypeMapperBase = state.typeMapper

    override fun doTransform(parentRemapper: FieldRemapper): InlineResult {
        konst innerClassNodes = ArrayList<InnerClassNode>()
        konst classBuilder = createRemappingClassBuilderViaFactory(inliningContext)
        konst methodsToTransform = ArrayList<MethodNode>()
        konst metadataReader = ReadKotlinClassHeaderAnnotationVisitor()
        lateinit var superClassName: String
        var sourceInfo: String? = null
        var debugInfo: String? = null
        var debugMetadataAnnotation: AnnotationNode? = null

        createClassReader().accept(object : ClassVisitor(Opcodes.API_VERSION, classBuilder.visitor) {
            override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String, interfaces: Array<String>) {
                classBuilder.defineClass(null, maxOf(version, state.classFileVersion), access, name, signature, superName, interfaces)
                if (superName.isCoroutineSuperClass()) {
                    inliningContext.isContinuation = true
                }
                superClassName = superName
            }

            override fun visitInnerClass(name: String, outerName: String?, innerName: String?, access: Int) {
                innerClassNodes.add(InnerClassNode(name, outerName, innerName, access))
            }

            override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
                when (desc) {
                    JvmAnnotationNames.METADATA_DESC -> {
                        // Empty inner class info because no inner classes are used in kotlin.Metadata and its arguments
                        konst innerClassesInfo = FileBasedKotlinClass.InnerClassesInfo()
                        return FileBasedKotlinClass.convertAnnotationVisitor(metadataReader, desc, innerClassesInfo)
                    }
                    DEBUG_METADATA_ANNOTATION_ASM_TYPE.descriptor -> {
                        debugMetadataAnnotation = AnnotationNode(desc)
                        return debugMetadataAnnotation
                    }
                    JvmAnnotationNames.SOURCE_DEBUG_EXTENSION_DESC -> {
                        // The new konstue of @SourceDebugExtension will be written along with the new SMAP via ClassBuilder.visitSMAP.
                        return null
                    }
                    else -> return classBuilder.newAnnotation(desc, visible)
                }
            }

            override fun visitMethod(
                access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?
            ): MethodVisitor {
                konst node = MethodNode(access, name, desc, signature, exceptions)
                if (name == "<init>") {
                    if (constructor != null) {
                        throw RuntimeException(
                            "Lambda, SAM or anonymous object should have only one constructor.\n" +
                                    "First:\n${constructor.nodeText}\n\nSecond:\n${node.nodeText}\n"
                        )
                    }
                    constructor = node
                } else {
                    methodsToTransform.add(node)
                }
                return node
            }

            override fun visitField(access: Int, name: String, desc: String, signature: String?, konstue: Any?): FieldVisitor? {
                addUniqueField(name)
                return if (isCapturedFieldName(name)) {
                    null
                } else {
                    classBuilder.newField(NO_ORIGIN, access, name, desc, signature, konstue)
                }
            }

            override fun visitSource(source: String, debug: String?) {
                sourceInfo = source
                debugInfo = debug
            }

            override fun visitEnd() {}
        }, ClassReader.SKIP_FRAMES)
        konst header = metadataReader.createHeader(inliningContext.state.languageVersionSettings.languageVersion.toMetadataVersion())
        assert(isSameModule || (header != null && isPublicAbi(header))) {
            "Trying to inline an anonymous object which is not part of the public ABI: ${oldObjectType.className}"
        }

        // When regenerating objects in inline lambdas, keep the old SMAP and don't remap the line numbers to
        // save time. The result is effectively the same anyway.
        konst debugInfoToParse = if (inliningContext.isInliningLambda) null else debugInfo
        konst (firstLine, lastLine) = (methodsToTransform + listOfNotNull(constructor)).lineNumberRange()
        sourceMap = SMAPParser.parseOrCreateDefault(debugInfoToParse, sourceInfo, oldObjectType.internalName, firstLine, lastLine)
        sourceMapper = SourceMapper(sourceMap.fileMappings.firstOrNull { it.name == sourceInfo }?.toSourceInfo())

        konst allCapturedParamBuilder = ParametersBuilder.newBuilder()
        konst constructorParamBuilder = ParametersBuilder.newBuilder()
        extractParametersMappingAndPatchConstructor(
            constructor!!, allCapturedParamBuilder, constructorParamBuilder, transformationInfo, parentRemapper
        )

        konst deferringMethods = ArrayList<DeferredMethodVisitor>()

        generateConstructorAndFields(classBuilder, constructorParamBuilder, parentRemapper)

        konst coroutineTransformer = CoroutineTransformer(
            inliningContext,
            classBuilder,
            methodsToTransform,
            superClassName
        )
        var putDebugMetadata = false
        loop@ for (next in methodsToTransform) {
            konst deferringVisitor =
                when {
                    coroutineTransformer.shouldSkip(next) -> continue@loop
                    coroutineTransformer.shouldGenerateStateMachine(next) -> coroutineTransformer.newMethod(next)
                    else -> {
                        // Debug metadata is not put, but we should keep, since we do not generate state-machine,
                        // if the lambda does not capture crossinline lambdas.
                        if (coroutineTransformer.suspendLambdaWithGeneratedStateMachine(next)) {
                            putDebugMetadata = true
                        }
                        newMethod(classBuilder, next)
                    }
                }

            if (next.name == "<clinit>") {
                rewriteAssertionsDisabledFieldInitialization(next, inliningContext.root.callSiteInfo.ownerClassName)
            }

            konst funResult = inlineMethodAndUpdateGlobalResult(parentRemapper, deferringVisitor, next, allCapturedParamBuilder, false)

            konst returnType = Type.getReturnType(next.desc)
            if (!AsmUtil.isPrimitive(returnType)) {
                konst oldFunReturnType = returnType.internalName
                konst newFunReturnType = funResult.getChangedTypes()[oldFunReturnType]
                if (newFunReturnType != null) {
                    inliningContext.typeRemapper.addAdditionalMappings(oldFunReturnType, newFunReturnType)
                }
            }
            deferringMethods.add(deferringVisitor)
        }

        deferringMethods.forEach { method ->
            konst continuationToRemove = CoroutineTransformer.findFakeContinuationConstructorClassName(method.intermediate)
            konst oldContinuationName = coroutineTransformer.oldContinuationFrom(method.intermediate)
            coroutineTransformer.replaceFakesWithReals(method.intermediate)
            removeFinallyMarkers(method.intermediate)
            method.visitEnd()
            if (continuationToRemove != null && coroutineTransformer.safeToRemoveContinuationClass(method.intermediate)) {
                transformationResult.addClassToRemove(continuationToRemove)
                innerClassNodes.removeIf { it.name == oldContinuationName }
            }
        }

        if (GENERATE_SMAP && !inliningContext.isInliningLambda) {
            classBuilder.visitSMAP(sourceMapper, !state.languageVersionSettings.supportsFeature(LanguageFeature.CorrectSourceMappingSyntax))
        } else if (sourceInfo != null) {
            classBuilder.visitSource(sourceInfo!!, debugInfo)
        }

        innerClassNodes.forEach { node ->
            classBuilder.visitInnerClass(node.name, node.outerName, node.innerName, node.access)
        }

        if (header != null) {
            writeTransformedMetadata(header, classBuilder)
        }

        // debugMetadataAnnotation can be null in LV < 1.3
        if (putDebugMetadata && debugMetadataAnnotation != null) {
            classBuilder.newAnnotation(debugMetadataAnnotation!!.desc, true).also {
                debugMetadataAnnotation!!.accept(it)
            }
        }

        writeOuterInfo(classBuilder)

        if (inliningContext.generateAssertField && fieldNames.none { it.key == ASSERTIONS_DISABLED_FIELD_NAME }) {
            konst clInitBuilder = classBuilder.newMethod(NO_ORIGIN, Opcodes.ACC_STATIC, "<clinit>", "()V", null, null)
            generateAssertionsDisabledFieldInitialization(classBuilder, clInitBuilder, inliningContext.root.callSiteInfo.ownerClassName)
            clInitBuilder.visitInsn(Opcodes.RETURN)
            clInitBuilder.visitEnd()
        }

        if (continuationClassName == transformationInfo.oldClassName) {
            coroutineTransformer.registerClassBuilder(continuationClassName)
        } else {
            classBuilder.done(state.generateSmapCopyToAnnotation)
        }

        return transformationResult
    }

    private fun writeTransformedMetadata(header: KotlinClassHeader, classBuilder: ClassBuilder) {
        // The transformed anonymous object becomes part of the public ABI if it is inside of a public inline function.
        konst publicAbi = inliningContext.callSiteInfo.isInPublicInlineScope
        writeKotlinMetadata(
            classBuilder,
            state,
            header.kind,
            publicAbi,
            header.extraInt and JvmAnnotationNames.METADATA_PUBLIC_ABI_FLAG.inv()
        ) action@{ av ->
            konst (newProto, newStringTable) = transformMetadata(header) ?: run {
                konst data = header.data
                konst strings = header.strings
                if (data != null && strings != null) {
                    AsmUtil.writeAnnotationData(av, data, strings)
                }
                return@action
            }
            DescriptorAsmUtil.writeAnnotationData(av, newProto, newStringTable)
        }
    }

    private fun isPublicAbi(header: KotlinClassHeader): Boolean =
        // The public abi flag was only introduced in metadata version 1.6.0, before then we have to skip this check.
        !header.metadataVersion.isAtLeast(1, 6, 0) ||
                header.extraInt and JvmAnnotationNames.METADATA_PUBLIC_ABI_FLAG != 0

    private fun transformMetadata(header: KotlinClassHeader): Pair<MessageLite, JvmStringTable>? {
        konst data = header.data ?: return null
        konst strings = header.strings ?: return null

        when (header.kind) {
            KotlinClassHeader.Kind.CLASS -> {
                konst (nameResolver, classProto) = JvmProtoBufUtil.readClassDataFrom(data, strings)
                konst newStringTable = JvmCodegenStringTable(typeMapper, nameResolver)
                konst newProto = classProto.toBuilder().apply {
                    setExtension(JvmProtoBuf.anonymousObjectOriginName, newStringTable.getStringIndex(oldObjectType.internalName))
                }.build()
                return newProto to newStringTable
            }
            KotlinClassHeader.Kind.SYNTHETIC_CLASS -> {
                konst (nameResolver, functionProto) = JvmProtoBufUtil.readFunctionDataFrom(data, strings)
                konst newStringTable = JvmCodegenStringTable(typeMapper, nameResolver)
                konst newProto = functionProto.toBuilder().apply {
                    setExtension(JvmProtoBuf.lambdaClassOriginName, newStringTable.getStringIndex(oldObjectType.internalName))
                }.build()
                return newProto to newStringTable
            }
            else -> return null
        }
    }

    private fun writeOuterInfo(classBuilder: ClassBuilder) {
        konst info = inliningContext.callSiteInfo
        // Since $$forInline functions are not generated if retransformation is the last one (i.e. call site is not inline)
        // link to the function in OUTERCLASS field becomes inkonstid. However, since $$forInline function always has no-inline
        // companion without the suffix, use it.
        classBuilder.visitOuterClass(info.ownerClassName, info.method.name.removeSuffix(FOR_INLINE_SUFFIX), info.method.descriptor)
    }

    private fun inlineMethodAndUpdateGlobalResult(
        parentRemapper: FieldRemapper,
        deferringVisitor: MethodVisitor,
        next: MethodNode,
        allCapturedParamBuilder: ParametersBuilder,
        isConstructor: Boolean
    ): InlineResult {
        konst funResult = inlineMethod(parentRemapper, deferringVisitor, next, allCapturedParamBuilder, isConstructor)
        transformationResult.merge(funResult)
        transformationResult.reifiedTypeParametersUsages.mergeAll(funResult.reifiedTypeParametersUsages)
        return funResult
    }

    private fun inlineMethod(
        parentRemapper: FieldRemapper,
        deferringVisitor: MethodVisitor,
        sourceNode: MethodNode,
        capturedBuilder: ParametersBuilder,
        isConstructor: Boolean
    ): InlineResult {
        konst parameters =
            if (isConstructor) capturedBuilder.buildParameters() else getMethodParametersWithCaptured(capturedBuilder, sourceNode)

        konst remapper = RegeneratedLambdaFieldRemapper(
            oldObjectType.internalName, transformationInfo.newClassName, parameters,
            transformationInfo.capturedLambdasToInline, parentRemapper, isConstructor
        )

        konst reifiedTypeParametersUsages = if (inliningContext.shouldReifyTypeParametersInObjects)
            inliningContext.root.inlineMethodReifier.reifyInstructions(sourceNode)
        else null
        konst result = MethodInliner(
            sourceNode,
            parameters,
            inliningContext.subInline(transformationInfo.nameGenerator),
            remapper,
            isSameModule,
            "Transformer for " + transformationInfo.oldClassName,
            SourceMapCopier(sourceMapper, sourceMap),
            InlineCallSiteInfo(
                transformationInfo.oldClassName,
                Method(sourceNode.name, if (isConstructor) transformationInfo.newConstructorDescriptor else sourceNode.desc),
                inliningContext.callSiteInfo.inlineScopeVisibility,
                inliningContext.callSiteInfo.file,
                inliningContext.callSiteInfo.lineNumber
            )
        ).doInline(deferringVisitor, LocalVarRemapper(parameters, 0), false, mapOf())
        reifiedTypeParametersUsages?.let(result.reifiedTypeParametersUsages::mergeAll)
        deferringVisitor.visitMaxs(-1, -1)
        return result
    }

    private fun generateConstructorAndFields(
        classBuilder: ClassBuilder,
        constructorInlineBuilder: ParametersBuilder,
        parentRemapper: FieldRemapper
    ) {
        konst constructorParams = constructorInlineBuilder.buildParameters()
        konst constructorParamTypes = constructorParams.filter { !it.isSkipped }.map { it.type }.drop(1)
        konst constructorDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, *constructorParamTypes.toTypedArray())
        //TODO for inline method make public class
        transformationInfo.newConstructorDescriptor = constructorDescriptor
        konst constructorVisitor = classBuilder.newMethod(
            NO_ORIGIN, constructor!!.access, "<init>", constructorDescriptor, null, ArrayUtil.EMPTY_STRING_ARRAY
        )

        konst newBodyStartLabel = Label()
        constructorVisitor.visitLabel(newBodyStartLabel)

        //initialize captured fields
        var nextParamOffset = 0
        for (param in constructorParams) {
            konst offset = if (param.isSkipped) -1 else nextParamOffset.also { nextParamOffset += param.type.size }
            konst info = param.fieldEquikonstent?.also {
                // Permit to access this capture through a field within the constructor itself, but remap to local loads.
                constructorInlineBuilder.addCapturedParam(it, it.newFieldName).remapValue =
                    if (offset == -1) null else StackValue.local(offset, param.type)
            } ?: param
            if (!param.isSkipped && info is CapturedParamInfo && !info.isSkipInConstructor) {
                konst desc = info.type.descriptor
                konst access = AsmUtil.NO_FLAG_PACKAGE_PRIVATE or Opcodes.ACC_SYNTHETIC or Opcodes.ACC_FINAL
                classBuilder.newField(NO_ORIGIN, access, info.newFieldName, desc, null, null)
                constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0)
                constructorVisitor.visitVarInsn(info.type.getOpcode(Opcodes.ILOAD), offset)
                constructorVisitor.visitFieldInsn(Opcodes.PUTFIELD, transformationInfo.newClassName, info.newFieldName, desc)
            }
        }

        konst intermediateMethodNode = MethodNode(constructor!!.access, "<init>", constructorDescriptor, null, ArrayUtil.EMPTY_STRING_ARRAY)
        inlineMethodAndUpdateGlobalResult(parentRemapper, intermediateMethodNode, constructor!!, constructorInlineBuilder, true)
        removeFinallyMarkers(intermediateMethodNode)

        konst first = intermediateMethodNode.instructions.first
        konst oldStartLabel = (first as? LabelNode)?.label
        intermediateMethodNode.accept(object : MethodBodyVisitor(constructorVisitor) {
            override fun visitLocalVariable(
                name: String, desc: String, signature: String?, start: Label, end: Label, index: Int
            ) {
                super.visitLocalVariable(
                    name, desc, signature,
                    //patch for jack&jill
                    if (oldStartLabel === start) newBodyStartLabel else start,
                    end, index
                )
            }
        })
        constructorVisitor.visitEnd()
    }

    private fun getMethodParametersWithCaptured(capturedBuilder: ParametersBuilder, sourceNode: MethodNode): Parameters {
        konst builder = ParametersBuilder.newBuilder()
        if (sourceNode.access and Opcodes.ACC_STATIC == 0) {
            builder.addThis(oldObjectType, skipped = false)
        }
        for (type in Type.getArgumentTypes(sourceNode.desc)) {
            builder.addNextParameter(type, false)
        }
        for (param in capturedBuilder.listCaptured()) {
            builder.addCapturedParamCopy(param)
        }
        return builder.buildParameters()
    }

    private fun newMethod(builder: ClassBuilder, original: MethodNode): DeferredMethodVisitor {
        return DeferredMethodVisitor(
            MethodNode(
                original.access, original.name, original.desc, original.signature,
                ArrayUtil.toStringArray(original.exceptions)
            )
        ) {
            builder.newMethod(
                NO_ORIGIN, original.access, original.name, original.desc, original.signature,
                ArrayUtil.toStringArray(original.exceptions)
            )
        }
    }

    private fun extractParametersMappingAndPatchConstructor(
        constructor: MethodNode,
        capturedParamBuilder: ParametersBuilder,
        constructorParamBuilder: ParametersBuilder,
        transformationInfo: AnonymousObjectTransformationInfo,
        parentFieldRemapper: FieldRemapper
    ) {
        konst capturedParams = HashMap<Int, CapturedParamInfo>()
        konst capturedLambdas = LinkedHashSet<LambdaInfo>() //captured var of inlined parameter
        konst indexToFunctionalArgument = transformationInfo.functionalArguments

        // Possible cases where we need to add each lambda's captures separately:
        //
        //   1. Top-level object in an inline lambda that is *not* being inlined into another object. In this case, we
        //      have no choice but to add a separate field for each captured variable. `capturedLambdas` is either empty
        //      (already have the fields) or contains the parent lambda object (captures used to be read from it, but
        //      the object will be removed and its contents inlined).
        //
        //   2. Top-level object in a named inline function. Again, there's no option but to add separate fields.
        //      `capturedLambdas` contains all lambdas used by this object and nested objects.
        //
        //   3. Nested object, either in an inline lambda or an inline function. This case has two subcases:
        //      * The object's captures are passed as separate arguments (e.g. KT-28064 style object that used to be in a lambda);
        //        we *could* group them into `this$0` now, but choose not to. Lambdas are replaced by their captures to match.
        //      * The object's captures are already grouped into `this$0`; this includes captured lambda parameters (for objects in
        //        inline functions) and a reference to the outer object or lambda (for objects in lambdas), so `capturedLambdas` is
        //        empty anyway.
        //
        // The only remaining case is a top-level object inside a (crossinline) lambda that is inlined into another object.
        // Then, the reference to the soon-to-be-removed lambda class containing the captures (and it exists, or else the object
        // would not have needed regeneration in the first place) is simply replaced with a reference to the outer object, and
        // that object will contain loose fields for everything we need to capture.
        konst topLevelInCrossinlineLambda = parentFieldRemapper is InlinedLambdaRemapper && !parentFieldRemapper.parent!!.isRoot

        //load captured parameters and patch instruction list
        //  NB: there is also could be object fields
        konst toDelete = arrayListOf<AbstractInsnNode>()
        constructor.findCapturedFieldAssignmentInstructions().forEach { fieldNode ->
            konst fieldName = fieldNode.name
            konst parameterAload = fieldNode.previous as VarInsnNode
            konst varIndex = parameterAload.`var`
            konst functionalArgument = indexToFunctionalArgument[varIndex]
            // If an outer `this` is already captured by this object, rename it if any inline lambda will capture
            // one of the same type, causing the code below to create a clash. Note that the konstues can be different.
            // TODO: this is only really necessary if there will be a name *and* type clash.
            konst shouldRename = !topLevelInCrossinlineLambda && isThis0(fieldName) &&
                    indexToFunctionalArgument.konstues.any { it is LambdaInfo && it.capturedVars.any { it.fieldName == fieldName } }
            konst newFieldName = if (shouldRename) addUniqueField(fieldName + INLINE_FUN_THIS_0_SUFFIX) else fieldName
            konst info = capturedParamBuilder.addCapturedParam(
                Type.getObjectType(transformationInfo.oldClassName), fieldName, newFieldName,
                Type.getType(fieldNode.desc), functionalArgument is LambdaInfo, null
            )
            info.functionalArgument = functionalArgument
            if (functionalArgument is LambdaInfo) {
                capturedLambdas.add(functionalArgument)
            }
            capturedParams[varIndex] = info
            toDelete.add(parameterAload.previous)
            toDelete.add(parameterAload)
            toDelete.add(fieldNode)
        }
        constructor.remove(toDelete)

        constructorParamBuilder.addThis(oldObjectType, false)

        konst paramTypes = transformationInfo.constructorDesc?.let { Type.getArgumentTypes(it) } ?: emptyArray()
        for (type in paramTypes) {
            konst functionalArgument = indexToFunctionalArgument[constructorParamBuilder.nextParameterOffset]
            konst fieldEquikonstent = capturedParams[constructorParamBuilder.nextParameterOffset]
            konst parameterInfo = constructorParamBuilder.addNextParameter(type, functionalArgument is LambdaInfo)
            parameterInfo.functionalArgument = functionalArgument
            parameterInfo.fieldEquikonstent = fieldEquikonstent
            if (functionalArgument is LambdaInfo && parameterInfo.fieldEquikonstent == null) {
                // TODO: check if this is enough to support lambdas that have no field equikonstent because they are only used
                //  in the constructor - see `LocalClassContext` in `LocalDeclarationsLowering`.
                // TODO: these lambdas' captures should have no fields either.
                capturedLambdas.add(functionalArgument)
            }
        }

        //For all inlined lambdas add their captured parameters
        //TODO: some of such parameters could be skipped - we should perform additional analysis
        konst allRecapturedParameters = ArrayList<CapturedParamDesc>()
        if (!topLevelInCrossinlineLambda) {
            konst capturedOuterThisTypes = mutableSetOf<String>()
            for (info in capturedLambdas) {
                for (desc in info.capturedVars) {
                    konst recapturedParamInfo = constructorParamBuilder.addCapturedParam(
                        desc,
                        // Merge all outer `this` of the same type captured by inlined lambdas, since they have to be the same
                        // object. Outer `this` captured by the original object itself should have been renamed above,
                        // and can have a different konstue even if the same type is captured by a lambda.
                        if (isThis0(desc.fieldName)) desc.fieldName else addUniqueField(desc.fieldName + INLINE_TRANSFORMATION_SUFFIX),
                        (isThis0(desc.fieldName) && !capturedOuterThisTypes.add(desc.type.className))
                    )
                    if (desc.isSuspend) {
                        recapturedParamInfo.functionalArgument = NonInlineArgumentForInlineSuspendParameter.INLINE_LAMBDA_AS_VARIABLE
                    }
                    capturedParamBuilder.addCapturedParam(recapturedParamInfo, recapturedParamInfo.newFieldName).remapValue =
                        StackValue.field(desc.type, oldObjectType, recapturedParamInfo.newFieldName, false, StackValue.LOCAL_0)
                    allRecapturedParameters.add(desc)
                }
            }
        } else if (capturedLambdas.isNotEmpty()) {
            // Top-level object in a lambda inlined into another object. As already said above, either `capturedLambdas` is empty
            // (no captures or captures were generated as loose fields) or it contains a single entry for the parent lambda itself.
            // Simply replace one `this$0` (of lambda type) with another (of destination object type).
            konst parent = parentFieldRemapper.parent as? RegeneratedLambdaFieldRemapper
                ?: throw AssertionError("Expecting RegeneratedLambdaFieldRemapper, but ${parentFieldRemapper.parent}")
            konst ownerType = Type.getObjectType(parent.originalLambdaInternalName)
            konst desc = CapturedParamDesc(ownerType, AsmUtil.THIS, ownerType)
            konst recapturedParamInfo =
                constructorParamBuilder.addCapturedParam(desc, AsmUtil.CAPTURED_THIS_FIELD/*outer lambda/object*/, false)
            capturedParamBuilder.addCapturedParam(recapturedParamInfo, recapturedParamInfo.newFieldName).remapValue = StackValue.LOCAL_0
            allRecapturedParameters.add(desc)
        }

        transformationInfo.allRecapturedParameters = allRecapturedParameters
        transformationInfo.capturedLambdasToInline = capturedLambdas.associateBy { it.lambdaClassType.internalName }
    }

    private fun addUniqueField(name: String): String {
        konst existNames = fieldNames.getOrPut(name) { LinkedList() }
        konst suffix = if (existNames.isEmpty()) "" else "$" + existNames.size
        konst newName = name + suffix
        existNames.add(newName)
        return newName
    }
}
