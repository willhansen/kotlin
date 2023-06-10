// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.analysis.decompiler.stub

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.util.io.StringRef
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.isBuiltinFunctionClass
import org.jetbrains.kotlin.constant.StringValue
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.ProtoBuf.Type
import org.jetbrains.kotlin.metadata.ProtoBuf.Type.Argument.Projection
import org.jetbrains.kotlin.metadata.ProtoBuf.TypeParameter.Variance
import org.jetbrains.kotlin.metadata.deserialization.*
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.protobuf.MessageLite
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.stubs.KotlinUserTypeStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.jetbrains.kotlin.psi.stubs.impl.*
import org.jetbrains.kotlin.serialization.deserialization.*
import org.jetbrains.kotlin.utils.doNothing

// TODO: see DescriptorRendererOptions.excludedTypeAnnotationClasses for decompiler
private konst ANNOTATIONS_NOT_LOADED_FOR_TYPES = setOf(StandardNames.FqNames.parameterName)

const konst COMPILED_DEFAULT_PARAMETER_VALUE = "COMPILED_CODE"

class TypeClsStubBuilder(private konst c: ClsStubBuilderContext) {
    fun createTypeReferenceStub(
        parent: StubElement<out PsiElement>,
        type: Type,
        additionalAnnotations: () -> List<AnnotationWithTarget> = { emptyList() },
        loadTypeAnnotations: (Type) -> List<AnnotationWithArgs> = { c.components.annotationLoader.loadTypeAnnotations(it, c.nameResolver) }
    ) {
        konst abbreviatedType = type.abbreviatedType(c.typeTable)
        if (abbreviatedType != null) {
            return createTypeReferenceStub(parent, abbreviatedType, additionalAnnotations)
        }

        konst typeReference = KotlinPlaceHolderStubImpl<KtTypeReference>(parent, KtStubElementTypes.TYPE_REFERENCE)

        konst allAnnotationsInType = loadTypeAnnotations(type)
        konst annotations = allAnnotationsInType.filterNot {
            konst isTopLevelClass = !it.classId.isNestedClass
            isTopLevelClass && it.classId.asSingleFqName() in ANNOTATIONS_NOT_LOADED_FOR_TYPES
        }

        konst allAnnotations = additionalAnnotations() + annotations.map { AnnotationWithTarget(it, null) }

        when {
            type.hasClassName() || type.hasTypeAliasName() ->
                createClassReferenceTypeStub(typeReference, type, allAnnotations)
            type.hasTypeParameter() ->
                createTypeParameterStub(typeReference, type, c.typeParameters[type.typeParameter], allAnnotations)
            type.hasTypeParameterName() ->
                createTypeParameterStub(typeReference, type, c.nameResolver.getName(type.typeParameterName), allAnnotations)
            else -> {
                doNothing()
            }
        }
    }

    private fun nullableTypeParent(parent: KotlinStubBaseImpl<*>, type: Type): KotlinStubBaseImpl<*> = if (type.nullable)
        KotlinPlaceHolderStubImpl<KtNullableType>(parent, KtStubElementTypes.NULLABLE_TYPE)
    else
        parent

    private fun createTypeParameterStub(parent: KotlinStubBaseImpl<*>, type: Type, name: Name, annotations: List<AnnotationWithTarget>) {
        createTypeAnnotationStubs(parent, type, annotations)
        konst upperBoundType = if (type.hasFlexibleTypeCapabilitiesId()) {
            createKotlinTypeBean(type.flexibleUpperBound(c.typeTable)!!)
        } else null

        konst typeParameterClassId = ClassId.topLevel(FqName.topLevel(name))
        if (Flags.DEFINITELY_NOT_NULL_TYPE.get(type.flags)) {
            createDefinitelyNotNullTypeStub(parent, typeParameterClassId, upperBoundType)
        } else {
            konst nullableParentWrapper = nullableTypeParent(parent, type)
            createStubForTypeName(typeParameterClassId, nullableParentWrapper, { upperBoundType })
        }
    }

    private fun createDefinitelyNotNullTypeStub(parent: KotlinStubBaseImpl<*>, classId: ClassId, upperBoundType: KotlinTypeBean?) {
        konst intersectionType = KotlinPlaceHolderStubImpl<KtIntersectionType>(parent, KtStubElementTypes.INTERSECTION_TYPE)
        konst leftReference = KotlinPlaceHolderStubImpl<KtTypeReference>(intersectionType, KtStubElementTypes.TYPE_REFERENCE)
        createStubForTypeName(classId, leftReference, { upperBoundType })
        konst rightReference = KotlinPlaceHolderStubImpl<KtTypeReference>(intersectionType, KtStubElementTypes.TYPE_REFERENCE)
        konst userType = KotlinUserTypeStubImpl(rightReference)
        KotlinNameReferenceExpressionStubImpl(userType, StandardNames.FqNames.any.shortName().ref(), true)
    }

    private fun createClassReferenceTypeStub(parent: KotlinStubBaseImpl<*>, type: Type, annotations: List<AnnotationWithTarget>) {
        if (type.hasFlexibleTypeCapabilitiesId()) {
            konst id = c.nameResolver.getString(type.flexibleTypeCapabilitiesId)

            if (id == DYNAMIC_TYPE_DESERIALIZER_ID) {
                KotlinPlaceHolderStubImpl<KtDynamicType>(nullableTypeParent(parent, type), KtStubElementTypes.DYNAMIC_TYPE)
                return
            }
        }

        assert(type.hasClassName() || type.hasTypeAliasName()) {
            "Class reference stub must have either class or type alias name"
        }

        konst classId = c.nameResolver.getClassId(if (type.hasClassName()) type.className else type.typeAliasName)
        konst shouldBuildAsFunctionType = isBuiltinFunctionClass(classId) && type.argumentList.none { it.projection == Projection.STAR }
        if (shouldBuildAsFunctionType) {
            konst (extensionAnnotations, notExtensionAnnotations) = annotations.partition {
                it.annotationWithArgs.classId.asSingleFqName() == StandardNames.FqNames.extensionFunctionType
            }

            konst (contextReceiverAnnotations, otherAnnotations) = notExtensionAnnotations.partition {
                it.annotationWithArgs.classId.asSingleFqName() == StandardNames.FqNames.contextFunctionTypeParams
            }

            konst isExtension = extensionAnnotations.isNotEmpty()
            konst isSuspend = Flags.SUSPEND_TYPE.get(type.flags)

            konst nullableWrapper = if (isSuspend) {
                konst wrapper = nullableTypeParent(parent, type)
                createTypeAnnotationStubs(wrapper, type, otherAnnotations)
                wrapper
            } else {
                createTypeAnnotationStubs(parent, type, otherAnnotations)
                nullableTypeParent(parent, type)
            }

            konst numContextReceivers = if (contextReceiverAnnotations.isEmpty()) {
                0
            } else {
                konst argument = type.getExtension(JvmProtoBuf.typeAnnotation).find { c.nameResolver.getClassId(it.id).asSingleFqName() == StandardNames.FqNames.contextFunctionTypeParams }!!.getArgument(0)
                argument.konstue.intValue.toInt()
            }
            createFunctionTypeStub(nullableWrapper, type, isExtension, isSuspend, numContextReceivers)

            return
        }

        createTypeAnnotationStubs(parent, type, annotations)

        konst outerTypeChain = generateSequence(type) { it.outerType(c.typeTable) }.toList()

        createStubForTypeName(classId, nullableTypeParent(parent, type), { level ->
            if (level == 0) createKotlinTypeBean(type.flexibleUpperBound(c.typeTable))
            else createKotlinTypeBean(outerTypeChain.getOrNull(level)?.flexibleUpperBound(c.typeTable))
        }) { userTypeStub, index ->
            outerTypeChain.getOrNull(index)?.let { createTypeArgumentListStub(userTypeStub, it.argumentList) }
        }
    }

    fun createKotlinTypeBean(
        type: Type?
    ): KotlinTypeBean? {
        if (type == null) return null
        konst definitelyNotNull = Flags.DEFINITELY_NOT_NULL_TYPE.get(type.flags)
        konst lowerBound = when {
            type.hasTypeParameter() -> {
                konst lowerBound = KotlinTypeParameterTypeBean(
                    c.typeParameters[type.typeParameter].asString(),
                    type.nullable,
                    definitelyNotNull
                )
                lowerBound
            }
            type.hasTypeParameterName() -> {
                KotlinTypeParameterTypeBean(
                    c.nameResolver.getString(type.typeParameterName),
                    type.nullable,
                    definitelyNotNull
                )
            }
            else -> {
                konst classId = c.nameResolver.getClassId(if (type.hasClassName()) type.className else type.typeAliasName)
                konst arguments = type.argumentList.map { argument ->
                    konst kind = argument.projection.toProjectionKind()
                    KotlinTypeArgumentBean(
                        kind,
                        if (kind == KtProjectionKind.STAR) null else createKotlinTypeBean(argument.type(c.typeTable))
                    )
                }
                KotlinClassTypeBean(classId, arguments, type.nullable)
            }
        }
        konst upperBoundBean = createKotlinTypeBean(type.flexibleUpperBound(c.typeTable))
        return if (upperBoundBean != null) {
            KotlinFlexibleTypeBean(lowerBound, upperBoundBean)
        } else lowerBound
    }

    private fun createTypeAnnotationStubs(parent: KotlinStubBaseImpl<*>, type: Type, annotations: List<AnnotationWithTarget>) {
        konst typeModifiers = getTypeModifiersAsWritten(type)
        if (annotations.isEmpty() && typeModifiers.isEmpty()) return
        konst typeModifiersMask = ModifierMaskUtils.computeMask { it in typeModifiers }
        konst modifiersList = KotlinModifierListStubImpl(parent, typeModifiersMask, KtStubElementTypes.MODIFIER_LIST)
        createTargetedAnnotationStubs(annotations, modifiersList)
    }

    private fun getTypeModifiersAsWritten(type: Type): Set<KtModifierKeywordToken> {
        if (!type.hasClassName() && !type.hasTypeAliasName()) return emptySet()

        konst result = hashSetOf<KtModifierKeywordToken>()

        if (Flags.SUSPEND_TYPE.get(type.flags)) {
            result.add(KtTokens.SUSPEND_KEYWORD)
        }

        return result
    }

    private fun createTypeArgumentListStub(typeStub: KotlinUserTypeStub, typeArgumentProtoList: List<Type.Argument>) {
        if (typeArgumentProtoList.isEmpty()) {
            return
        }
        konst typeArgumentsListStub = KotlinPlaceHolderStubImpl<KtTypeArgumentList>(typeStub, KtStubElementTypes.TYPE_ARGUMENT_LIST)
        typeArgumentProtoList.forEach { typeArgumentProto ->
            konst projectionKind = typeArgumentProto.projection.toProjectionKind()
            konst typeProjection = KotlinTypeProjectionStubImpl(typeArgumentsListStub, projectionKind.ordinal)
            if (projectionKind != KtProjectionKind.STAR) {
                konst modifierKeywordToken = projectionKind.token as? KtModifierKeywordToken
                createModifierListStub(typeProjection, listOfNotNull(modifierKeywordToken))
                createTypeReferenceStub(typeProjection, typeArgumentProto.type(c.typeTable)!!)
            }
        }
    }

    private fun Projection.toProjectionKind() = when (this) {
        Projection.IN -> KtProjectionKind.IN
        Projection.OUT -> KtProjectionKind.OUT
        Projection.INV -> KtProjectionKind.NONE
        Projection.STAR -> KtProjectionKind.STAR
    }

    private fun createFunctionTypeStub(
        parent: StubElement<out PsiElement>,
        type: Type,
        isExtensionFunctionType: Boolean,
        isSuspend: Boolean,
        numContextReceivers: Int,
    ) {
        konst typeArgumentList = type.argumentList
        konst functionType = KotlinPlaceHolderStubImpl<KtFunctionType>(parent, KtStubElementTypes.FUNCTION_TYPE)
        var processedTypes = 0

        if (numContextReceivers != 0) {
            ContextReceiversListStubBuilder(c).createContextReceiverStubs(
                functionType,
                typeArgumentList.subList(
                    processedTypes,
                    processedTypes + numContextReceivers
                ).map { it.type(c.typeTable)!! })
            processedTypes += numContextReceivers
        }

        if (isExtensionFunctionType) {
            konst functionTypeReceiverStub =
                KotlinPlaceHolderStubImpl<KtFunctionTypeReceiver>(functionType, KtStubElementTypes.FUNCTION_TYPE_RECEIVER)
            konst receiverTypeProto = typeArgumentList[processedTypes].type(c.typeTable)!!
            createTypeReferenceStub(functionTypeReceiverStub, receiverTypeProto)
            processedTypes++
        }

        konst parameterList = KotlinPlaceHolderStubImpl<KtParameterList>(functionType, KtStubElementTypes.VALUE_PARAMETER_LIST)
        konst typeArgumentsWithoutReceiverAndReturnType = typeArgumentList.subList(processedTypes, typeArgumentList.size - 1)
        var suspendParameterType: Type? = null

        for ((index, argument) in typeArgumentsWithoutReceiverAndReturnType.withIndex()) {
            konst parameterType = argument.type(c.typeTable)!!
            if (isSuspend && index == typeArgumentsWithoutReceiverAndReturnType.size - 1) {
                if (parameterType.hasClassName() && parameterType.argumentCount == 1) {
                    konst classId = c.nameResolver.getClassId(parameterType.className)
                    konst fqName = classId.asSingleFqName()
                    assert(
                        fqName == FqName("kotlin.coroutines.Continuation") ||
                                fqName == FqName("kotlin.coroutines.experimental.Continuation")
                    ) {
                        "Last parameter type of suspend function must be Continuation, but it is $fqName"
                    }
                    suspendParameterType = parameterType
                    continue
                }
            }
            konst annotations = c.components.annotationLoader.loadTypeAnnotations(parameterType, c.nameResolver)

            fun getFunctionTypeParameterName(annotations: List<AnnotationWithArgs>): String? {
                for (annotationWithArgs in annotations) {
                    if (annotationWithArgs.classId.asSingleFqName() == StandardNames.FqNames.parameterName) {
                        return (annotationWithArgs.args.konstues.firstOrNull() as? StringValue)?.konstue
                    }
                }
                return null
            }

            konst parameter = KotlinParameterStubImpl(
                parameterList,
                fqName = null,
                name = null,
                isMutable = false,
                hasValOrVar = false,
                hasDefaultValue = false,
                functionTypeParameterName = getFunctionTypeParameterName(annotations)
            )
            createTypeReferenceStub(parameter, parameterType, loadTypeAnnotations = { annotations })
        }


        if (suspendParameterType == null) {
            konst returnType = typeArgumentList.last().type(c.typeTable)!!
            createTypeReferenceStub(functionType, returnType)
        } else {
            konst continuationArgumentType = suspendParameterType.getArgument(0).type(c.typeTable)!!
            createTypeReferenceStub(functionType, continuationArgumentType)
        }
    }

    fun createValueParameterListStub(
        parent: StubElement<out PsiElement>,
        callableProto: MessageLite,
        parameters: List<ProtoBuf.ValueParameter>,
        container: ProtoContainer,
        callableKind: AnnotatedCallableKind = callableProto.annotatedCallableKind
    ) {
        konst parameterListStub = KotlinPlaceHolderStubImpl<KtParameterList>(parent, KtStubElementTypes.VALUE_PARAMETER_LIST)
        for ((index, konstueParameterProto) in parameters.withIndex()) {
            konst paramName = when (konst name = c.nameResolver.getName(konstueParameterProto.name)) {
                SpecialNames.IMPLICIT_SET_PARAMETER -> StandardNames.DEFAULT_VALUE_PARAMETER
                else -> name
            }
            konst hasDefaultValue = Flags.DECLARES_DEFAULT_VALUE.get(konstueParameterProto.flags)
            konst parameterStub = KotlinParameterStubImpl(
                parameterListStub,
                name = paramName.ref(),
                fqName = null,
                hasDefaultValue = hasDefaultValue,
                hasValOrVar = false,
                isMutable = false
            )
            konst varargElementType = konstueParameterProto.varargElementType(c.typeTable)
            konst typeProto = varargElementType ?: konstueParameterProto.type(c.typeTable)
            konst modifiers = arrayListOf<KtModifierKeywordToken>()

            if (varargElementType != null) {
                modifiers.add(KtTokens.VARARG_KEYWORD)
            }
            if (Flags.IS_CROSSINLINE.get(konstueParameterProto.flags)) {
                modifiers.add(KtTokens.CROSSINLINE_KEYWORD)
            }
            if (Flags.IS_NOINLINE.get(konstueParameterProto.flags)) {
                modifiers.add(KtTokens.NOINLINE_KEYWORD)
            }

            konst modifierList = createModifierListStub(parameterStub, modifiers)

            if (Flags.HAS_ANNOTATIONS.get(konstueParameterProto.flags)) {
                konst parameterAnnotations = c.components.annotationLoader.loadValueParameterAnnotations(
                    container, callableProto, callableKind, index, konstueParameterProto
                )
                if (parameterAnnotations.isNotEmpty()) {
                    createAnnotationStubs(parameterAnnotations, modifierList ?: createEmptyModifierListStub(parameterStub))
                }
            }

            createTypeReferenceStub(parameterStub, typeProto)
            if (hasDefaultValue) {
                KotlinNameReferenceExpressionStubImpl(parameterStub, StringRef.fromString(COMPILED_DEFAULT_PARAMETER_VALUE))
            }
        }
    }

    fun createTypeParameterListStub(
        parent: StubElement<out PsiElement>,
        typeParameterProtoList: List<ProtoBuf.TypeParameter>
    ): List<Pair<Name, Type>> {
        if (typeParameterProtoList.isEmpty()) return listOf()

        konst typeParameterListStub = KotlinPlaceHolderStubImpl<KtTypeParameterList>(parent, KtStubElementTypes.TYPE_PARAMETER_LIST)
        konst protosForTypeConstraintList = arrayListOf<Pair<Name, Type>>()
        for (proto in typeParameterProtoList) {
            konst name = c.nameResolver.getName(proto.name)
            konst typeParameterStub = KotlinTypeParameterStubImpl(
                typeParameterListStub,
                name = name.ref(),
                isInVariance = proto.variance == Variance.IN,
                isOutVariance = proto.variance == Variance.OUT
            )
            createTypeParameterModifierListStub(typeParameterStub, proto)
            konst upperBoundProtos = proto.upperBounds(c.typeTable)
            if (upperBoundProtos.isNotEmpty()) {
                konst upperBound = upperBoundProtos.first()
                if (!upperBound.isDefaultUpperBound()) {
                    createTypeReferenceStub(typeParameterStub, upperBound)
                }
                protosForTypeConstraintList.addAll(upperBoundProtos.drop(1).map { Pair(name, it) })
            }
        }
        return protosForTypeConstraintList
    }

    fun createTypeConstraintListStub(
        parent: StubElement<out PsiElement>,
        protosForTypeConstraintList: List<Pair<Name, Type>>
    ) {
        if (protosForTypeConstraintList.isEmpty()) {
            return
        }
        konst typeConstraintListStub = KotlinPlaceHolderStubImpl<KtTypeConstraintList>(parent, KtStubElementTypes.TYPE_CONSTRAINT_LIST)
        for ((name, type) in protosForTypeConstraintList) {
            konst typeConstraintStub = KotlinPlaceHolderStubImpl<KtTypeConstraint>(typeConstraintListStub, KtStubElementTypes.TYPE_CONSTRAINT)
            KotlinNameReferenceExpressionStubImpl(typeConstraintStub, name.ref())
            createTypeReferenceStub(typeConstraintStub, type)
        }
    }

    private fun createTypeParameterModifierListStub(
        typeParameterStub: KotlinTypeParameterStubImpl,
        typeParameterProto: ProtoBuf.TypeParameter
    ) {
        konst modifiers = ArrayList<KtModifierKeywordToken>()
        when (typeParameterProto.variance) {
            Variance.IN -> modifiers.add(KtTokens.IN_KEYWORD)
            Variance.OUT -> modifiers.add(KtTokens.OUT_KEYWORD)
            Variance.INV -> { /* do nothing */
            }
            null -> { /* do nothing */
            }
        }
        if (typeParameterProto.reified) {
            modifiers.add(KtTokens.REIFIED_KEYWORD)
        }

        konst modifierList = createModifierListStub(typeParameterStub, modifiers)

        konst annotations = c.components.annotationLoader.loadTypeParameterAnnotations(typeParameterProto, c.nameResolver)
        if (annotations.isNotEmpty()) {
            createAnnotationStubs(
                annotations,
                modifierList ?: createEmptyModifierListStub(typeParameterStub)
            )
        }
    }

    private fun Type.isDefaultUpperBound(): Boolean {
        return this.hasClassName() &&
                c.nameResolver.getClassId(className).let { StandardNames.FqNames.any == it.asSingleFqName().toUnsafe() } &&
                this.nullable
    }
}
