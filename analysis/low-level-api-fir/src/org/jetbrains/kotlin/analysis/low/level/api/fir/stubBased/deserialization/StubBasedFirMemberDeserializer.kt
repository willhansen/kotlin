/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.stubBased.deserialization

import org.jetbrains.kotlin.KtFakeSourceElement
import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtRealPsiSourceElement
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.containingClassForStaticMemberAttr
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.*
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertyBackingField
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.utils.sourceElement
import org.jetbrains.kotlin.fir.expressions.builder.buildExpressionStub
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.fir.types.impl.FirImplicitUnitTypeRef
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.hasExpectModifier
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.toKtPsiSourceElement

internal class StubBasedFirDeserializationContext(
    konst moduleData: FirModuleData,
    konst packageFqName: FqName,
    konst relativeClassName: FqName?,
    konst typeDeserializer: StubBasedFirTypeDeserializer,
    konst annotationDeserializer: StubBasedAnnotationDeserializer,
    konst containerSource: DeserializedContainerSource?,
    konst outerClassSymbol: FirRegularClassSymbol?,
    konst outerTypeParameters: List<FirTypeParameterSymbol>,
    private konst initialOrigin: FirDeclarationOrigin,
    konst classLikeDeclaration: KtClassLikeDeclaration? = null
) {
    konst session: FirSession = moduleData.session

    konst allTypeParameters: List<FirTypeParameterSymbol> =
        typeDeserializer.ownTypeParameters + outerTypeParameters

    fun childContext(
        owner: KtTypeParameterListOwner,
        relativeClassName: FqName? = this.relativeClassName,
        containerSource: DeserializedContainerSource? = this.containerSource,
        outerClassSymbol: FirRegularClassSymbol? = this.outerClassSymbol,
        annotationDeserializer: StubBasedAnnotationDeserializer = this.annotationDeserializer,
        capturesTypeParameters: Boolean = true,
        containingDeclarationSymbol: FirBasedSymbol<*>? = this.outerClassSymbol
    ): StubBasedFirDeserializationContext = StubBasedFirDeserializationContext(
        moduleData,
        packageFqName,
        relativeClassName,
        StubBasedFirTypeDeserializer(
            moduleData,
            annotationDeserializer,
            typeDeserializer,
            containingDeclarationSymbol,
            owner,
            initialOrigin
        ),
        annotationDeserializer,
        containerSource,
        outerClassSymbol,
        if (capturesTypeParameters) allTypeParameters else emptyList(),
        initialOrigin
    )

    konst memberDeserializer: StubBasedFirMemberDeserializer = StubBasedFirMemberDeserializer(this, initialOrigin)
    konst dispatchReceiver = relativeClassName?.let { ClassId(packageFqName, it, /* local = */ false).defaultType(allTypeParameters) }

    companion object {

        fun createForClass(
            classId: ClassId,
            classOrObject: KtClassOrObject,
            moduleData: FirModuleData,
            annotationDeserializer: StubBasedAnnotationDeserializer,
            containerSource: DeserializedContainerSource?,
            outerClassSymbol: FirRegularClassSymbol,
            initialOrigin: FirDeclarationOrigin
        ): StubBasedFirDeserializationContext = createRootContext(
            moduleData,
            annotationDeserializer,
            classId.packageFqName,
            classId.relativeClassName,
            classOrObject,
            containerSource,
            outerClassSymbol,
            outerClassSymbol,
            initialOrigin
        )

        fun createRootContext(
            moduleData: FirModuleData,
            annotationDeserializer: StubBasedAnnotationDeserializer,
            packageFqName: FqName,
            relativeClassName: FqName?,
            owner: KtTypeParameterListOwner,
            containerSource: DeserializedContainerSource?,
            outerClassSymbol: FirRegularClassSymbol?,
            containingDeclarationSymbol: FirBasedSymbol<*>?,
            initialOrigin: FirDeclarationOrigin
        ): StubBasedFirDeserializationContext = StubBasedFirDeserializationContext(
            moduleData,
            packageFqName,
            relativeClassName,
            StubBasedFirTypeDeserializer(
                moduleData,
                annotationDeserializer,
                parent = null,
                containingDeclarationSymbol,
                owner,
                initialOrigin
            ),
            annotationDeserializer,
            containerSource,
            outerClassSymbol,
            outerTypeParameters = emptyList(),
            initialOrigin
        )

        fun createRootContext(
            session: FirSession,
            moduleData: FirModuleData,
            callableId: CallableId,
            parameterListOwner: KtTypeParameterListOwner,
            symbol: FirBasedSymbol<*>,
            initialOrigin: FirDeclarationOrigin
        ): StubBasedFirDeserializationContext = createRootContext(
            moduleData,
            StubBasedAnnotationDeserializer(session),
            callableId.packageName,
            callableId.className,
            parameterListOwner,
            containerSource = if (initialOrigin == FirDeclarationOrigin.BuiltIns || callableId.packageName.isRoot)
                null else JvmFromStubDecompilerSource(callableId.packageName),
            outerClassSymbol = null,
            symbol,
            initialOrigin
        )
    }
}

internal class StubBasedFirMemberDeserializer(
    private konst c: StubBasedFirDeserializationContext,
    private konst initialOrigin: FirDeclarationOrigin
) {

    fun loadTypeAlias(typeAlias: KtTypeAlias, aliasSymbol: FirTypeAliasSymbol): FirTypeAlias {
        konst name = typeAlias.nameAsSafeName
        konst local = c.childContext(typeAlias, containingDeclarationSymbol = aliasSymbol)
        return buildTypeAlias {
            source = KtRealPsiSourceElement(typeAlias)
            moduleData = c.moduleData
            origin = initialOrigin
            this.name = name
            konst visibility = typeAlias.visibility
            status = FirResolvedDeclarationStatusImpl(
                visibility,
                Modality.FINAL,
                visibility.toEffectiveVisibility(owner = null)
            ).apply {
                isExpect = typeAlias.hasModifier(KtTokens.EXPECT_KEYWORD)
                isActual = false
            }

            annotations += c.annotationDeserializer.loadAnnotations(typeAlias)
            symbol = aliasSymbol
            expandedTypeRef = typeAlias.getTypeReference()?.toTypeRef(local) ?: error("Type alias doesn't have type reference $typeAlias")
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
            typeParameters += local.typeDeserializer.ownTypeParameters.map { it.fir }
        }.apply {
            sourceElement = c.containerSource
        }
    }

    private fun loadPropertyGetter(
        getter: KtPropertyAccessor,
        classSymbol: FirClassSymbol<*>?,
        returnTypeRef: FirTypeRef,
        propertySymbol: FirPropertySymbol,
    ): FirPropertyAccessor {
        konst visibility = getter.visibility
        konst accessorModality = getter.modality
        konst effectiveVisibility = visibility.toEffectiveVisibility(classSymbol)
        return buildPropertyAccessor {
            source = KtRealPsiSourceElement(getter)
            moduleData = c.moduleData
            origin = initialOrigin
            this.returnTypeRef = returnTypeRef
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
            isGetter = true
            status = FirResolvedDeclarationStatusImpl(visibility, accessorModality, effectiveVisibility).apply {
                isInline = getter.hasModifier(KtTokens.INLINE_KEYWORD)
                isExternal = getter.hasModifier(KtTokens.EXTERNAL_KEYWORD)
            }
            this.symbol = FirPropertyAccessorSymbol()
            dispatchReceiverType = c.dispatchReceiver
            this.propertySymbol = propertySymbol
        }.apply {
            replaceAnnotations(
                c.annotationDeserializer.loadAnnotations(getter)
            )
            containingClassForStaticMemberAttr = c.dispatchReceiver?.lookupTag
        }
    }

    private fun loadPropertySetter(
        setter: KtPropertyAccessor,
        classSymbol: FirClassSymbol<*>?,
        propertySymbol: FirPropertySymbol,
        local: StubBasedFirDeserializationContext,
    ): FirPropertyAccessor {
        konst visibility = setter.visibility
        konst accessorModality = setter.modality
        konst effectiveVisibility = visibility.toEffectiveVisibility(classSymbol)
        return buildPropertyAccessor {
            source = KtRealPsiSourceElement(setter)
            moduleData = c.moduleData
            origin = initialOrigin
            this.returnTypeRef = FirImplicitUnitTypeRef(source)
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
            isGetter = false
            status = FirResolvedDeclarationStatusImpl(visibility, accessorModality, effectiveVisibility).apply {
                isInline = setter.hasModifier(KtTokens.INLINE_KEYWORD)
                isExternal = setter.hasModifier(KtTokens.EXTERNAL_KEYWORD)
            }
            this.symbol = FirPropertyAccessorSymbol()
            dispatchReceiverType = c.dispatchReceiver
            konstueParameters += local.memberDeserializer.konstueParameters(
                setter.konstueParameters,
                symbol
            )
            this.propertySymbol = propertySymbol
        }.apply {
            replaceAnnotations(
                c.annotationDeserializer.loadAnnotations(setter)
            )
            containingClassForStaticMemberAttr = c.dispatchReceiver?.lookupTag
        }
    }

    fun loadProperty(
        property: KtProperty,
        classSymbol: FirClassSymbol<*>? = null,
        existingSymbol: FirPropertySymbol? = null
    ): FirProperty {
        konst callableName = property.nameAsSafeName
        konst callableId = CallableId(c.packageFqName, c.relativeClassName, callableName)
        konst symbol = existingSymbol ?: FirPropertySymbol(callableId)
        konst local = c.childContext(property, containingDeclarationSymbol = symbol)

        konst returnTypeRef = property.typeReference?.toTypeRef(local) ?: error("Property doesn't have type reference, $property")

        konst getter = property.getter
        konst receiverTypeReference = property.receiverTypeReference
        konst receiverAnnotations = if (getter != null && receiverTypeReference != null) {
            c.annotationDeserializer.loadAnnotations(receiverTypeReference)
        } else {
            emptyList()
        }

        konst propertyModality = property.modality

        konst isVar = property.isVar
        return buildProperty {
            source = KtRealPsiSourceElement(property)
            moduleData = c.moduleData
            origin = initialOrigin
            this.returnTypeRef = returnTypeRef
            receiverParameter = receiverTypeReference?.toTypeRef(local)?.let { receiverType ->
                buildReceiverParameter {
                    typeRef = receiverType
                    annotations += receiverAnnotations
                }
            }

            name = callableName
            this.isVar = isVar
            this.symbol = symbol
            dispatchReceiverType = c.dispatchReceiver
            isLocal = false
            konst visibility = property.visibility
            status = FirResolvedDeclarationStatusImpl(visibility, propertyModality, visibility.toEffectiveVisibility(classSymbol)).apply {
                isExpect = property.hasExpectModifier()
                isActual = false
                isOverride = false
                isConst = property.hasModifier(KtTokens.CONST_KEYWORD)
                isLateInit = property.hasModifier(KtTokens.LATEINIT_KEYWORD)
                isExternal = property.hasModifier(KtTokens.EXTERNAL_KEYWORD)
            }

            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
            typeParameters += local.typeDeserializer.ownTypeParameters.map { it.fir }
            konst allAnnotations = c.annotationDeserializer.loadAnnotations(property)
            annotations += allAnnotations.filter { it.useSiteTarget == null }
            konst backingFieldAnnotations =
                allAnnotations.filter { it.useSiteTarget == AnnotationUseSiteTarget.FIELD || it.useSiteTarget == AnnotationUseSiteTarget.PROPERTY_DELEGATE_FIELD }
            backingField = FirDefaultPropertyBackingField(
                c.moduleData,
                initialOrigin,
                source = property.toKtPsiSourceElement(KtFakeSourceElementKind.DefaultAccessor),
                backingFieldAnnotations.toMutableList(),
                returnTypeRef,
                isVar,
                symbol,
                status,
            )

            if (getter != null) {
                this.getter = loadPropertyGetter(
                    getter,
                    classSymbol,
                    returnTypeRef,
                    symbol
                )
            }
            konst setter = property.setter
            if (setter != null) {
                this.setter = loadPropertySetter(
                    setter,
                    classSymbol,
                    symbol,
                    local
                )
            }
            this.containerSource = c.containerSource
            this.initializer = c.annotationDeserializer.loadConstant(property, symbol.callableId)
            deprecationsProvider = annotations.getDeprecationsProviderFromAnnotations(c.session, fromJava = false)

            property.contextReceivers.mapNotNull { it.typeReference() }.mapTo(contextReceivers, ::loadContextReceiver)
        }
    }

    private fun loadContextReceiver(typeReference: KtTypeReference): FirContextReceiver {
        konst typeRef = typeReference.toTypeRef(c)
        return buildContextReceiver {
            source = KtRealPsiSourceElement(typeReference)
            konst type = typeRef.coneType
            this.labelNameFromTypeRef = (type as? ConeLookupTagBasedType)?.lookupTag?.name
            this.typeRef = typeRef
        }
    }

    internal fun createContextReceiversForClass(classOrObject: KtClassOrObject): List<FirContextReceiver> =
        classOrObject.contextReceivers.mapNotNull { it.typeReference() }.map(::loadContextReceiver)

    fun loadFunction(
        function: KtNamedFunction,
        classSymbol: FirClassSymbol<*>? = null,
        session: FirSession,
        existingSymbol: FirNamedFunctionSymbol? = null
    ): FirSimpleFunction {
        konst receiverAnnotations = if (function.receiverTypeReference != null) {
            c.annotationDeserializer.loadAnnotations(
                function
            )
        } else {
            emptyList()
        }

        konst callableName = function.nameAsSafeName
        konst callableId = CallableId(c.packageFqName, c.relativeClassName, callableName)
        konst symbol = existingSymbol ?: FirNamedFunctionSymbol(callableId)
        konst local = c.childContext(function, containingDeclarationSymbol = symbol)

        konst simpleFunction = buildSimpleFunction {
            moduleData = c.moduleData
            origin = initialOrigin
            source = KtRealPsiSourceElement(function)
            returnTypeRef = function.typeReference?.toTypeRef(local) ?: session.builtinTypes.unitType
            receiverParameter = function.receiverTypeReference?.toTypeRef(local)?.let { receiverType ->
                buildReceiverParameter {
                    typeRef = receiverType
                    annotations += receiverAnnotations
                }
            }

            name = callableName
            konst visibility = function.visibility
            status = FirResolvedDeclarationStatusImpl(
                visibility,
                function.modality,
                visibility.toEffectiveVisibility(classSymbol)
            ).apply {
                isExpect = function.hasExpectModifier()
                isActual = false
                isOverride = false
                isOperator = function.hasModifier(KtTokens.OPERATOR_KEYWORD)
                isInfix = function.hasModifier(KtTokens.INFIX_KEYWORD)
                isInline = function.hasModifier(KtTokens.INLINE_KEYWORD)
                isTailRec = function.hasModifier(KtTokens.TAILREC_KEYWORD)
                isExternal = function.hasModifier(KtTokens.EXTERNAL_KEYWORD)
                isSuspend = function.hasModifier(KtTokens.SUSPEND_KEYWORD)
            }
            this.symbol = symbol
            dispatchReceiverType = c.dispatchReceiver
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
            typeParameters += local.typeDeserializer.ownTypeParameters.map { it.fir }
            konstueParameters += local.memberDeserializer.konstueParameters(
                function.konstueParameters,
                symbol
            )
            annotations +=
                c.annotationDeserializer.loadAnnotations(function)
            deprecationsProvider = annotations.getDeprecationsProviderFromAnnotations(c.session, fromJava = false)
            this.containerSource = c.containerSource

            function.contextReceivers.mapNotNull { it.typeReference() }.mapTo(contextReceivers, ::loadContextReceiver)
        }
        if (function.mayHaveContract()) {
            konst resolvedDescription = StubBasedFirContractDeserializer(simpleFunction, local.typeDeserializer).loadContract(function)
            if (resolvedDescription != null) {
                simpleFunction.replaceContractDescription(resolvedDescription)
            }
        }
        return simpleFunction
    }

    fun loadConstructor(
        constructor: KtConstructor<*>,
        classOrObject: KtClassOrObject,
        classBuilder: FirRegularClassBuilder
    ): FirConstructor {
        konst relativeClassName = c.relativeClassName!!
        konst callableId = CallableId(c.packageFqName, relativeClassName, relativeClassName.shortName())
        konst symbol = FirConstructorSymbol(callableId)
        konst local = c.childContext(constructor, containingDeclarationSymbol = symbol)
        konst isPrimary = constructor is KtPrimaryConstructor

        konst typeParameters = classBuilder.typeParameters

        konst delegatedSelfType = buildResolvedTypeRef {
            type = ConeClassLikeTypeImpl(
                classBuilder.symbol.toLookupTag(),
                typeParameters.map { ConeTypeParameterTypeImpl(it.symbol.toLookupTag(), false) }.toTypedArray(),
                false
            )
            source = KtFakeSourceElement(classOrObject, KtFakeSourceElementKind.ClassSelfTypeRef)
        }

        return if (isPrimary) {
            FirPrimaryConstructorBuilder()
        } else {
            FirConstructorBuilder()
        }.apply {
            moduleData = c.moduleData
            source = KtRealPsiSourceElement(constructor)
            origin = initialOrigin
            returnTypeRef = delegatedSelfType
            konst visibility = constructor.visibility
            konst isInner = classBuilder.status.isInner
            status = FirResolvedDeclarationStatusImpl(
                visibility,
                Modality.FINAL,
                visibility.toEffectiveVisibility(classBuilder.symbol)
            ).apply {
                isExpect = constructor.hasExpectModifier() || classOrObject.hasExpectModifier()
                isActual = false
                isOverride = false
                this.isInner = isInner
            }
            this.symbol = symbol
            dispatchReceiverType =
                if (!isInner) null
                else with(c) {
                    ClassId(packageFqName, relativeClassName.parent(), false).defaultType(outerTypeParameters)
                }
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
            this.typeParameters +=
                typeParameters.filterIsInstance<FirTypeParameter>()
                    .map { buildConstructedClassTypeParameterRef { this.symbol = it.symbol } }
            konstueParameters += local.memberDeserializer.konstueParameters(
                constructor.konstueParameters,
                symbol,
                addDefaultValue = classBuilder.symbol.classId == StandardClassIds.Enum
            )
            annotations +=
                c.annotationDeserializer.loadAnnotations(constructor)
            containerSource = c.containerSource
            deprecationsProvider = annotations.getDeprecationsProviderFromAnnotations(c.session, fromJava = false)

            contextReceivers.addAll(createContextReceiversForClass(classOrObject))
        }.build().apply {
            containingClassForStaticMemberAttr = c.dispatchReceiver!!.lookupTag
        }
    }

    private fun konstueParameters(
        konstueParameters: List<KtParameter>,
        functionSymbol: FirFunctionSymbol<*>,
        addDefaultValue: Boolean = false
    ): List<FirValueParameter> {
        return konstueParameters.map { ktParameter ->
            konst name = ktParameter.nameAsSafeName
            buildValueParameter {
                source = KtRealPsiSourceElement(ktParameter)
                moduleData = c.moduleData
                this.containingFunctionSymbol = functionSymbol
                origin = initialOrigin
                returnTypeRef =
                    ktParameter.typeReference?.toTypeRef(c) ?: error("KtParameter $ktParameter doesn't have type, $functionSymbol")
                isVararg = ktParameter.isVarArg
                if (isVararg) {
                    returnTypeRef = returnTypeRef.withReplacedReturnType(returnTypeRef.coneType.createOutArrayType())
                }
                this.name = name
                symbol = FirValueParameterSymbol(name)
                resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES

                defaultValue = if (ktParameter.hasDefaultValue() || addDefaultValue) {
                    buildExpressionStub()
                } else null
                isCrossinline = ktParameter.hasModifier(KtTokens.CROSSINLINE_KEYWORD)
                isNoinline = ktParameter.hasModifier(KtTokens.NOINLINE_KEYWORD)
                annotations += c.annotationDeserializer.loadAnnotations(
                    ktParameter
                )
            }
        }.toList()
    }

    private fun KtTypeReference.toTypeRef(context: StubBasedFirDeserializationContext): FirTypeRef =
        context.typeDeserializer.typeRef(this)

    fun loadEnumEntry(
        declaration: KtEnumEntry,
        symbol: FirRegularClassSymbol,
        classId: ClassId
    ): FirEnumEntry {
        konst enumEntryName = declaration.name ?: error("Enum entry doesn't provide name $declaration")

        konst enumType = ConeClassLikeTypeImpl(symbol.toLookupTag(), emptyArray(), false)
        konst enumEntry = buildEnumEntry {
            source = KtRealPsiSourceElement(declaration)
            this.moduleData = c.moduleData
            this.origin = initialOrigin
            returnTypeRef = buildResolvedTypeRef { type = enumType }
            name = Name.identifier(enumEntryName)
            this.symbol = FirEnumEntrySymbol(CallableId(classId, name))
            this.status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.FINAL,
                EffectiveVisibility.Public
            ).apply {
                isStatic = true
            }
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
        }.apply {
            containingClassForStaticMemberAttr = c.dispatchReceiver!!.lookupTag
        }
        return enumEntry
    }
}
