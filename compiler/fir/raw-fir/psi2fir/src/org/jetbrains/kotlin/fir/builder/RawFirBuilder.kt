/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.builder

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.util.AstLoadingFilter
import org.jetbrains.kotlin.*
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.StandardNames.BACKING_FIELD
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget.*
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.contracts.FirContractDescription
import org.jetbrains.kotlin.fir.contracts.builder.buildRawContractDescription
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.*
import org.jetbrains.kotlin.fir.declarations.impl.*
import org.jetbrains.kotlin.fir.declarations.utils.*
import org.jetbrains.kotlin.fir.diagnostics.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.expressions.impl.FirContractCallBlock
import org.jetbrains.kotlin.fir.expressions.impl.FirSingleExpressionBlock
import org.jetbrains.kotlin.fir.extensions.extensionService
import org.jetbrains.kotlin.fir.references.builder.*
import org.jetbrains.kotlin.fir.scopes.FirScopeProvider
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.*
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.impl.FirImplicitTypeRefImplWithoutSource
import org.jetbrains.kotlin.fir.types.impl.FirQualifierPartImpl
import org.jetbrains.kotlin.fir.types.impl.FirTypeArgumentListImpl
import org.jetbrains.kotlin.lexer.KtTokens.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.NameUtils
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.expressions.OperatorConventions
import org.jetbrains.kotlin.util.OperatorNameConventions
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import org.jetbrains.kotlin.utils.addToStdlib.runIf
import org.jetbrains.kotlin.utils.addToStdlib.shouldNotBeCalled

open class RawFirBuilder(
    session: FirSession,
    konst baseScopeProvider: FirScopeProvider,
    bodyBuildingMode: BodyBuildingMode = BodyBuildingMode.NORMAL
) : BaseFirBuilder<PsiElement>(session) {
    protected open fun bindFunctionTarget(target: FirFunctionTarget, function: FirFunction) = target.bind(function)
    protected open fun FirFunctionBuilder.additionalFunctionInit() {}
    protected open fun FirPropertyBuilder.additionalPropertyInit() {}
    protected open fun FirPropertyAccessorBuilder.additionalPropertyAccessorInit() {}
    protected open fun FirBackingFieldBuilder.additionalBackingFieldInit() {}

    var mode: BodyBuildingMode = bodyBuildingMode
        private set

    private inline fun <T> runOnStubs(crossinline body: () -> T): T {
        return when (mode) {
            BodyBuildingMode.NORMAL -> body()
            BodyBuildingMode.LAZY_BODIES -> {
                AstLoadingFilter.disallowTreeLoading<T, Nothing> { body() }
            }
        }
    }

    fun buildFirFile(file: KtFile): FirFile {
        return runOnStubs { file.accept(Visitor(), null) as FirFile }
    }

    fun buildAnnotationCall(annotation: KtAnnotationEntry): FirAnnotationCall {
        return Visitor().visitAnnotationEntry(annotation, null) as FirAnnotationCall
    }

    fun buildTypeReference(reference: KtTypeReference): FirTypeRef {
        return reference.accept(Visitor(), null) as FirTypeRef
    }

    override fun PsiElement.toFirSourceElement(kind: KtFakeSourceElementKind?): KtPsiSourceElement {
        konst actualKind = kind ?: this@RawFirBuilder.context.forcedElementSourceKind ?: KtRealSourceElementKind
        return this.toKtPsiSourceElement(actualKind)
    }

    override konst PsiElement.elementType: IElementType
        get() = node.elementType

    override konst PsiElement.asText: String
        get() = text

    override konst PsiElement.unescapedValue: String
        get() = (this as KtEscapeStringTemplateEntry).unescapedValue

    override fun PsiElement.getChildNodeByType(type: IElementType): PsiElement? {
        return children.firstOrNull { it.node.elementType == type }
    }

    override fun PsiElement.getReferencedNameAsName(): Name {
        return (this as KtSimpleNameExpression).getReferencedNameAsName()
    }

    override fun PsiElement.getLabelName(): String? {
        return when (this) {
            is KtExpressionWithLabel -> getLabelName()
            is KtNamedFunction -> parent.getLabelName()
            else -> null
        }
    }

    override fun PsiElement.getExpressionInParentheses(): PsiElement? {
        return (this as KtParenthesizedExpression).expression
    }

    override fun PsiElement.getAnnotatedExpression(): PsiElement? {
        return (this as KtAnnotatedExpression).baseExpression
    }

    override fun PsiElement.getLabeledExpression(): PsiElement? {
        return (this as KtLabeledExpression).baseExpression
    }

    override konst PsiElement?.receiverExpression: PsiElement?
        get() = (this as? KtQualifiedExpression)?.receiverExpression

    override konst PsiElement?.selectorExpression: PsiElement?
        get() = (this as? KtQualifiedExpression)?.selectorExpression

    override konst PsiElement?.arrayExpression: PsiElement?
        get() = (this as? KtArrayAccessExpression)?.arrayExpression

    override konst PsiElement?.indexExpressions: List<PsiElement>?
        get() = (this as? KtArrayAccessExpression)?.indexExpressions

    private konst KtModifierListOwner.visibility: Visibility
        get() = with(modifierList) {
            when {
                this == null -> Visibilities.Unknown
                hasModifier(PRIVATE_KEYWORD) -> Visibilities.Private
                hasModifier(PUBLIC_KEYWORD) -> Visibilities.Public
                hasModifier(PROTECTED_KEYWORD) -> Visibilities.Protected
                else -> if (hasModifier(INTERNAL_KEYWORD)) Visibilities.Internal else Visibilities.Unknown
            }
        }

    private konst KtDeclaration.modality: Modality?
        get() = with(modifierList) {
            when {
                this == null -> null
                hasModifier(FINAL_KEYWORD) -> Modality.FINAL
                hasModifier(SEALED_KEYWORD) -> if (this@modality is KtClassOrObject) Modality.SEALED else null
                hasModifier(ABSTRACT_KEYWORD) -> Modality.ABSTRACT
                else -> if (hasModifier(OPEN_KEYWORD)) Modality.OPEN else null
            }
        }

    protected open inner class Visitor : KtVisitor<FirElement, FirElement?>() {
        private inline fun <reified R : FirElement> KtElement?.convertSafe(): R? =
            this?.let { convertElement(it, null)} as? R

        private inline fun <reified R : FirElement> KtElement.convert(): R =
            convertElement(this, null) as R

        private inline fun <T> buildOrLazy(build: () -> T, noinline lazy: () -> T): T {
            return when (mode) {
                BodyBuildingMode.NORMAL -> build()
                BodyBuildingMode.LAZY_BODIES -> runOnStubs(lazy)
            }
        }

        private inline fun buildOrLazyExpression(
            sourceElement: KtSourceElement?,
            buildExpression: () -> FirExpression,
        ): FirExpression {
            return buildOrLazy(buildExpression, {
                buildLazyExpression {
                    source = sourceElement
                }
            })
        }

        private inline fun buildOrLazyBlock(buildBlock: () -> FirBlock): FirBlock {
            return buildOrLazy(buildBlock, ::buildLazyBlock)
        }

        private inline fun buildOrLazyDelegatedConstructorCall(
            isThis: Boolean,
            constructedTypeRef: FirTypeRef,
            buildCall: () -> FirDelegatedConstructorCall
        ): FirDelegatedConstructorCall {
            return buildOrLazy(buildCall, {
                buildLazyDelegatedConstructorCall {
                    this.isThis = isThis
                    this.constructedTypeRef = constructedTypeRef
                    calleeReference = if (isThis) {
                        buildExplicitThisReference {
                            source = null
                        }
                    } else {
                        buildExplicitSuperReference {
                            source = null
                            superTypeRef = constructedTypeRef
                        }
                    }
                }
            })
        }

        open fun convertElement(element: KtElement, original: FirElement? = null): FirElement? =
            element.accept(this@Visitor, original)

        open fun convertProperty(
            property: KtProperty, ownerRegularOrAnonymousObjectSymbol: FirClassSymbol<*>?,
            ownerRegularClassTypeParametersCount: Int?
        ): FirProperty = property.toFirProperty(
            ownerRegularOrAnonymousObjectSymbol,
            context
        )

        open fun convertValueParameter(
            konstueParameter: KtParameter,
            functionSymbol: FirFunctionSymbol<*>,
            defaultTypeRef: FirTypeRef? = null,
            konstueParameterDeclaration: ValueParameterDeclaration,
            additionalAnnotations: List<FirAnnotation> = emptyList()
        ): FirValueParameter =
            konstueParameter.toFirValueParameter(defaultTypeRef, functionSymbol, konstueParameterDeclaration, additionalAnnotations)

        private fun KtTypeReference?.toFirOrImplicitType(): FirTypeRef =
            this?.toFirOrErrorType() ?: FirImplicitTypeRefImplWithoutSource

        private fun KtTypeReference?.toFirOrUnitType(): FirTypeRef =
            convertSafe() ?: implicitUnitType

        protected fun KtTypeReference?.toFirOrErrorType(): FirTypeRef =
            convertSafe() ?: buildErrorTypeRef {
                source = this@toFirOrErrorType?.toFirSourceElement()
                diagnostic = ConeSyntaxDiagnostic(
                    if (this@toFirOrErrorType == null) "Incomplete code" else "Conversion failed"
                )
            }

        // Here we accept lambda as receiver to prevent expression calculation in stub mode
        private fun (() -> KtExpression?).toFirExpression(errorReason: String): FirExpression =
            this().toFirExpression(errorReason)

        private fun KtElement?.toFirExpression(
            errorReason: String,
            kind: DiagnosticKind = DiagnosticKind.ExpressionExpected
        ): FirExpression = toFirExpression { ConeSimpleDiagnostic(errorReason, kind) }

        private inline fun KtElement?.toFirExpression(diagnosticFn: () -> ConeDiagnostic): FirExpression {
            if (this == null) {
                return buildErrorExpression(source = null, diagnosticFn())
            }

            konst result = when (konst fir = convertElement(this, null)) {
                is FirExpression -> fir
                else -> {
                    return buildErrorExpression {
                        nonExpressionElement = fir
                        diagnostic = diagnosticFn()
                        source = toFirSourceElement()
                    }
                }
            }

            return toFirExpression(result)
        }

        private fun KtElement.toFirExpression(result: FirExpression): FirExpression {
            konst callExpressionCallee = (this as? KtCallExpression)?.calleeExpression?.unwrapParenthesesLabelsAndAnnotations()

            if (this is KtNameReferenceExpression ||
                (this is KtCallExpression && callExpressionCallee !is KtLambdaExpression) ||
                getQualifiedExpressionForSelector() == null
            ) {
                return result
            }

            return buildErrorExpression {
                source = callExpressionCallee?.toFirSourceElement() ?: toFirSourceElement()
                diagnostic =
                    ConeSimpleDiagnostic(
                        "The expression cannot be a selector (occur after a dot)",
                        if (callExpressionCallee == null) DiagnosticKind.IllegalSelector else DiagnosticKind.NoReceiverAllowed
                    )
                expression = result
            }
        }

        private inline fun KtExpression.toFirStatement(errorReasonLazy: () -> String): FirStatement {
            return when (konst fir = convertElement(this, null)) {
                is FirStatement -> fir
                else -> buildErrorExpression {
                    nonExpressionElement = fir
                    diagnostic = ConeSyntaxDiagnostic(errorReasonLazy())
                    source = toFirSourceElement()
                }
            }
        }

        private fun KtExpression.toFirStatement(): FirStatement =
            convert()

        private fun KtDeclaration.toFirDeclaration(
            delegatedSuperType: FirTypeRef,
            delegatedSelfType: FirResolvedTypeRef,
            owner: KtClassOrObject,
            ownerClassBuilder: FirClassBuilder,
            ownerTypeParameters: List<FirTypeParameterRef>
        ): FirDeclaration {
            return when (this) {
                is KtSecondaryConstructor -> {
                    toFirConstructor(
                        if (isDelegatedCallToThis()) delegatedSelfType else delegatedSuperType,
                        delegatedSelfType,
                        owner,
                        ownerTypeParameters,
                    )
                }
                is KtEnumEntry -> {
                    konst primaryConstructor = owner.primaryConstructor
                    konst ownerClassHasDefaultConstructor =
                        primaryConstructor?.konstueParameters?.isEmpty() ?: owner.secondaryConstructors.let { constructors ->
                            constructors.isEmpty() || constructors.any { it.konstueParameters.isEmpty() }
                        }
                    toFirEnumEntry(delegatedSelfType, ownerClassHasDefaultConstructor)
                }
                is KtProperty -> {
                    convertProperty(
                        this@toFirDeclaration,
                        ownerClassBuilder.ownerRegularOrAnonymousObjectSymbol,
                        ownerClassBuilder.ownerRegularClassTypeParametersCount
                    )
                }
                is KtDestructuringDeclaration -> {
                    buildErrorTopLevelDestructuringDeclaration(toFirSourceElement())
                }
                else -> convert()
            }
        }

        private fun KtExpression?.toFirBlock(): FirBlock =
            when (this) {
                is KtBlockExpression ->
                    accept(this@Visitor, null) as FirBlock
                null ->
                    buildEmptyExpressionBlock()
                else -> {
                    var firBlock: FirBlock? = null
                    if (this is KtAnnotatedExpression) {
                        konst lastChild = children.lastOrNull()
                        if (lastChild is KtBlockExpression) {
                            firBlock = lastChild.toFirBlock()
                            extractAnnotationsTo(firBlock)
                        }
                    }
                    firBlock ?: FirSingleExpressionBlock(convert())
                }
            }

        private fun KtDeclarationWithBody.buildFirBody(): Pair<FirBlock?, FirContractDescription?> =
            if (hasBody()) {
                buildOrLazyBlock {
                    if (hasBlockBody()) {
                        konst block = bodyBlockExpression?.accept(this@Visitor, null) as? FirBlock
                        konst contractDescription = when {
                            !hasContractEffectList() -> block?.let(::processLegacyContractDescription)
                            else -> null
                        }
                        return@buildFirBody block to contractDescription
                    } else {
                        konst result = { bodyExpression }.toFirExpression("Function has no body (but should)")
                        FirSingleExpressionBlock(result.toReturn(baseSource = result.source))
                    }
                } to null
            } else {
                null to null
            }

        private fun ValueArgument?.toFirExpression(): FirExpression {
            if (this == null) {
                return buildErrorExpression(
                    source = null,
                    ConeSyntaxDiagnostic("No argument given"),
                )
            }
            konst name = this.getArgumentName()?.asName
            konst firExpression = when (konst expression = this.getArgumentExpression()) {
                is KtConstantExpression, is KtStringTemplateExpression -> {
                    expression.accept(this@Visitor, null) as FirExpression
                }

                else -> {
                    { expression }.toFirExpression("Argument is absent")
                }
            }
            konst isSpread = getSpreadElement() != null
            return when {
                name != null -> buildNamedArgumentExpression {
                    source = (this@toFirExpression as? PsiElement)?.toFirSourceElement()
                    this.expression = firExpression
                    this.isSpread = isSpread
                    this.name = name
                }
                isSpread -> buildSpreadArgumentExpression {
                    source = (this@toFirExpression as? PsiElement)?.toFirSourceElement()
                    this.expression = firExpression
                }
                else -> firExpression
            }
        }

        private fun KtPropertyAccessor?.toFirPropertyAccessor(
            property: KtProperty,
            propertyTypeRef: FirTypeRef,
            propertySymbol: FirPropertySymbol,
            isGetter: Boolean,
            accessorAnnotationsFromProperty: List<FirAnnotation>,
            parameterAnnotationsFromProperty: List<FirAnnotation>,
        ): FirPropertyAccessor? {
            konst accessorVisibility =
                if (this?.visibility != null && this.visibility != Visibilities.Unknown) this.visibility else property.visibility
            // Downward propagation of `inline` and `external` modifiers (from property to its accessors)
            konst status =
                FirDeclarationStatusImpl(accessorVisibility, this?.modality).apply {
                    isInline = property.hasModifier(INLINE_KEYWORD) ||
                            this@toFirPropertyAccessor?.hasModifier(INLINE_KEYWORD) == true
                    isExternal = property.hasModifier(EXTERNAL_KEYWORD) ||
                            this@toFirPropertyAccessor?.hasModifier(EXTERNAL_KEYWORD) == true
                }
            konst propertyTypeRefToUse = propertyTypeRef.copyWithNewSourceKind(KtFakeSourceElementKind.ImplicitTypeRef)
            return when {
                this != null && hasBody() -> {
                    // Property has a non-default getter or setter.
                    // NOTE: We still need the setter even for a konst property so we can report errors (e.g., VAL_WITH_SETTER).
                    konst source = this.toFirSourceElement()
                    konst accessorTarget = FirFunctionTarget(labelName = null, isLambda = false)
                    buildPropertyAccessor {
                        this.source = source
                        moduleData = baseModuleData
                        origin = FirDeclarationOrigin.Source
                        returnTypeRef = if (isGetter) {
                            returnTypeReference?.convertSafe() ?: propertyTypeRefToUse
                        } else {
                            returnTypeReference.toFirOrUnitType()
                        }
                        this.isGetter = isGetter
                        this.status = status
                        annotations += accessorAnnotationsFromProperty
                        extractAnnotationsTo(this)
                        this@RawFirBuilder.context.firFunctionTargets += accessorTarget
                        symbol = FirPropertyAccessorSymbol()
                        extractValueParametersTo(
                            this, symbol, ValueParameterDeclaration.SETTER, propertyTypeRefToUse, parameterAnnotationsFromProperty
                        )
                        if (!isGetter && konstueParameters.isEmpty()) {
                            konstueParameters += buildDefaultSetterValueParameter {
                                this.source = source.fakeElement(KtFakeSourceElementKind.DefaultAccessor)
                                moduleData = baseModuleData
                                origin = FirDeclarationOrigin.Source
                                returnTypeRef = propertyTypeRefToUse
                                symbol = FirValueParameterSymbol(StandardNames.DEFAULT_VALUE_PARAMETER)
                                annotations += parameterAnnotationsFromProperty
                            }
                        }
                        konst outerContractDescription = this@toFirPropertyAccessor.obtainContractDescription()
                        konst (body, innerContractDescription) = this@toFirPropertyAccessor.buildFirBody()
                        this.body = body
                        konst contractDescription = outerContractDescription ?: innerContractDescription
                        contractDescription?.let {
                            this.contractDescription = it
                        }
                        this.propertySymbol = propertySymbol

                        additionalPropertyAccessorInit()
                    }.also {
                        it.initContainingClassAttr()
                        bindFunctionTarget(accessorTarget, it)
                        this@RawFirBuilder.context.firFunctionTargets.removeLast()
                    }
                }
                isGetter || property.isVar -> {
                    // Default getter for konst/var properties, and default setter for var properties.
                    konst propertySource =
                        this?.toFirSourceElement() ?: property.toKtPsiSourceElement(KtFakeSourceElementKind.DefaultAccessor)
                    FirDefaultPropertyAccessor
                        .createGetterOrSetter(
                            propertySource,
                            baseModuleData,
                            FirDeclarationOrigin.Source,
                            propertyTypeRefToUse,
                            accessorVisibility,
                            propertySymbol,
                            isGetter,
                            parameterAnnotations = parameterAnnotationsFromProperty
                        )
                        .also {
                            if (this != null) {
                                it.extractAnnotationsFrom(this)
                            }
                            it.replaceAnnotations(it.annotations.smartPlus(accessorAnnotationsFromProperty))
                            it.status = status
                            it.initContainingClassAttr()
                        }
                }
                else -> {
                    // No default setter for konst properties.
                    null
                }
            }
        }

        private fun obtainPropertyComponentStatus(
            componentVisibility: Visibility,
            declaration: KtDeclaration?,
            property: KtProperty,
        ): FirDeclarationStatus {
            return FirDeclarationStatusImpl(componentVisibility, declaration?.modality).apply {
                isInline = property.hasModifier(INLINE_KEYWORD) ||
                        declaration?.hasModifier(INLINE_KEYWORD) == true
                isExternal = property.hasModifier(EXTERNAL_KEYWORD) ||
                        declaration?.hasModifier(EXTERNAL_KEYWORD) == true
                isLateInit = declaration?.hasModifier(LATEINIT_KEYWORD) == true
            }
        }

        private fun KtBackingField?.toFirBackingField(
            property: KtProperty,
            propertySymbol: FirPropertySymbol,
            propertyReturnType: FirTypeRef,
            annotationsFromProperty: List<FirAnnotationCall>,
        ): FirBackingField {
            konst componentVisibility = if (this?.visibility != null && this.visibility != Visibilities.Unknown) {
                this.visibility
            } else {
                Visibilities.Private
            }
            konst status = obtainPropertyComponentStatus(componentVisibility, this, property)
            konst backingFieldInitializer = this?.toInitializerExpression()
            konst returnType = this?.returnTypeReference.toFirOrImplicitType()
            konst source = this?.toFirSourceElement()
            return if (this != null) {
                buildBackingField {
                    this.source = source
                    moduleData = baseModuleData
                    origin = FirDeclarationOrigin.Source
                    returnTypeRef = returnType
                    this.status = status
                    extractAnnotationsTo(this)
                    this.annotations += annotationsFromProperty
                    name = BACKING_FIELD
                    symbol = FirBackingFieldSymbol(CallableId(name))
                    this.propertySymbol = propertySymbol
                    this.initializer = backingFieldInitializer
                    this.isVar = property.isVar
                    this.isVal = !property.isVar

                    additionalBackingFieldInit()
                }
            } else {
                FirDefaultPropertyBackingField(
                    moduleData = baseModuleData,
                    origin = FirDeclarationOrigin.Source,
                    source = property.toFirSourceElement(KtFakeSourceElementKind.DefaultAccessor),
                    annotations = annotationsFromProperty.toMutableList(),
                    returnTypeRef = propertyReturnType.copyWithNewSourceKind(KtFakeSourceElementKind.DefaultAccessor),
                    isVar = property.isVar,
                    propertySymbol = propertySymbol,
                    status = status,
                )
            }
        }

        private fun KtParameter.toFirValueParameter(
            defaultTypeRef: FirTypeRef?,
            functionSymbol: FirFunctionSymbol<*>,
            konstueParameterDeclaration: ValueParameterDeclaration,
            additionalAnnotations: List<FirAnnotation>,
        ): FirValueParameter {
            konst name = convertValueParameterName(nameAsSafeName, konstueParameterDeclaration) { nameIdentifier?.node?.text }
            return buildValueParameter {
                source = toFirSourceElement()
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Source
                returnTypeRef = when {
                    typeReference != null -> typeReference.toFirOrErrorType()
                    defaultTypeRef != null -> defaultTypeRef
                    konstueParameterDeclaration.shouldExplicitParameterTypeBePresent -> createNoTypeForParameterTypeRef()
                    else -> null.toFirOrImplicitType()
                }
                this.name = name
                symbol = FirValueParameterSymbol(name)
                defaultValue = if (hasDefaultValue()) {
                    buildOrLazyExpression(null, { { this@toFirValueParameter.defaultValue }.toFirExpression("Should have default konstue") })
                } else null
                isCrossinline = hasModifier(CROSSINLINE_KEYWORD)
                isNoinline = hasModifier(NOINLINE_KEYWORD)
                isVararg = isVarArg
                containingFunctionSymbol = functionSymbol
                addAnnotationsFrom(
                    this@toFirValueParameter,
                    isFromPrimaryConstructor = konstueParameterDeclaration == ValueParameterDeclaration.PRIMARY_CONSTRUCTOR
                )
                annotations += additionalAnnotations
            }
        }

        private fun FirValueParameterBuilder.addAnnotationsFrom(ktParameter: KtParameter, isFromPrimaryConstructor: Boolean) {
            for (annotationEntry in ktParameter.annotationEntries) {
                annotationEntry.convert<FirAnnotation>().takeIf {
                    !isFromPrimaryConstructor || it.useSiteTarget.appliesToPrimaryConstructorParameter()
                }?.let {
                    annotations += it
                }
            }
        }

        private fun KtParameter.toFirProperty(firParameter: FirValueParameter): FirProperty {
            require(hasValOrVar())
            konst type = typeReference.convertSafe<FirTypeRef>() ?: createNoTypeForParameterTypeRef()
            konst status = FirDeclarationStatusImpl(visibility, modality).apply {
                isExpect = hasExpectModifier() || this@RawFirBuilder.context.containerIsExpect
                isActual = hasActualModifier()
                isOverride = hasModifier(OVERRIDE_KEYWORD)
                isConst = hasModifier(CONST_KEYWORD)
            }
            konst propertySource = toFirSourceElement(KtFakeSourceElementKind.PropertyFromParameter)
            konst propertyName = nameAsSafeName
            konst parameterAnnotations = mutableListOf<FirAnnotationCall>()
            for (annotationEntry in annotationEntries) {
                parameterAnnotations += annotationEntry.convert<FirAnnotationCall>()
            }
            return buildProperty {
                source = propertySource
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Source
                returnTypeRef = type.copyWithNewSourceKind(KtFakeSourceElementKind.PropertyFromParameter)
                name = propertyName
                initializer = buildPropertyAccessExpression {
                    source = propertySource
                    calleeReference = buildPropertyFromParameterResolvedNamedReference {
                        source = propertySource
                        name = propertyName
                        resolvedSymbol = firParameter.symbol
                    }
                }
                isVar = isMutable
                symbol = FirPropertySymbol(callableIdForName(propertyName))
                isLocal = false
                konst defaultAccessorSource = propertySource.fakeElement(KtFakeSourceElementKind.DefaultAccessor)
                backingField = FirDefaultPropertyBackingField(
                    moduleData = baseModuleData,
                    origin = FirDeclarationOrigin.Source,
                    source = defaultAccessorSource,
                    annotations = parameterAnnotations.filter {
                        it.useSiteTarget == FIELD || it.useSiteTarget == PROPERTY_DELEGATE_FIELD
                    }.toMutableList(),
                    returnTypeRef = returnTypeRef.copyWithNewSourceKind(KtFakeSourceElementKind.DefaultAccessor),
                    isVar = isVar,
                    propertySymbol = symbol,
                    status = status.copy(),
                )

                this.status = status
                getter = FirDefaultPropertyGetter(
                    defaultAccessorSource,
                    baseModuleData,
                    FirDeclarationOrigin.Source,
                    type.copyWithNewSourceKind(KtFakeSourceElementKind.DefaultAccessor),
                    visibility,
                    symbol,
                ).also { getter ->
                    getter.initContainingClassAttr()
                    getter.replaceAnnotations(parameterAnnotations.filterUseSiteTarget(PROPERTY_GETTER))
                }
                setter = if (isMutable) FirDefaultPropertySetter(
                    defaultAccessorSource,
                    baseModuleData,
                    FirDeclarationOrigin.Source,
                    type.copyWithNewSourceKind(KtFakeSourceElementKind.DefaultAccessor),
                    visibility,
                    symbol,
                    parameterAnnotations = parameterAnnotations.filterUseSiteTarget(SETTER_PARAMETER)
                ).also { setter ->
                    setter.initContainingClassAttr()
                    setter.replaceAnnotations(parameterAnnotations.filterUseSiteTarget(PROPERTY_SETTER))
                } else null
                annotations += parameterAnnotations.filter {
                    it.useSiteTarget == null || it.useSiteTarget == PROPERTY
                }

                dispatchReceiverType = currentDispatchReceiverType()
            }.apply {
                if (firParameter.isVararg) {
                    isFromVararg = true
                }
                firParameter.correspondingProperty = this
                fromPrimaryConstructor = true
            }
        }

        private fun FirDefaultPropertyAccessor.extractAnnotationsFrom(annotated: KtAnnotated) {
            annotated.extractAnnotationsTo(this)
        }

        private fun KtAnnotated.extractAnnotationsTo(container: FirAnnotationContainer) {
            if (annotationEntries.isEmpty()) return
            konst annotations = buildList {
                addAll(container.annotations)
                for (annotationEntry in annotationEntries) {
                    add(annotationEntry.convert())
                }
            }
            container.replaceAnnotations(annotations)
        }

        private fun KtAnnotated.extractAnnotationsTo(container: FirAnnotationContainerBuilder) {
            for (annotationEntry in annotationEntries) {
                container.annotations += annotationEntry.convert<FirAnnotation>()
            }
        }

        private fun KtTypeParameterListOwner.extractTypeParametersTo(
            container: FirTypeParameterRefsOwnerBuilder,
            declarationSymbol: FirBasedSymbol<*>
        ) {
            for (typeParameter in typeParameters) {
                container.typeParameters += extractTypeParameter(typeParameter, declarationSymbol)
            }
        }

        private fun KtTypeParameterListOwner.extractTypeParametersTo(
            container: FirTypeParametersOwnerBuilder,
            declarationSymbol: FirBasedSymbol<*>
        ) {
            for (typeParameter in typeParameters) {
                container.typeParameters += extractTypeParameter(typeParameter, declarationSymbol)
            }
        }

        private fun extractTypeParameter(parameter: KtTypeParameter, declarationSymbol: FirBasedSymbol<*>): FirTypeParameter {
            konst parameterName = parameter.nameAsSafeName
            return buildTypeParameter {
                source = parameter.toFirSourceElement()
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Source
                name = parameterName
                symbol = FirTypeParameterSymbol()
                containingDeclarationSymbol = declarationSymbol
                variance = parameter.variance
                isReified = parameter.hasModifier(REIFIED_KEYWORD)
                parameter.extractAnnotationsTo(this)
                konst extendsBound = parameter.extendsBound
                if (extendsBound != null) {
                    bounds += extendsBound.convert<FirTypeRef>()
                }
                konst owner = parameter.getStrictParentOfType<KtTypeParameterListOwner>() ?: return@buildTypeParameter
                for (typeConstraint in owner.typeConstraints) {
                    konst subjectName = typeConstraint.subjectTypeParameterName?.getReferencedNameAsName()

                    if (subjectName == parameterName) {
                        bounds += typeConstraint.boundTypeReference.toFirOrErrorType()
                    }

                    for (entry in typeConstraint.annotationEntries) {
                        annotations += buildErrorAnnotationCall {
                            source = entry.toFirSourceElement()
                            useSiteTarget = entry.useSiteTarget?.getAnnotationUseSiteTarget()
                            annotationTypeRef = entry.typeReference.toFirOrErrorType()
                            diagnostic = ConeSimpleDiagnostic(
                                "Type parameter annotations are not allowed inside where clauses", DiagnosticKind.AnnotationNotAllowed,
                            )
                            konst name = (annotationTypeRef as? FirUserTypeRef)?.qualifier?.last()?.name
                                ?: Name.special("<no-annotation-name>")
                            calleeReference = buildSimpleNamedReference {
                                source = (entry.typeReference?.typeElement as? KtUserType)?.referenceExpression?.toFirSourceElement()
                                this.name = name
                            }
                            entry.extractArgumentsTo(this)
                            typeArguments.appendTypeArguments(entry.typeArguments)
                        }
                    }
                }
                addDefaultBoundIfNecessary()
            }
        }

        override fun visitTypeParameter(parameter: KtTypeParameter, data: FirElement?): FirElement {
            throw AssertionError("KtTypeParameter should be process via extractTypeParameter")
        }

        private fun <T> KtTypeParameterListOwner.fillDanglingConstraintsTo(to: T) where T : FirDeclaration, T : FirTypeParameterRefsOwner {
            konst typeParamNames = typeParameters.mapNotNull { it.nameAsName }.toSet()
            konst result = typeConstraints.mapNotNull { constraint ->
                constraint.subjectTypeParameterName?.getReferencedNameAsName()?.let { name ->
                    if (!typeParamNames.contains(name)) {
                        DanglingTypeConstraint(name, constraint.subjectTypeParameterName!!.toFirSourceElement())
                    } else {
                        null
                    }
                }

            }
            if (result.isNotEmpty()) {
                to.danglingTypeConstraints = result
            }
        }

        private fun KtDeclarationWithBody.extractValueParametersTo(
            container: FirFunctionBuilder,
            functionSymbol: FirFunctionSymbol<*>,
            konstueParameterDeclaration: ValueParameterDeclaration,
            defaultTypeRef: FirTypeRef? = null,
            additionalAnnotations: List<FirAnnotation> = emptyList(),
        ) {
            for (konstueParameter in konstueParameters) {
                container.konstueParameters += convertValueParameter(
                    konstueParameter, functionSymbol, defaultTypeRef, konstueParameterDeclaration, additionalAnnotations = additionalAnnotations
                )
            }
        }

        private fun KtCallElement.extractArgumentsTo(container: FirCallBuilder) {
            konst argumentList = buildArgumentList {
                source = konstueArgumentList?.toFirSourceElement()
                for (argument in konstueArguments) {
                    konst argumentExpression =
                        buildOrLazyExpression((argument as? PsiElement)?.toFirSourceElement()) { argument.toFirExpression() }
                    arguments += when (argument) {
                        is KtLambdaArgument -> buildLambdaArgumentExpression {
                            source = argument.toFirSourceElement()
                            expression = argumentExpression
                        }
                        else -> argumentExpression
                    }
                }
            }
            container.argumentList = argumentList
        }

        protected fun buildFieldForSupertypeDelegate(entry: KtDelegatedSuperTypeEntry, type: FirTypeRef, fieldOrd: Int): FirField {
            konst delegateSource = entry.toFirSourceElement(KtFakeSourceElementKind.ClassDelegationField)

            konst delegateExpression = buildOrLazyExpression(delegateSource) {
                { entry.delegateExpression }
                    .toFirExpression("Should have delegate")
            }
            return buildField {
                source = delegateSource
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Synthetic
                name = NameUtils.delegateFieldName(fieldOrd)
                returnTypeRef = type
                symbol = FirFieldSymbol(CallableId(this@RawFirBuilder.context.currentClassId, name))
                isVar = false
                status = FirDeclarationStatusImpl(Visibilities.Private, Modality.FINAL)
                initializer = delegateExpression
                dispatchReceiverType = currentDispatchReceiverType()
            }
        }

        private fun KtClassOrObject.extractSuperTypeListEntriesTo(
            container: FirClassBuilder,
            delegatedSelfTypeRef: FirTypeRef?,
            delegatedEnumSuperTypeRef: FirTypeRef?,
            classKind: ClassKind,
            containerTypeParameters: List<FirTypeParameterRef>,
            containingClassIsExpectClass: Boolean
        ): Pair<FirTypeRef, Map<Int, FirFieldSymbol>?> {
            var superTypeCallEntry: KtSuperTypeCallEntry? = null
            konst allSuperTypeCallEntries = mutableListOf<Pair<KtSuperTypeCallEntry, FirTypeRef>>()
            var delegatedSuperTypeRef: FirTypeRef? = null
            konst delegateFieldsMap = mutableMapOf<Int, FirFieldSymbol>()
            superTypeListEntries.forEachIndexed { index, superTypeListEntry ->
                when (superTypeListEntry) {
                    is KtSuperTypeEntry -> {
                        container.superTypeRefs += superTypeListEntry.typeReference.toFirOrErrorType()
                    }
                    is KtSuperTypeCallEntry -> {
                        delegatedSuperTypeRef = superTypeListEntry.calleeExpression.typeReference.toFirOrErrorType()
                        container.superTypeRefs += delegatedSuperTypeRef!!
                        superTypeCallEntry = superTypeListEntry
                        allSuperTypeCallEntries.add(superTypeListEntry to delegatedSuperTypeRef!!)
                    }
                    is KtDelegatedSuperTypeEntry -> {
                        konst type = superTypeListEntry.typeReference.toFirOrErrorType()
                        container.superTypeRefs += type
                        konst delegateField = buildFieldForSupertypeDelegate(superTypeListEntry, type, delegateFieldsMap.size)
                        delegateFieldsMap[index] = delegateField.symbol
                    }
                }
            }

            when {
                this is KtClass && classKind == ClassKind.ENUM_CLASS && superTypeCallEntry == null -> {
                    /*
                     * kotlin.Enum constructor has (name: String, ordinal: Int) signature,
                     *   so we should generate non-trivial constructors for enum and it's entry
                     *   for correct resolve of super constructor call or just call kotlin.Any constructor
                     *   and convert it to right call at backend, because of it doesn't affects frontend work
                     */
                    delegatedSuperTypeRef = buildResolvedTypeRef {
                        type = ConeClassLikeTypeImpl(
                            implicitEnumType.type.lookupTag,
                            delegatedSelfTypeRef?.coneType?.let { arrayOf(it) } ?: emptyArray(),
                            isNullable = false,
                        )
                    }
                    container.superTypeRefs += delegatedSuperTypeRef!!
                }
                this is KtClass && classKind == ClassKind.ANNOTATION_CLASS -> {
                    container.superTypeRefs += implicitAnnotationType
                    delegatedSuperTypeRef = implicitAnyType
                }
            }

            konst defaultDelegatedSuperTypeRef =
                when {
                    classKind == ClassKind.ENUM_ENTRY && this is KtClass -> delegatedEnumSuperTypeRef ?: implicitAnyType
                    container.superTypeRefs.isEmpty() -> implicitAnyType
                    else -> FirImplicitTypeRefImplWithoutSource
                }


            if (container.superTypeRefs.isEmpty()) {
                container.superTypeRefs += implicitAnyType
                delegatedSuperTypeRef = implicitAnyType
            }

            // TODO: in case we have no primary constructor,
            // it may be not possible to determine delegated super type right here
            delegatedSuperTypeRef = delegatedSuperTypeRef ?: defaultDelegatedSuperTypeRef
            konst shouldGenerateImplicitPrimaryConstructor =
                !hasExplicitPrimaryConstructor() && !hasSecondaryConstructors() && !(containingClassIsExpectClass && classKind != ClassKind.ENUM_CLASS)

            if (primaryConstructor != null || (this !is KtClass || !this.isInterface()) && shouldGenerateImplicitPrimaryConstructor) {
                konst firPrimaryConstructor = primaryConstructor.toFirConstructor(
                    superTypeCallEntry,
                    delegatedSuperTypeRef,
                    delegatedSelfTypeRef ?: delegatedSuperTypeRef!!,
                    owner = this,
                    containerTypeParameters,
                    allSuperTypeCallEntries,
                    containingClassIsExpectClass,
                    copyConstructedTypeRefWithImplicitSource = true,
                )
                container.declarations += firPrimaryConstructor
            }
            delegateFieldsMap.konstues.mapTo(container.declarations) { it.fir }
            return delegatedSuperTypeRef!! to delegateFieldsMap.takeIf { it.isNotEmpty() }
        }

        /**
         * @param delegatedSuperTypeRef can be null if containingClassIsExpectClass is true
         */
        protected fun KtPrimaryConstructor?.toFirConstructor(
            superTypeCallEntry: KtSuperTypeCallEntry?,
            delegatedSuperTypeRef: FirTypeRef?,
            delegatedSelfTypeRef: FirTypeRef,
            owner: KtClassOrObject,
            ownerTypeParameters: List<FirTypeParameterRef>,
            allSuperTypeCallEntries: List<Pair<KtSuperTypeCallEntry, FirTypeRef>>,
            containingClassIsExpectClass: Boolean,
            copyConstructedTypeRefWithImplicitSource: Boolean,
        ): FirConstructor {
            konst constructorSource = this?.toFirSourceElement()
                ?: owner.toKtPsiSourceElement(KtFakeSourceElementKind.ImplicitConstructor)
            fun buildDelegatedCall(superTypeCallEntry: KtSuperTypeCallEntry?, delegatedTypeRef: FirTypeRef): FirDelegatedConstructorCall? {
                konst constructorCall = superTypeCallEntry?.toFirSourceElement()
                return if (containingClassIsExpectClass) null else {
                    konst constructedTypeRef = if (copyConstructedTypeRefWithImplicitSource) {
                        delegatedTypeRef.copyWithNewSourceKind(KtFakeSourceElementKind.ImplicitTypeRef)
                    } else {
                        delegatedTypeRef
                    }
                    buildOrLazyDelegatedConstructorCall(isThis = false, constructedTypeRef) {
                        buildDelegatedConstructorCall {
                            source = constructorCall ?: constructorSource.fakeElement(KtFakeSourceElementKind.DelegatingConstructorCall)
                            this.constructedTypeRef = constructedTypeRef
                            isThis = false
                            calleeReference = buildExplicitSuperReference {
                                source =
                                    superTypeCallEntry?.calleeExpression?.toFirSourceElement(KtFakeSourceElementKind.DelegatingConstructorCall)
                                        ?: this@buildDelegatedConstructorCall.source?.fakeElement(KtFakeSourceElementKind.DelegatingConstructorCall)
                                superTypeRef = this@buildDelegatedConstructorCall.constructedTypeRef
                            }
                            superTypeCallEntry?.extractArgumentsTo(this)
                        }
                    }
                }
            }
            konst firDelegatedCall = if (allSuperTypeCallEntries.size <= 1 ) {
                buildDelegatedCall(superTypeCallEntry, delegatedSuperTypeRef!!)
            } else {
                buildMultiDelegatedConstructorCall {
                    allSuperTypeCallEntries.mapTo(delegatedConstructorCalls) { (superTypeCallEntry, delegatedTypeRef) ->
                        buildDelegatedCall(superTypeCallEntry, delegatedTypeRef)!!
                    }
                }
            }

            // See DescriptorUtils#getDefaultConstructorVisibility in core.descriptors
            fun defaultVisibility() = when {
                owner is KtObjectDeclaration || owner.hasModifier(ENUM_KEYWORD) || owner is KtEnumEntry -> Visibilities.Private
                owner.hasModifier(SEALED_KEYWORD) -> Visibilities.Protected
                else -> Visibilities.Unknown
            }

            konst explicitVisibility = this?.visibility?.takeUnless { it == Visibilities.Unknown }
            konst status = FirDeclarationStatusImpl(explicitVisibility ?: defaultVisibility(), Modality.FINAL).apply {
                isExpect = this@toFirConstructor?.hasExpectModifier() == true || this@RawFirBuilder.context.containerIsExpect
                isActual = this@toFirConstructor?.hasActualModifier() == true
                isInner = owner.hasModifier(INNER_KEYWORD)
                isFromSealedClass = owner.hasModifier(SEALED_KEYWORD) && explicitVisibility !== Visibilities.Private
                isFromEnumClass = owner.hasModifier(ENUM_KEYWORD)
            }
            return buildPrimaryConstructor {
                source = constructorSource
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Source
                returnTypeRef = delegatedSelfTypeRef
                this.status = status
                dispatchReceiverType = owner.obtainDispatchReceiverForConstructor()
                symbol = FirConstructorSymbol(callableIdForClassConstructor())
                delegatedConstructor = firDelegatedCall
                typeParameters += constructorTypeParametersFromConstructedClass(ownerTypeParameters)
                this.contextReceivers.addAll(convertContextReceivers(owner.contextReceivers))
                this@toFirConstructor?.extractAnnotationsTo(this)
                this@toFirConstructor?.extractValueParametersTo(this, symbol, ValueParameterDeclaration.PRIMARY_CONSTRUCTOR)
                this.body = null
            }.apply {
                containingClassForStaticMemberAttr = currentDispatchReceiverType()!!.lookupTag
            }
        }

        private fun KtClassOrObject.obtainDispatchReceiverForConstructor(): ConeClassLikeType? =
            if (hasModifier(INNER_KEYWORD)) dispatchReceiverForInnerClassConstructor() else null

        override fun visitKtFile(file: KtFile, data: FirElement?): FirElement {
            context.packageFqName = when (mode) {
                BodyBuildingMode.NORMAL -> file.packageFqNameByTree
                BodyBuildingMode.LAZY_BODIES -> file.packageFqName
            }
            return buildFile {
                symbol = FirFileSymbol()
                source = file.toFirSourceElement()
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Source
                name = file.name
                sourceFile = KtPsiSourceFile(file)
                sourceFileLinesMapping = KtPsiSourceFileLinesMapping(file)
                packageDirective = buildPackageDirective {
                    packageFqName = context.packageFqName
                    source = file.packageDirective?.toKtPsiSourceElement()
                }
                annotationsContainer = buildFileAnnotationsContainer {
                    moduleData = baseModuleData
                    containingFileSymbol = this@buildFile.symbol
                    source = file.fileAnnotationList?.toKtPsiSourceElement()
                    for (annotationEntry in file.annotationEntries) {
                        annotations += annotationEntry.convert<FirAnnotation>()
                    }

                    annotations.ifEmpty {
                        resolvePhase = FirResolvePhase.BODY_RESOLVE
                    }
                }

                for (importDirective in file.importDirectives) {
                    imports += buildImport {
                        source = importDirective.toFirSourceElement()
                        importedFqName = importDirective.importedFqName
                        isAllUnder = importDirective.isAllUnder
                        aliasName = importDirective.aliasName?.let { Name.identifier(it) }
                        aliasSource = importDirective.alias?.nameIdentifier?.toFirSourceElement()
                    }
                }
                for (declaration in file.declarations) {
                    declarations += when (declaration) {
                        is KtScript -> {
                            require(file.declarations.size == 1) { "Expect the script to be the only declaration in the file $name" }
                            convertScript(declaration, this.name) {
                                for (configurator in baseSession.extensionService.scriptConfigurators) {
                                    with(configurator) { configure(this@buildFile) }
                                }
                            }
                        }
                        is KtDestructuringDeclaration -> buildErrorTopLevelDestructuringDeclaration(declaration.toFirSourceElement())
                        else -> declaration.convert()
                    }
                }

                for (danglingModifierList in file.danglingModifierLists) {
                    declarations += buildErrorTopLevelDeclarationForDanglingModifierList(danglingModifierList)
                }
            }
        }

        private fun convertScript(script: KtScript, fileName: String, setup: FirScriptBuilder.() -> Unit = {}): FirScript {
            return buildScript {
                source = script.toFirSourceElement()
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Source
                name = Name.special("<script-$fileName>")
                symbol = FirScriptSymbol(context.packageFqName.child(name))
                for (declaration in script.declarations) {
                    when (declaration) {
                        is KtScriptInitializer -> {
                            declaration.body?.let { statements.add(it.toFirStatement()) }
                        }
                        else -> {
                            statements.add(declaration.toFirStatement())
                        }
                    }
                }
                setup()
            }
        }

        // TODO: if no original FirScript is passed here, the inkonstid script could be constructed, consider throwing an error
        override fun visitScript(script: KtScript, data: FirElement?): FirElement {
            konst ktFile = script.containingKtFile
            konst fileName = ktFile.name
            return convertScript(script, fileName) {
                (data as? FirScript)?.let {
                    contextReceivers.addAll(it.contextReceivers)
                    parameters.addAll(it.parameters)
                }
            }
        }

        protected fun KtEnumEntry.toFirEnumEntry(
            delegatedEnumSelfTypeRef: FirResolvedTypeRef,
            ownerClassHasDefaultConstructor: Boolean
        ): FirDeclaration {
            konst ktEnumEntry = this@toFirEnumEntry
            konst containingClassIsExpectClass = hasExpectModifier() || this@RawFirBuilder.context.containerIsExpect
            return buildEnumEntry {
                source = toFirSourceElement()
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Source
                returnTypeRef = delegatedEnumSelfTypeRef
                name = nameAsSafeName
                status = FirDeclarationStatusImpl(Visibilities.Public, Modality.FINAL).apply {
                    isStatic = true
                    isExpect = containingClassIsExpectClass
                }
                symbol = FirEnumEntrySymbol(callableIdForName(nameAsSafeName))
                if (ownerClassHasDefaultConstructor && ktEnumEntry.initializerList == null &&
                    ktEnumEntry.annotationEntries.isEmpty() && ktEnumEntry.body == null
                ) {
                    return@buildEnumEntry
                }
                extractAnnotationsTo(this)
                initializer = buildOrLazyExpression(toFirSourceElement(KtFakeSourceElementKind.EnumInitializer)) {
                    withChildClassName(nameAsSafeName, isExpect = false) {
                        buildAnonymousObjectExpression {
                            konst enumEntrySource = toFirSourceElement(KtFakeSourceElementKind.EnumInitializer)
                            source = enumEntrySource
                            anonymousObject = buildAnonymousObject {
                                source = enumEntrySource
                                moduleData = baseModuleData
                                origin = FirDeclarationOrigin.Source
                                classKind = ClassKind.ENUM_ENTRY
                                scopeProvider = this@RawFirBuilder.baseScopeProvider
                                symbol = FirAnonymousObjectSymbol()
                                status = FirDeclarationStatusImpl(Visibilities.Local, Modality.FINAL)

                                konst delegatedEntrySelfType = buildResolvedTypeRef {
                                    type =
                                        ConeClassLikeTypeImpl(
                                            this@buildAnonymousObject.symbol.toLookupTag(),
                                            emptyArray(),
                                            isNullable = false
                                        )
                                }
                                registerSelfType(delegatedEntrySelfType)

                                superTypeRefs += delegatedEnumSelfTypeRef
                                konst superTypeCallEntry = superTypeListEntries.firstIsInstanceOrNull<KtSuperTypeCallEntry>()
                                konst correctedEnumSelfTypeRef = buildResolvedTypeRef {
                                    source = superTypeCallEntry?.calleeExpression?.typeReference?.toFirSourceElement()
                                    type = delegatedEnumSelfTypeRef.type
                                }
                                declarations += primaryConstructor.toFirConstructor(
                                    superTypeCallEntry,
                                    correctedEnumSelfTypeRef,
                                    delegatedEntrySelfType,
                                    owner = ktEnumEntry,
                                    typeParameters,
                                    allSuperTypeCallEntries = emptyList(),
                                    containingClassIsExpectClass,
                                    copyConstructedTypeRefWithImplicitSource = true,
                                )
                                // Use ANONYMOUS_OBJECT_NAME for the owner class id for enum entry declarations (see KT-42351)
                                withChildClassName(SpecialNames.ANONYMOUS, forceLocalContext = true, isExpect = false) {
                                    for (declaration in ktEnumEntry.declarations) {
                                        declarations += declaration.toFirDeclaration(
                                            correctedEnumSelfTypeRef,
                                            delegatedSelfType = delegatedEntrySelfType,
                                            ktEnumEntry,
                                            ownerClassBuilder = this,
                                            ownerTypeParameters = emptyList()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }.apply {
                containingClassForStaticMemberAttr = currentDispatchReceiverType()!!.lookupTag
            }
        }

        private fun convertContextReceivers(receivers: List<KtContextReceiver>): List<FirContextReceiver> {
            return receivers.map { contextReceiverElement ->
                buildContextReceiver {
                    this.source = contextReceiverElement.toFirSourceElement()
                    this.customLabelName = contextReceiverElement.labelNameAsName()
                    this.labelNameFromTypeRef = contextReceiverElement.typeReference()?.nameForReceiverLabel()?.let(Name::identifier)

                    contextReceiverElement.typeReference().convertSafe<FirTypeRef>()?.let {
                        this.typeRef = it
                    }
                }
            }
        }

        override fun visitClassOrObject(classOrObject: KtClassOrObject, data: FirElement?): FirElement {
            // NB: enum entry nested classes are considered local by FIR design (see discussion in KT-45115)
            konst isLocal = classOrObject.isLocal || classOrObject.getStrictParentOfType<KtEnumEntry>() != null
            konst classIsExpect = classOrObject.hasExpectModifier() || context.containerIsExpect
            return withChildClassName(
                classOrObject.nameAsSafeName,
                isExpect = classIsExpect,
                forceLocalContext = isLocal
            ) {
                konst classKind = when (classOrObject) {
                    is KtObjectDeclaration -> ClassKind.OBJECT
                    is KtClass -> when {
                        classOrObject.isInterface() -> ClassKind.INTERFACE
                        classOrObject.isEnum() -> ClassKind.ENUM_CLASS
                        classOrObject.isAnnotation() -> ClassKind.ANNOTATION_CLASS
                        else -> ClassKind.CLASS
                    }
                    else -> throw AssertionError("Unexpected class or object: ${classOrObject.text}")
                }
                konst status = FirDeclarationStatusImpl(
                    if (isLocal) Visibilities.Local else classOrObject.visibility,
                    classOrObject.modality,
                ).apply {
                    isExpect = classIsExpect
                    isActual = classOrObject.hasActualModifier()
                    isInner = classOrObject.hasModifier(INNER_KEYWORD)
                    isCompanion = (classOrObject as? KtObjectDeclaration)?.isCompanion() == true
                    isData = classOrObject.hasModifier(DATA_KEYWORD)
                    isInline = classOrObject.hasModifier(INLINE_KEYWORD) || classOrObject.hasModifier(VALUE_KEYWORD)
                    isFun = classOrObject.hasModifier(FUN_KEYWORD)
                    isExternal = classOrObject.hasModifier(EXTERNAL_KEYWORD)
                }

                withCapturedTypeParameters(status.isInner || isLocal, classOrObject.toFirSourceElement(), listOf()) {
                    var delegatedFieldsMap: Map<Int, FirFieldSymbol>?
                    buildRegularClass {
                        source = classOrObject.toFirSourceElement()
                        moduleData = baseModuleData
                        origin = FirDeclarationOrigin.Source
                        name = classOrObject.nameAsSafeName
                        this.status = status
                        this.classKind = classKind
                        scopeProvider = baseScopeProvider
                        symbol = FirRegularClassSymbol(context.currentClassId)

                        classOrObject.extractAnnotationsTo(this)
                        classOrObject.extractTypeParametersTo(this, symbol)

                        context.appendOuterTypeParameters(ignoreLastLevel = true, typeParameters)
                        context.pushFirTypeParameters(
                            status.isInner || isLocal,
                            typeParameters.subList(0, classOrObject.typeParameters.size)
                        )

                        konst delegatedSelfType = classOrObject.toDelegatedSelfType(this)
                        registerSelfType(delegatedSelfType)

                        konst (delegatedSuperType, extractedDelegatedFieldsMap) = classOrObject.extractSuperTypeListEntriesTo(
                            this,
                            delegatedSelfType,
                            null,
                            classKind,
                            typeParameters,
                            containingClassIsExpectClass = classIsExpect
                        )
                        delegatedFieldsMap = extractedDelegatedFieldsMap

                        konst primaryConstructor = classOrObject.primaryConstructor
                        konst firPrimaryConstructor = declarations.firstOrNull { it is FirConstructor } as? FirConstructor
                        if (primaryConstructor != null && firPrimaryConstructor != null) {
                            primaryConstructor.konstueParameters.zip(
                                firPrimaryConstructor.konstueParameters
                            ).forEach { (ktParameter, firParameter) ->
                                if (ktParameter.hasValOrVar()) {
                                    addDeclaration(ktParameter.toFirProperty(firParameter))
                                }
                            }
                        }

                        for (declaration in classOrObject.declarations) {
                            addDeclaration(
                                declaration.toFirDeclaration(
                                    delegatedSuperType,
                                    delegatedSelfType,
                                    classOrObject,
                                    this,
                                    typeParameters
                                )
                            )
                        }
                        for (danglingModifier in classOrObject.body?.danglingModifierLists ?: emptyList()) {
                            addDeclaration(
                                buildErrorTopLevelDeclarationForDanglingModifierList(danglingModifier).apply {
                                    containingClassAttr = currentDispatchReceiverType()?.lookupTag
                                }
                            )
                        }

                        if (classOrObject.hasModifier(DATA_KEYWORD) && firPrimaryConstructor != null) {
                            konst zippedParameters =
                                classOrObject.primaryConstructorParameters.filter { it.hasValOrVar() } zip declarations.filterIsInstance<FirProperty>()
                            DataClassMembersGenerator(
                                classOrObject,
                                this,
                                zippedParameters,
                                context.packageFqName,
                                context.className,
                                createClassTypeRefWithSourceKind = { firPrimaryConstructor.returnTypeRef.copyWithNewSourceKind(it) },
                                createParameterTypeRefWithSourceKind = { property, newKind ->
                                    // just making a shallow copy isn't enough type ref may be a function type ref
                                    // and contain konstue parameters inside
                                    withDefaultSourceElementKind(newKind) {
                                        (property.returnTypeRef.psi as KtTypeReference?).toFirOrImplicitType()
                                    }
                                },
                                addValueParameterAnnotations = { addAnnotationsFrom(it as KtParameter, isFromPrimaryConstructor = true) },
                            ).generate()
                        }

                        if (classOrObject.hasModifier(ENUM_KEYWORD)) {
                            generateValuesFunction(
                                baseModuleData,
                                context.packageFqName,
                                context.className,
                                classIsExpect
                            )
                            generateValueOfFunction(
                                baseModuleData, context.packageFqName, context.className,
                                classIsExpect
                            )
                            generateEntriesGetter(
                                baseModuleData, context.packageFqName, context.className,
                                classIsExpect
                            )
                        }

                        initCompanionObjectSymbolAttr()

                        context.popFirTypeParameters()
                        contextReceivers.addAll(convertContextReceivers(classOrObject.contextReceivers))
                    }.also {
                        it.delegateFieldsMap = delegatedFieldsMap
                    }
                }
            }.also {
                if (classOrObject.parent is KtClassBody) {
                    it.initContainingClassForLocalAttr()
                }
                classOrObject.fillDanglingConstraintsTo(it)
            }
        }

        override fun visitObjectLiteralExpression(expression: KtObjectLiteralExpression, data: FirElement?): FirElement {
            return withChildClassName(SpecialNames.ANONYMOUS, forceLocalContext = true, isExpect = false) {
                var delegatedFieldsMap: Map<Int, FirFieldSymbol>?
                buildAnonymousObjectExpression {
                    source = expression.toFirSourceElement()
                    anonymousObject = buildAnonymousObject {
                        konst objectDeclaration = expression.objectDeclaration
                        source = objectDeclaration.toFirSourceElement()
                        moduleData = baseModuleData
                        origin = FirDeclarationOrigin.Source
                        classKind = ClassKind.OBJECT
                        scopeProvider = baseScopeProvider
                        symbol = FirAnonymousObjectSymbol()
                        status = FirDeclarationStatusImpl(Visibilities.Local, Modality.FINAL)
                        context.appendOuterTypeParameters(ignoreLastLevel = false, typeParameters)
                        konst delegatedSelfType = objectDeclaration.toDelegatedSelfType(this)
                        registerSelfType(delegatedSelfType)
                        objectDeclaration.extractAnnotationsTo(this)
                        konst (delegatedSuperType, extractedDelegatedFieldsMap) = objectDeclaration.extractSuperTypeListEntriesTo(
                            this,
                            delegatedSelfType,
                            null,
                            ClassKind.CLASS,
                            containerTypeParameters = emptyList(),
                            containingClassIsExpectClass = false
                        )
                        delegatedFieldsMap = extractedDelegatedFieldsMap

                        for (declaration in objectDeclaration.declarations) {
                            declarations += declaration.toFirDeclaration(
                                delegatedSuperType,
                                delegatedSelfType,
                                owner = objectDeclaration,
                                ownerClassBuilder = this,
                                ownerTypeParameters = emptyList()
                            )
                        }

                        for (danglingModifier in objectDeclaration.body?.danglingModifierLists ?: emptyList()) {
                            declarations += buildErrorTopLevelDeclarationForDanglingModifierList(danglingModifier).apply {
                                containingClassAttr = currentDispatchReceiverType()?.lookupTag
                            }
                        }
                    }.also {
                        it.delegateFieldsMap = delegatedFieldsMap
                    }
                }
            }
        }

        override fun visitTypeAlias(typeAlias: KtTypeAlias, data: FirElement?): FirElement {
            konst typeAliasIsExpect = typeAlias.hasExpectModifier() || context.containerIsExpect
            return withChildClassName(typeAlias.nameAsSafeName, isExpect = typeAliasIsExpect) {
                buildTypeAlias {
                    source = typeAlias.toFirSourceElement()
                    moduleData = baseModuleData
                    origin = FirDeclarationOrigin.Source
                    name = typeAlias.nameAsSafeName
                    status = FirDeclarationStatusImpl(typeAlias.visibility, Modality.FINAL).apply {
                        isExpect = typeAliasIsExpect
                        isActual = typeAlias.hasActualModifier()
                    }
                    symbol = FirTypeAliasSymbol(context.currentClassId)
                    expandedTypeRef = typeAlias.getTypeReference().toFirOrErrorType()
                    typeAlias.extractAnnotationsTo(this)
                    typeAlias.extractTypeParametersTo(this, symbol)
                }
            }
        }

        override fun visitNamedFunction(function: KtNamedFunction, data: FirElement?): FirElement {
            konst typeReference = function.typeReference
            konst returnType = if (function.hasBlockBody()) {
                typeReference.toFirOrUnitType()
            } else {
                typeReference.toFirOrImplicitType()
            }
            konst receiverType = function.receiverTypeReference.convertSafe<FirTypeRef>()

            konst labelName: String?
            konst isAnonymousFunction = function.isAnonymous
            konst isLocalFunction = function.isLocal
            konst functionSymbol: FirFunctionSymbol<*>
            konst functionBuilder = if (isAnonymousFunction) {
                FirAnonymousFunctionBuilder().apply {
                    receiverParameter = receiverType?.convertToReceiverParameter()
                    symbol = FirAnonymousFunctionSymbol().also { functionSymbol = it }
                    isLambda = false
                    hasExplicitParameterList = true
                    label = context.getLastLabel(function)
                    labelName = label?.name ?: context.calleeNamesForLambda.lastOrNull()?.identifier
                    if (function.hasModifier(SUSPEND_KEYWORD)) {
                        status = FirResolvedDeclarationStatusImpl.DEFAULT_STATUS_FOR_SUSPEND_FUNCTION_EXPRESSION
                    }
                }
            } else {
                FirSimpleFunctionBuilder().apply {
                    receiverParameter = receiverType?.convertToReceiverParameter()
                    name = function.nameAsSafeName
                    labelName = context.getLastLabel(function)?.name ?: runIf(!name.isSpecial) { name.identifier }
                    symbol = FirNamedFunctionSymbol(callableIdForName(function.nameAsSafeName)).also { functionSymbol = it }
                    dispatchReceiverType = currentDispatchReceiverType()
                    status = FirDeclarationStatusImpl(
                        if (isLocalFunction) Visibilities.Local else function.visibility,
                        function.modality,
                    ).apply {
                        isExpect = function.hasExpectModifier() || context.containerIsExpect
                        isActual = function.hasActualModifier()
                        isOverride = function.hasModifier(OVERRIDE_KEYWORD)
                        isOperator = function.hasModifier(OPERATOR_KEYWORD)
                        isInfix = function.hasModifier(INFIX_KEYWORD)
                        isInline = function.hasModifier(INLINE_KEYWORD)
                        isTailRec = function.hasModifier(TAILREC_KEYWORD)
                        isExternal = function.hasModifier(EXTERNAL_KEYWORD)
                        isSuspend = function.hasModifier(SUSPEND_KEYWORD)
                    }

                    contextReceivers.addAll(convertContextReceivers(function.contextReceivers))
                }
            }

            konst target = FirFunctionTarget(labelName, isLambda = false)
            konst functionSource = function.toFirSourceElement()
            konst firFunction = functionBuilder.apply {
                source = functionSource
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Source
                returnTypeRef = returnType

                context.firFunctionTargets += target
                function.extractAnnotationsTo(this)
                if (this is FirSimpleFunctionBuilder) {
                    function.extractTypeParametersTo(this, symbol)
                }
                for (konstueParameter in function.konstueParameters) {
                    konstueParameters += convertValueParameter(
                        konstueParameter,
                        functionSymbol,
                        null,
                        if (isAnonymousFunction) ValueParameterDeclaration.LAMBDA else ValueParameterDeclaration.FUNCTION
                    )
                }
                konst actualTypeParameters = if (this is FirSimpleFunctionBuilder)
                    this.typeParameters
                else
                    listOf()
                withCapturedTypeParameters(true, functionSource, actualTypeParameters) {
                    konst outerContractDescription = function.obtainContractDescription()
                    konst (body, innerContractDescription) = function.buildFirBody()
                    this.body = body
                    konst contractDescription = outerContractDescription ?: innerContractDescription
                    contractDescription?.let {
                        if (this is FirSimpleFunctionBuilder) {
                            this.contractDescription = it
                        } else if (this is FirAnonymousFunctionBuilder) {
                            this.contractDescription = it
                        }
                    }
                }
                context.firFunctionTargets.removeLast()
                additionalFunctionInit()
            }.build().also {
                bindFunctionTarget(target, it)
                if (it is FirSimpleFunction) {
                    function.fillDanglingConstraintsTo(it)
                }
            }
            return if (firFunction is FirAnonymousFunction) {
                buildAnonymousFunctionExpression {
                    source = functionSource
                    anonymousFunction = firFunction
                }
            } else {
                firFunction
            }
        }

        private fun KtDeclarationWithBody.obtainContractDescription(): FirContractDescription? {
            return when (konst description = contractDescription) {
                null -> null
                else -> buildRawContractDescription {
                    source = description.toFirSourceElement()
                    description.extractRawEffects(rawEffects)
                }
            }
        }

        private fun KtContractEffectList.extractRawEffects(destination: MutableList<FirExpression>) {
            getContractEffects().mapTo(destination) { effect ->
                buildOrLazyExpression(effect.toFirSourceElement()) {
                    effect.getExpression().accept(this@Visitor, null) as FirExpression
                }
            }
        }

        override fun visitLambdaExpression(expression: KtLambdaExpression, data: FirElement?): FirElement {
            konst literal = expression.functionLiteral
            konst literalSource = literal.toFirSourceElement()
            konst implicitTypeRefSource = literal.toFirSourceElement(KtFakeSourceElementKind.ImplicitTypeRef)
            konst returnType = buildImplicitTypeRef {
                source = implicitTypeRefSource
            }
            konst receiverType = buildImplicitTypeRef {
                source = implicitTypeRefSource
            }

            konst target: FirFunctionTarget
            konst anonymousFunction = buildAnonymousFunction {
                source = literalSource
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Source
                returnTypeRef = returnType
                receiverParameter = receiverType.asReceiverParameter()
                symbol = FirAnonymousFunctionSymbol()
                isLambda = true
                hasExplicitParameterList = expression.functionLiteral.arrow != null

                konst destructuringStatements = mutableListOf<FirStatement>()
                for (konstueParameter in literal.konstueParameters) {
                    konst multiDeclaration = konstueParameter.destructuringDeclaration
                    konstueParameters += if (multiDeclaration != null) {
                        konst name = SpecialNames.DESTRUCT
                        konst multiParameter = buildValueParameter {
                            source = konstueParameter.toFirSourceElement()
                            containingFunctionSymbol = this@buildAnonymousFunction.symbol
                            moduleData = baseModuleData
                            origin = FirDeclarationOrigin.Source
                            returnTypeRef = konstueParameter.typeReference?.convertSafe() ?: buildImplicitTypeRef {
                                source = multiDeclaration.toFirSourceElement(KtFakeSourceElementKind.ImplicitTypeRef)
                            }
                            this.name = name
                            symbol = FirValueParameterSymbol(name)
                            isCrossinline = false
                            isNoinline = false
                            isVararg = false
                        }
                        destructuringStatements += generateDestructuringBlock(
                            baseModuleData,
                            multiDeclaration,
                            multiParameter,
                            tmpVariable = false,
                            extractAnnotationsTo = { extractAnnotationsTo(it) },
                        ) { toFirOrImplicitType() }.statements
                        multiParameter
                    } else {
                        konst typeRef = konstueParameter.typeReference?.convertSafe() ?: buildImplicitTypeRef {
                            source = konstueParameter.toFirSourceElement()
                                .fakeElement(KtFakeSourceElementKind.ImplicitReturnTypeOfLambdaValueParameter)
                        }
                        convertValueParameter(konstueParameter, symbol, typeRef, ValueParameterDeclaration.LAMBDA)
                    }
                }
                konst expressionSource = expression.toFirSourceElement()
                label = context.getLastLabel(expression) ?: context.calleeNamesForLambda.lastOrNull()?.let {
                    buildLabel {
                        source = expressionSource.fakeElement(KtFakeSourceElementKind.GeneratedLambdaLabel)
                        name = it.asString()
                    }
                }
                target = FirFunctionTarget(label?.name, isLambda = true).also {
                    context.firFunctionTargets += it
                }
                konst ktBody = literal.bodyExpression
                body = if (ktBody == null) {
                    konst errorExpression = buildErrorExpression(source, ConeSyntaxDiagnostic("Lambda has no body"))
                    FirSingleExpressionBlock(errorExpression.toReturn())
                } else {
                    configureBlockWithoutBuilding(ktBody).apply {
                        statements.firstOrNull()?.let {
                            if (it.isContractBlockFirCheck()) {
                                this@buildAnonymousFunction.contractDescription = it.toLegacyRawContractDescription()
                                statements[0] = FirContractCallBlock(it)
                            }
                        }

                        if (statements.isEmpty()) {
                            statements.add(
                                buildReturnExpression {
                                    source = expressionSource.fakeElement(KtFakeSourceElementKind.ImplicitReturn.FromExpressionBody)
                                    this.target = target
                                    result = buildUnitExpression {
                                        source = expressionSource.fakeElement(KtFakeSourceElementKind.ImplicitUnit)
                                    }
                                }
                            )
                        }
                        statements.addAll(0, destructuringStatements)
                    }.build()
                }
                context.firFunctionTargets.removeLast()
            }.also {
                bindFunctionTarget(target, it)
            }
            return buildAnonymousFunctionExpression {
                source = expression.toKtPsiSourceElement()
                this.anonymousFunction = anonymousFunction
            }
        }

        protected fun KtSecondaryConstructor.toFirConstructor(
            delegatedTypeRef: FirTypeRef,
            selfTypeRef: FirTypeRef,
            owner: KtClassOrObject,
            ownerTypeParameters: List<FirTypeParameterRef>
        ): FirConstructor {
            konst target = FirFunctionTarget(labelName = null, isLambda = false)
            return buildConstructor {
                source = this@toFirConstructor.toFirSourceElement()
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Source
                returnTypeRef = selfTypeRef
                konst explicitVisibility = visibility
                status = FirDeclarationStatusImpl(explicitVisibility, Modality.FINAL).apply {
                    isExpect = hasExpectModifier() || this@RawFirBuilder.context.containerIsExpect
                    isActual = hasActualModifier()
                    isInner = owner.hasModifier(INNER_KEYWORD)
                    isFromSealedClass = owner.hasModifier(SEALED_KEYWORD) && explicitVisibility !== Visibilities.Private
                    isFromEnumClass = owner.hasModifier(ENUM_KEYWORD)
                }
                dispatchReceiverType = owner.obtainDispatchReceiverForConstructor()
                contextReceivers.addAll(convertContextReceivers(owner.contextReceivers))
                symbol = FirConstructorSymbol(callableIdForClassConstructor())
                delegatedConstructor = buildOrLazyDelegatedConstructorCall(
                    isThis = isDelegatedCallToThis(),
                    constructedTypeRef = delegatedTypeRef,
                ) {
                    getDelegationCall().convert(delegatedTypeRef)
                }
                this@RawFirBuilder.context.firFunctionTargets += target
                extractAnnotationsTo(this)
                typeParameters += constructorTypeParametersFromConstructedClass(ownerTypeParameters)
                extractValueParametersTo(this, symbol, ValueParameterDeclaration.FUNCTION)


                konst (body, contractDescription) = buildFirBody()
                contractDescription?.let { this.contractDescription = it }
                this.body = body
                this@RawFirBuilder.context.firFunctionTargets.removeLast()
            }.also {
                it.containingClassForStaticMemberAttr = currentDispatchReceiverType()!!.lookupTag
                bindFunctionTarget(target, it)
            }
        }

        private fun KtConstructorDelegationCall.convert(
            delegatedType: FirTypeRef,
        ): FirDelegatedConstructorCall {
            konst isThis = isCallToThis //|| (isImplicit && hasPrimaryConstructor)
            konst source = if (isImplicit) {
                this.toFirSourceElement(KtFakeSourceElementKind.ImplicitConstructor)
            } else {
                this.toFirSourceElement()
            }
            return buildDelegatedConstructorCall {
                this.source = source
                constructedTypeRef = delegatedType.copyWithNewSourceKind(KtFakeSourceElementKind.ImplicitTypeRef)
                this.isThis = isThis
                konst calleeKind =
                    if (isImplicit) KtFakeSourceElementKind.ImplicitConstructor else KtFakeSourceElementKind.DelegatingConstructorCall
                konst calleeSource = this@convert.calleeExpression?.toFirSourceElement(calleeKind)
                    ?: source.fakeElement(calleeKind)
                this.calleeReference = if (isThis) {
                    buildExplicitThisReference {
                        this.source = calleeSource
                    }
                } else {
                    buildExplicitSuperReference {
                        this.source = calleeSource
                        this.superTypeRef = this@buildDelegatedConstructorCall.constructedTypeRef
                    }
                }
                extractArgumentsTo(this)
            }
        }

        private fun KtDeclarationWithInitializer.toInitializerExpression() =
            runIf(hasInitializer()) {
                this@RawFirBuilder.context.calleeNamesForLambda += null

                konst expression = buildOrLazyExpression(null) {
                    initializer.toFirExpression("Should have initializer")
                }

                this@RawFirBuilder.context.calleeNamesForLambda.removeLast()
                expression
            }

        private fun <T> KtProperty.toFirProperty(
            ownerRegularOrAnonymousObjectSymbol: FirClassSymbol<*>?,
            context: Context<T>
        ): FirProperty {
            konst propertyType = typeReference.toFirOrImplicitType()
            konst propertyName = nameAsSafeName
            konst isVar = isVar
            konst propertyInitializer = toInitializerExpression()

            konst propertySource = toFirSourceElement()

            return buildProperty {
                source = propertySource
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Source
                returnTypeRef = propertyType
                name = propertyName
                this.isVar = isVar

                receiverParameter = receiverTypeReference.convertSafe<FirTypeRef>()?.convertToReceiverParameter()
                initializer = propertyInitializer

                konst propertyAnnotations = mutableListOf<FirAnnotationCall>()
                for (annotationEntry in annotationEntries) {
                    propertyAnnotations += annotationEntry.convert<FirAnnotationCall>()
                }
                if (this@toFirProperty.isLocal) {
                    isLocal = true
                    symbol = FirPropertySymbol(propertyName)

                    extractTypeParametersTo(this, symbol)
                    backingField = this@toFirProperty.fieldDeclaration.toFirBackingField(
                        this@toFirProperty,
                        propertySymbol = symbol,
                        propertyType,
                        emptyList(),
                    )

                    status = FirDeclarationStatusImpl(Visibilities.Local, Modality.FINAL).apply {
                        isLateInit = hasModifier(LATEINIT_KEYWORD)
                    }

                    if (hasDelegate()) {
                        fun extractDelegateExpression() =
                            this@toFirProperty.delegate?.expression.toFirExpression("Incorrect delegate expression")

                        konst delegateBuilder = FirWrappedDelegateExpressionBuilder().apply {
                            konst delegateFirExpression = extractDelegateExpression()
                            source = delegateFirExpression.source?.fakeElement(KtFakeSourceElementKind.WrappedDelegate)
                            expression = delegateFirExpression
                        }

                        generateAccessorsByDelegate(
                            delegateBuilder,
                            baseModuleData,
                            ownerRegularOrAnonymousObjectSymbol = null,
                            context = context,
                            isExtension = false,
                        )
                    }
                } else {
                    isLocal = false
                    symbol = FirPropertySymbol(callableIdForName(propertyName))
                    dispatchReceiverType = currentDispatchReceiverType()
                    extractTypeParametersTo(this, symbol)
                    withCapturedTypeParameters(true, propertySource, this.typeParameters) {
                        backingField = this@toFirProperty.fieldDeclaration.toFirBackingField(
                            this@toFirProperty,
                            propertySymbol = symbol,
                            propertyType,
                            propertyAnnotations.filter { it.useSiteTarget == FIELD || it.useSiteTarget == PROPERTY_DELEGATE_FIELD },
                        )

                        getter = this@toFirProperty.getter.toFirPropertyAccessor(
                            this@toFirProperty,
                            propertyType,
                            propertySymbol = symbol,
                            isGetter = true,
                            accessorAnnotationsFromProperty = propertyAnnotations.filterUseSiteTarget(PROPERTY_GETTER),
                            parameterAnnotationsFromProperty = emptyList()
                        )
                        setter = this@toFirProperty.setter.toFirPropertyAccessor(
                            this@toFirProperty,
                            propertyType,
                            propertySymbol = symbol,
                            isGetter = false,
                            accessorAnnotationsFromProperty = propertyAnnotations.filterUseSiteTarget(PROPERTY_SETTER),
                            parameterAnnotationsFromProperty = propertyAnnotations.filterUseSiteTarget(SETTER_PARAMETER)
                        )

                        status = FirDeclarationStatusImpl(visibility, modality).apply {
                            isExpect = hasExpectModifier() || this@RawFirBuilder.context.containerIsExpect
                            isActual = hasActualModifier()
                            isOverride = hasModifier(OVERRIDE_KEYWORD)
                            isConst = hasModifier(CONST_KEYWORD)
                            isLateInit = hasModifier(LATEINIT_KEYWORD)
                            isExternal = hasModifier(EXTERNAL_KEYWORD)
                        }

                        if (hasDelegate()) {
                            fun extractDelegateExpression() =
                                buildOrLazyExpression(this@toFirProperty.toFirSourceElement(KtFakeSourceElementKind.WrappedDelegate)) {
                                    this@toFirProperty.delegate?.expression?.let { expression ->
                                        expression.toFirExpression("Should have delegate")
                                    } ?: buildErrorExpression {
                                        diagnostic = ConeSimpleDiagnostic("Should have delegate", DiagnosticKind.ExpressionExpected)
                                    }
                                }

                            konst delegateBuilder = FirWrappedDelegateExpressionBuilder().apply {
                                konst delegateExpression = extractDelegateExpression()
                                source = delegateExpression.source?.fakeElement(KtFakeSourceElementKind.WrappedDelegate)
                                expression = delegateExpression
                            }

                            generateAccessorsByDelegate(
                                delegateBuilder,
                                baseModuleData,
                                ownerRegularOrAnonymousObjectSymbol,
                                context,
                                isExtension = receiverTypeReference != null,
                            )
                        }
                    }
                }
                annotations += if (isLocal) propertyAnnotations else propertyAnnotations.filter {
                    it.useSiteTarget != FIELD && it.useSiteTarget != PROPERTY_DELEGATE_FIELD && it.useSiteTarget != PROPERTY_GETTER &&
                            (!isVar || it.useSiteTarget != SETTER_PARAMETER && it.useSiteTarget != PROPERTY_SETTER)
                }

                contextReceivers.addAll(convertContextReceivers(this@toFirProperty.contextReceivers))
                additionalPropertyInit()
            }.also {
                if (!isLocal) {
                    fillDanglingConstraintsTo(it)
                }
            }
        }

        override fun visitAnonymousInitializer(initializer: KtAnonymousInitializer, data: FirElement?): FirElement {
            return buildAnonymousInitializer {
                source = initializer.toFirSourceElement()
                moduleData = baseModuleData
                origin = FirDeclarationOrigin.Source
                body = buildOrLazyBlock { initializer.body.toFirBlock() }
                dispatchReceiverType = context.dispatchReceiverTypesStack.lastOrNull()
            }
        }

        override fun visitProperty(property: KtProperty, data: FirElement?): FirElement {
            return property.toFirProperty(
                ownerRegularOrAnonymousObjectSymbol = null,
                context = context
            )
        }

        override fun visitTypeReference(typeReference: KtTypeReference, data: FirElement?): FirElement {
            konst typeElement = typeReference.typeElement
            konst source = typeReference.toFirSourceElement()
            konst isNullable = typeElement is KtNullableType

            // There can be KtDeclarationModifierLists in the KtTypeReference AND the descendant KtNullableTypes.
            // We aggregate them to get modifiers and annotations. Not only that, there could be multiple modifier lists on each. Examples:
            //
            //   `@A() (@B Int)`   -> Has 2 modifier lists (@A and @B) on KtTypeReference
            //   `(@A() (@B Int))? -> No modifier list on KtTypeReference, but 2 modifier lists (@A and @B) on child KtNullableType
            //   `@A() (@B Int)?   -> Has 1 modifier list (@A) on KtTypeReference, and 1 modifier list (@B) on child KtNullableType
            //   `@A (@B() (@C() (@Bar D)?)?)?` -> Has 1 modifier list (@A) on KtTypeReference and 1 modifier list on each of the
            //                                     3 descendant KtNullableTypes (@B, @C, @D)
            //
            // We need to examine all modifier lists for some cases:
            // 1. `@A Int?` and `(@A Int)?` are effectively the same, but in the latter, the modifier list is on the child KtNullableType
            // 2. `(suspend @A () -> Int)?` is a nullable suspend function type but the modifier list is on the child KtNullableType
            //
            // `getModifierList()` only returns the first one so we have to get all modifier list children.
            // TODO: Report MODIFIER_LIST_NOT_ALLOWED error when there are multiple modifier lists. How do we report on each of them?
            fun KtElementImplStub<*>.getAllModifierLists(): Array<out KtDeclarationModifierList> =
                getStubOrPsiChildren(KtStubElementTypes.MODIFIER_LIST, KtStubElementTypes.MODIFIER_LIST.arrayFactory)

            konst allModifierLists = mutableListOf<KtModifierList>(*typeReference.getAllModifierLists())

            fun KtTypeElement?.unwrapNullable(): KtTypeElement? =
                when (this) {
                    is KtNullableType -> {
                        allModifierLists += getAllModifierLists()
                        this.innerType.unwrapNullable()
                    }
                    else -> this
                }

            konst firTypeBuilder = when (konst unwrappedElement = typeElement.unwrapNullable()) {
                is KtDynamicType -> FirDynamicTypeRefBuilder().apply {
                    this.source = source
                    isMarkedNullable = isNullable
                }
                is KtUserType -> {
                    konst referenceExpression = unwrappedElement.referenceExpression
                    if (referenceExpression != null) {
                        convertKtTypeElement(source, isNullable, unwrappedElement, referenceExpression)
                    } else {
                        FirErrorTypeRefBuilder().apply {
                            this.source = source
                            diagnostic = ConeSyntaxDiagnostic("Incomplete user type")

                            konst qualifier = unwrappedElement.qualifier
                            konst reference = qualifier?.referenceExpression
                            if (qualifier != null && reference != null) {
                                partiallyResolvedTypeRef = convertKtTypeElement(
                                    qualifier.toFirSourceElement(),
                                    isNullable = false,
                                    qualifier,
                                    reference
                                ).build()
                            }
                        }
                    }
                }
                is KtFunctionType -> {
                    FirFunctionTypeRefBuilder().apply {
                        this.source = source
                        isMarkedNullable = isNullable
                        isSuspend = allModifierLists.any { it.hasSuspendModifier() }
                        receiverTypeRef = unwrappedElement.receiverTypeReference.convertSafe()
                        // TODO: probably implicit type should not be here
                        returnTypeRef = unwrappedElement.returnTypeReference.toFirOrErrorType()
                        for (konstueParameter in unwrappedElement.parameters) {
                            parameters += buildFunctionTypeParameter {
                                this.source = konstueParameter.toFirSourceElement()
                                name = konstueParameter.nameAsName
                                returnTypeRef = when {
                                    konstueParameter.typeReference != null -> konstueParameter.typeReference.toFirOrErrorType()
                                    else -> createNoTypeForParameterTypeRef()
                                }
                            }
                        }

                        contextReceiverTypeRefs.addAll(
                            unwrappedElement.contextReceiversTypeReferences.mapNotNull {
                                it.convertSafe()
                            }
                        )
                    }
                }
                is KtIntersectionType -> FirIntersectionTypeRefBuilder().apply {
                    this.source = source
                    isMarkedNullable = isNullable
                    leftType = unwrappedElement.getLeftTypeRef().toFirOrErrorType()
                    rightType = unwrappedElement.getRightTypeRef().toFirOrErrorType()
                }
                null -> FirErrorTypeRefBuilder().apply {
                    this.source = source
                    diagnostic = ConeSyntaxDiagnostic("Incomplete code")
                }
                else -> throw AssertionError("Unexpected type element: ${unwrappedElement.text}")
            }

            for (modifierList in allModifierLists) {
                for (annotationEntry in modifierList.annotationEntries) {
                    firTypeBuilder.annotations += annotationEntry.convert<FirAnnotation>()
                }
            }
            return firTypeBuilder.build()
        }

        private fun convertKtTypeElement(
            source: KtPsiSourceElement,
            isNullable: Boolean,
            ktUserType: KtUserType,
            reference: KtSimpleNameExpression,
        ): FirUserTypeRefBuilder {
            var referenceExpression: KtSimpleNameExpression? = reference
            return FirUserTypeRefBuilder().apply {
                this.source = source
                isMarkedNullable = isNullable
                var ktQualifier: KtUserType? = ktUserType

                do {
                    konst firQualifier = FirQualifierPartImpl(
                        referenceExpression!!.toFirSourceElement(),
                        referenceExpression!!.getReferencedNameAsName(),
                        FirTypeArgumentListImpl(ktQualifier?.typeArgumentList?.toKtPsiSourceElement() ?: source).apply {
                            typeArguments.appendTypeArguments(ktQualifier!!.typeArguments)
                        }
                    )
                    qualifier.add(firQualifier)

                    ktQualifier = ktQualifier!!.qualifier
                    referenceExpression = ktQualifier?.referenceExpression
                } while (referenceExpression != null)

                qualifier.reverse()
            }
        }

        override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry, data: FirElement?): FirElement {
            return buildAnnotationCall {
                source = annotationEntry.toFirSourceElement()
                useSiteTarget = annotationEntry.useSiteTarget?.getAnnotationUseSiteTarget()
                annotationTypeRef = annotationEntry.typeReference.toFirOrErrorType()
                annotationEntry.extractArgumentsTo(this)
                konst name = (annotationTypeRef as? FirUserTypeRef)?.qualifier?.last()?.name ?: Name.special("<no-annotation-name>")
                calleeReference = buildSimpleNamedReference {
                    source = (annotationEntry.typeReference?.typeElement as? KtUserType)?.referenceExpression?.toFirSourceElement()
                    this.name = name
                }
                typeArguments.appendTypeArguments(annotationEntry.typeArguments)
            }
        }

        override fun visitTypeProjection(typeProjection: KtTypeProjection, data: FirElement?): FirElement {
            konst projectionKind = typeProjection.projectionKind
            konst projectionSource = typeProjection.toFirSourceElement()
            if (projectionKind == KtProjectionKind.STAR) {
                return buildStarProjection {
                    source = projectionSource
                }
            }
            konst argumentList = typeProjection.parent as? KtTypeArgumentList
            konst typeReference = typeProjection.typeReference
            if (argumentList?.parent is KtCallExpression && typeReference?.isPlaceholder == true) {
                return buildPlaceholderProjection {
                    source = projectionSource
                }
            }
            konst firType = typeReference.toFirOrErrorType()
            return buildTypeProjectionWithVariance {
                source = projectionSource
                typeRef = firType
                variance = when (projectionKind) {
                    KtProjectionKind.IN -> Variance.IN_VARIANCE
                    KtProjectionKind.OUT -> Variance.OUT_VARIANCE
                    KtProjectionKind.NONE -> Variance.INVARIANT
                    KtProjectionKind.STAR -> shouldNotBeCalled()
                }
            }
        }

        override fun visitBlockExpression(expression: KtBlockExpression, data: FirElement?): FirElement {
            return configureBlockWithoutBuilding(expression).build()
        }

        private fun configureBlockWithoutBuilding(expression: KtBlockExpression): FirBlockBuilder {
            return FirBlockBuilder().apply {
                source = expression.toFirSourceElement()
                for (statement in expression.statements) {
                    konst firStatement = statement.toFirStatement { "Statement expected: ${statement.text}" }
                    konst isForLoopBlock =
                        firStatement is FirBlock && firStatement.source?.kind == KtFakeSourceElementKind.DesugaredForLoop
                    if (firStatement !is FirBlock || isForLoopBlock || firStatement.annotations.isNotEmpty()) {
                        statements += firStatement
                    } else {
                        statements += firStatement.statements
                    }
                }
            }
        }

        override fun visitSimpleNameExpression(expression: KtSimpleNameExpression, data: FirElement?): FirElement {
            konst qualifiedSource = when {
                expression.getQualifiedExpressionForSelector() != null -> expression.parent
                else -> expression
            }.toFirSourceElement()

            konst expressionSource = expression.toFirSourceElement()
            var diagnostic: ConeDiagnostic? = null
            konst rawText = expression.getReferencedNameElement().node.text
            if (rawText.isUnderscore) {
                diagnostic = ConeUnderscoreUsageWithoutBackticks(expressionSource)
            }

            return generateAccessExpression(
                qualifiedSource,
                expressionSource,
                expression.getReferencedNameAsName(),
                diagnostic
            )
        }

        override fun visitConstantExpression(expression: KtConstantExpression, data: FirElement?): FirElement =
            generateConstantExpressionByLiteral(expression)

        override fun visitStringTemplateExpression(expression: KtStringTemplateExpression, data: FirElement?): FirElement {
            return expression.entries.toInterpolatingCall(
                expression,
                getElementType = { element ->
                    when (element) {
                        is KtLiteralStringTemplateEntry -> KtNodeTypes.LITERAL_STRING_TEMPLATE_ENTRY
                        is KtEscapeStringTemplateEntry -> KtNodeTypes.ESCAPE_STRING_TEMPLATE_ENTRY
                        is KtSimpleNameStringTemplateEntry -> KtNodeTypes.SHORT_STRING_TEMPLATE_ENTRY
                        is KtBlockStringTemplateEntry -> KtNodeTypes.LONG_STRING_TEMPLATE_ENTRY
                        else -> error("inkonstid node type $element")
                    }
                },
                convertTemplateEntry = {
                    (this as KtStringTemplateEntryWithExpression).expression.toFirExpression(it)
                },
            )
        }

        override fun visitReturnExpression(expression: KtReturnExpression, data: FirElement?): FirElement {
            konst source = expression.toFirSourceElement(KtFakeSourceElementKind.ImplicitUnit)
            konst result = expression.returnedExpression?.toFirExpression("Incorrect return expression")
                ?: buildUnitExpression { this.source = source }
            return result.toReturn(source, expression.getTargetLabel()?.getReferencedName(), fromKtReturnExpression = true)
        }

        override fun visitTryExpression(expression: KtTryExpression, data: FirElement?): FirElement {
            return buildTryExpression {
                source = expression.toFirSourceElement()
                tryBlock = expression.tryBlock.toFirBlock()
                finallyBlock = expression.finallyBlock?.finalExpression?.toFirBlock()
                for (clause in expression.catchClauses) {
                    konst parameter = clause.catchParameter?.let { ktParameter ->
                        konst name = convertValueParameterName(
                            ktParameter.nameAsSafeName,
                            ValueParameterDeclaration.CATCH
                        ) { ktParameter.nameIdentifier?.node?.text }

                        buildProperty {
                            source = ktParameter.toFirSourceElement()
                            moduleData = baseModuleData
                            origin = FirDeclarationOrigin.Source
                            returnTypeRef = when {
                                ktParameter.typeReference != null -> ktParameter.typeReference.toFirOrErrorType()
                                else -> createNoTypeForParameterTypeRef()
                            }
                            isVar = false
                            status = FirResolvedDeclarationStatusImpl(Visibilities.Local, Modality.FINAL, EffectiveVisibility.Local)
                            isLocal = true
                            this.name = name
                            symbol = FirPropertySymbol(CallableId(name))
                            for (annotationEntry in ktParameter.annotationEntries) {
                                this.annotations += annotationEntry.convert<FirAnnotation>()
                            }
                        }.also {
                            it.isCatchParameter = true
                        }
                    } ?: continue
                    catches += buildCatch {
                        source = clause.toFirSourceElement()
                        this.parameter = parameter
                        block = clause.catchBody.toFirBlock()
                    }
                }
            }
        }

        override fun visitIfExpression(expression: KtIfExpression, data: FirElement?): FirElement {
            return buildWhenExpression {
                source = expression.toFirSourceElement()

                var ktLastIf: KtIfExpression = expression
                whenBranches@ while (true) {
                    konst ktCondition = ktLastIf.condition
                    branches += buildWhenBranch {
                        source = ktCondition?.toFirSourceElement(KtFakeSourceElementKind.WhenCondition)
                        condition = ktCondition.toFirExpression("If statement should have condition")
                        result = ktLastIf.then.toFirBlock()
                    }

                    when (konst ktElse = ktLastIf.`else`) {
                        null -> break@whenBranches
                        is KtIfExpression -> ktLastIf = ktElse
                        else -> {
                            branches += buildWhenBranch {
                                source = ktLastIf.elseKeyword?.toKtPsiSourceElement()
                                condition = buildElseIfTrueCondition()
                                result = ktLastIf.`else`.toFirBlock()
                            }
                            break@whenBranches
                        }
                    }
                }

                usedAsExpression = expression.usedAsExpression
            }
        }

        override fun visitWhenExpression(expression: KtWhenExpression, data: FirElement?): FirElement {
            konst ktSubjectExpression = expression.subjectExpression
            konst subjectExpression = when (ktSubjectExpression) {
                is KtVariableDeclaration -> ktSubjectExpression.initializer
                else -> ktSubjectExpression
            }?.toFirExpression("Incorrect when subject expression: ${ktSubjectExpression?.text}")
            konst subjectVariable = when (ktSubjectExpression) {
                is KtVariableDeclaration -> {
                    konst name = ktSubjectExpression.nameAsSafeName
                    buildProperty {
                        source = ktSubjectExpression.toFirSourceElement()
                        moduleData = baseModuleData
                        origin = FirDeclarationOrigin.Source
                        returnTypeRef = ktSubjectExpression.typeReference.toFirOrImplicitType()
                        this.name = name
                        initializer = subjectExpression
                        delegate = null
                        isVar = false
                        symbol = FirPropertySymbol(name)
                        isLocal = true
                        status = FirDeclarationStatusImpl(Visibilities.Local, Modality.FINAL)
                    }
                }
                else -> null
            }
            konst hasSubject = subjectExpression != null

            @OptIn(FirContractViolation::class)
            konst ref = FirExpressionRef<FirWhenExpression>()
            return buildWhenExpression {
                source = expression.toFirSourceElement()
                this.subject = subjectExpression
                this.subjectVariable = subjectVariable
                usedAsExpression = expression.usedAsExpression

                for (entry in expression.entries) {
                    konst entrySource = entry.toFirSourceElement()
                    konst branchBody = entry.expression.toFirBlock()
                    branches += if (!entry.isElse) {
                        if (hasSubject) {
                            buildWhenBranch {
                                source = entrySource
                                condition = entry.conditions.toFirWhenCondition(
                                    ref,
                                    { toFirExpression(it) },
                                    { toFirOrErrorType() },
                                )
                                result = branchBody
                            }
                        } else {
                            konst ktCondition = entry.conditions.first()
                            buildWhenBranch {
                                source = entrySource
                                condition = ((ktCondition as? KtWhenConditionWithExpression)?.expression
                                    ?: ktCondition)
                                    .toFirExpression(
                                        "No expression in condition with expression",
                                        DiagnosticKind.ExpressionExpected
                                    )
                                result = branchBody
                            }
                        }
                    } else {
                        buildWhenBranch {
                            source = entrySource
                            condition = buildElseIfTrueCondition()
                            result = branchBody
                        }
                    }
                }
            }.also {
                if (hasSubject) {
                    ref.bind(it)
                }
            }
        }

        private konst KtExpression.usedAsExpression: Boolean
            get() {
                var parent = parent
                while (parent.elementType == KtNodeTypes.ANNOTATED_EXPRESSION ||
                    parent.elementType == KtNodeTypes.LABELED_EXPRESSION
                ) {
                    parent = parent.parent
                }
                if (parent is KtBlockExpression) return false
                when (parent.elementType) {
                    KtNodeTypes.THEN, KtNodeTypes.ELSE, KtNodeTypes.WHEN_ENTRY -> {
                        return (parent.parent as? KtExpression)?.usedAsExpression ?: true
                    }
                }
                if (parent is KtScriptInitializer) return false
                // Here we check that when used is a single statement of a loop
                if (parent !is KtContainerNodeForControlStructureBody) return true
                konst type = parent.parent.elementType
                return !(type == KtNodeTypes.FOR || type == KtNodeTypes.WHILE || type == KtNodeTypes.DO_WHILE)
            }

        override fun visitDoWhileExpression(expression: KtDoWhileExpression, data: FirElement?): FirElement {
            konst target: FirLoopTarget
            return FirDoWhileLoopBuilder().apply {
                source = expression.toFirSourceElement()
                // For break/continue in the do-while loop condition, prepare the loop target first so that it can refer to the same loop.
                target = prepareTarget(expression)
                condition = expression.condition.toFirExpression("No condition in do-while loop")
            }.configure(target) { expression.body.toFirBlock() }
        }

        override fun visitWhileExpression(expression: KtWhileExpression, data: FirElement?): FirElement {
            konst target: FirLoopTarget
            return FirWhileLoopBuilder().apply {
                source = expression.toFirSourceElement()
                condition = expression.condition.toFirExpression("No condition in while loop")
                // break/continue in the while loop condition will refer to an outer loop if any.
                // So, prepare the loop target after building the condition.
                target = prepareTarget(expression)
            }.configure(target) { expression.body.toFirBlock() }
        }

        override fun visitForExpression(expression: KtForExpression, data: FirElement?): FirElement {
            konst rangeExpression = expression.loopRange.toFirExpression("No range in for loop")
            konst ktParameter = expression.loopParameter
            konst fakeSource = expression.toKtPsiSourceElement(KtFakeSourceElementKind.DesugaredForLoop)
            konst target: FirLoopTarget
            // NB: FirForLoopChecker relies on this block existence and structure
            return buildBlock {
                source = fakeSource
                konst rangeSource = expression.loopRange?.toFirSourceElement(KtFakeSourceElementKind.DesugaredForLoop)
                konst iteratorVal = generateTemporaryVariable(
                    baseModuleData, rangeSource, SpecialNames.ITERATOR,
                    buildFunctionCall {
                        source = fakeSource
                        calleeReference = buildSimpleNamedReference {
                            source = fakeSource
                            name = OperatorNameConventions.ITERATOR
                        }
                        explicitReceiver = rangeExpression
                    },
                )
                statements += iteratorVal
                statements += FirWhileLoopBuilder().apply {
                    source = expression.toFirSourceElement()
                    condition = buildFunctionCall {
                        source = fakeSource
                        calleeReference = buildSimpleNamedReference {
                            source = fakeSource
                            name = OperatorNameConventions.HAS_NEXT
                        }
                        explicitReceiver = generateResolvedAccessExpression(fakeSource, iteratorVal)
                    }
                    // break/continue in the for loop condition will refer to an outer loop if any.
                    // So, prepare the loop target after building the condition.
                    target = prepareTarget(expression)
                }.configure(target) {
                    konst blockBuilder = FirBlockBuilder().apply {
                        source = expression.toFirSourceElement(KtFakeSourceElementKind.DesugaredForLoop)
                    }
                    if (ktParameter != null) {
                        konst multiDeclaration = ktParameter.destructuringDeclaration
                        konst firLoopParameter = generateTemporaryVariable(
                            moduleData = baseModuleData, source = expression.loopParameter?.toFirSourceElement(),
                            name = if (multiDeclaration != null) SpecialNames.DESTRUCT else ktParameter.nameAsSafeName,
                            initializer = buildFunctionCall {
                                source = fakeSource
                                calleeReference = buildSimpleNamedReference {
                                    source = fakeSource
                                    name = OperatorNameConventions.NEXT
                                }
                                explicitReceiver = generateResolvedAccessExpression(fakeSource, iteratorVal)
                            },
                            typeRef = ktParameter.typeReference.toFirOrImplicitType(),
                        )
                        if (multiDeclaration != null) {
                            konst destructuringBlock = generateDestructuringBlock(
                                baseModuleData,
                                multiDeclaration = multiDeclaration,
                                container = firLoopParameter,
                                tmpVariable = true,
                                extractAnnotationsTo = { extractAnnotationsTo(it) },
                            ) { toFirOrImplicitType() }
                            blockBuilder.statements.addAll(destructuringBlock.statements)
                        } else {
                            blockBuilder.statements.add(firLoopParameter)
                        }
                    }
                    blockBuilder.statements.add(expression.body.toFirBlock())
                    blockBuilder.build()
                }
            }
        }

        override fun visitBreakExpression(expression: KtBreakExpression, data: FirElement?): FirElement {
            return FirBreakExpressionBuilder().apply {
                source = expression.toFirSourceElement()
            }.bindLabel(expression).build()
        }

        override fun visitContinueExpression(expression: KtContinueExpression, data: FirElement?): FirElement {
            return FirContinueExpressionBuilder().apply {
                source = expression.toFirSourceElement()
            }.bindLabel(expression).build()
        }

        override fun visitBinaryExpression(expression: KtBinaryExpression, data: FirElement?): FirElement {
            konst operationToken = expression.operationToken

            if (operationToken == IDENTIFIER) {
                context.calleeNamesForLambda += expression.operationReference.getReferencedNameAsName()
            } else {
                context.calleeNamesForLambda += null
            }

            konst leftArgument = expression.left.toFirExpression("No left operand")
            konst rightArgument = expression.right.toFirExpression("No right operand")

            // No need for the callee name since arguments are already generated
            context.calleeNamesForLambda.removeLast()

            konst source = expression.toFirSourceElement()

            when (operationToken) {
                ELVIS ->
                    return leftArgument.generateNotNullOrOther(rightArgument, source)
                ANDAND, OROR ->
                    return leftArgument.generateLazyLogicalOperation(rightArgument, operationToken == ANDAND, source)
                in OperatorConventions.IN_OPERATIONS ->
                    return rightArgument.generateContainsOperation(
                        leftArgument, operationToken == NOT_IN, source,
                        expression.operationReference.toFirSourceElement(),
                    )
                in OperatorConventions.COMPARISON_OPERATIONS ->
                    return leftArgument.generateComparisonExpression(
                        rightArgument, operationToken, source,
                        expression.operationReference.toFirSourceElement(),
                    )
            }
            konst conventionCallName = operationToken.toBinaryName()
            return if (conventionCallName != null || operationToken == IDENTIFIER) {
                buildFunctionCall {
                    this.source = source
                    calleeReference = buildSimpleNamedReference {
                        this.source = expression.operationReference.toFirSourceElement()
                        name = conventionCallName ?: expression.operationReference.getReferencedNameAsName()
                    }
                    explicitReceiver = leftArgument
                    argumentList = buildUnaryArgumentList(rightArgument)
                    origin = if (conventionCallName != null) FirFunctionCallOrigin.Operator else FirFunctionCallOrigin.Infix
                }
            } else {
                konst firOperation = operationToken.toFirOperation()
                if (firOperation in FirOperation.ASSIGNMENTS) {
                    return expression.left.generateAssignment(
                        source,
                        expression.left?.toFirSourceElement(),
                        rightArgument,
                        firOperation,
                        leftArgument.annotations,
                        expression.right,
                    ) {
                        (this as KtExpression).toFirExpression("Incorrect expression in assignment: ${expression.text}")
                    }
                } else {
                    buildEqualityOperatorCall {
                        this.source = source
                        operation = firOperation
                        argumentList = buildBinaryArgumentList(leftArgument, rightArgument)
                    }
                }
            }
        }

        override fun visitBinaryWithTypeRHSExpression(expression: KtBinaryExpressionWithTypeRHS, data: FirElement?): FirElement {
            return buildTypeOperatorCall {
                source = expression.toFirSourceElement()
                operation = expression.operationReference.getReferencedNameElementType().toFirOperation()
                conversionTypeRef = expression.right.toFirOrErrorType()
                argumentList = buildUnaryArgumentList(expression.left.toFirExpression("No left operand"))
            }
        }

        override fun visitIsExpression(expression: KtIsExpression, data: FirElement?): FirElement {
            return buildTypeOperatorCall {
                source = expression.toFirSourceElement()
                operation = if (expression.isNegated) FirOperation.NOT_IS else FirOperation.IS
                conversionTypeRef = expression.typeReference.toFirOrErrorType()
                argumentList = buildUnaryArgumentList(expression.leftHandSide.toFirExpression("No left operand"))
            }
        }

        override fun visitUnaryExpression(expression: KtUnaryExpression, data: FirElement?): FirElement {
            konst operationToken = expression.operationToken
            konst argument = expression.baseExpression
            konst conventionCallName = operationToken.toUnaryName()
            return when {
                operationToken == EXCLEXCL -> {
                    buildCheckNotNullCall {
                        source = expression.toFirSourceElement()
                        argumentList = buildUnaryArgumentList(argument.toFirExpression("No operand"))
                    }
                }
                conventionCallName != null -> {
                    if (operationToken in OperatorConventions.INCREMENT_OPERATIONS) {
                        return generateIncrementOrDecrementBlock(
                            expression, expression.operationReference, argument,
                            callName = conventionCallName,
                            prefix = expression is KtPrefixExpression,
                        ) { (this as KtExpression).toFirExpression("Incorrect expression inside inc/dec") }
                    }

                    konst receiver = argument.toFirExpression("No operand")

                    convertUnaryPlusMinusCallOnIntegerLiteralIfNecessary(expression, receiver, operationToken)?.let { return it }

                    buildFunctionCall {
                        source = expression.toFirSourceElement()
                        calleeReference = buildSimpleNamedReference {
                            source = expression.operationReference.toFirSourceElement()
                            name = conventionCallName
                        }
                        explicitReceiver = receiver
                        origin = FirFunctionCallOrigin.Operator
                    }
                }
                else -> throw IllegalStateException("Unexpected expression: ${expression.text}")
            }
        }

        private fun splitToCalleeAndReceiver(
            calleeExpression: KtExpression?,
            defaultSource: KtPsiSourceElement,
        ): CalleeAndReceiver {
            return when (calleeExpression) {
                is KtSimpleNameExpression ->
                    CalleeAndReceiver(
                        buildSimpleNamedReference {
                            source = calleeExpression.toFirSourceElement()
                            name = calleeExpression.getReferencedNameAsName()
                        }
                    )

                is KtParenthesizedExpression -> splitToCalleeAndReceiver(calleeExpression.expression, defaultSource)

                null -> {
                    CalleeAndReceiver(
                        buildErrorNamedReference { diagnostic = ConeSyntaxDiagnostic("Call has no callee") }
                    )
                }

                is KtSuperExpression -> {
                    CalleeAndReceiver(
                        buildErrorNamedReference {
                            source = calleeExpression.toFirSourceElement()
                            diagnostic = ConeSimpleDiagnostic("Super cannot be a callee", DiagnosticKind.SuperNotAllowed)
                        }
                    )
                }

                else -> {
                    CalleeAndReceiver(
                        buildSimpleNamedReference {
                            source = defaultSource.fakeElement(KtFakeSourceElementKind.ImplicitInvokeCall)
                            name = OperatorNameConventions.INVOKE
                        },
                        receiverExpression = calleeExpression.toFirExpression("Incorrect invoke receiver"),
                        isImplicitInvoke = true
                    )
                }
            }
        }

        override fun visitCallExpression(expression: KtCallExpression, data: FirElement?): FirElement {
            konst source = expression.toFirSourceElement()
            konst (calleeReference, explicitReceiver, isImplicitInvoke) = splitToCalleeAndReceiver(expression.calleeExpression, source)

            konst result: FirQualifiedAccessExpressionBuilder =
                if (expression.konstueArgumentList == null && expression.lambdaArguments.isEmpty()) {
                    FirPropertyAccessExpressionBuilder().apply {
                        this.source = source
                        this.calleeReference = calleeReference
                    }
                } else {
                    konst builder = if (isImplicitInvoke) FirImplicitInvokeCallBuilder() else FirFunctionCallBuilder()
                    builder.apply {
                        this.source = source
                        this.calleeReference = calleeReference
                        context.calleeNamesForLambda += calleeReference.name
                        expression.extractArgumentsTo(this)
                        context.calleeNamesForLambda.removeLast()
                    }
                }

            return result.apply {
                this.explicitReceiver = explicitReceiver
                typeArguments.appendTypeArguments(expression.typeArguments)
            }.build()
        }

        override fun visitArrayAccessExpression(expression: KtArrayAccessExpression, data: FirElement?): FirElement {
            konst arrayExpression = expression.arrayExpression
            konst setArgument = context.arraySetArgument.remove(expression)
            return buildFunctionCall {
                konst isGet = setArgument == null
                source = (if (isGet) expression else expression.parent).toFirSourceElement()
                calleeReference = buildSimpleNamedReference {
                    source = expression.toFirSourceElement().fakeElement(KtFakeSourceElementKind.ArrayAccessNameReference)
                    name = if (isGet) OperatorNameConventions.GET else OperatorNameConventions.SET
                }
                explicitReceiver = arrayExpression.toFirExpression("No array expression")
                argumentList = buildArgumentList {
                    for (indexExpression in expression.indexExpressions) {
                        arguments += indexExpression.toFirExpression("Incorrect index expression")
                    }
                    if (setArgument != null) {
                        arguments += setArgument
                    }
                }
                origin = FirFunctionCallOrigin.Operator
            }.pullUpSafeCallIfNecessary()
        }

        override fun visitQualifiedExpression(expression: KtQualifiedExpression, data: FirElement?): FirElement {
            konst receiver = expression.receiverExpression.toFirExpression("Incorrect receiver expression")

            konst selector = expression.selectorExpression
                ?: return buildErrorExpression {
                    source = expression.toFirSourceElement()
                    diagnostic = ConeSyntaxDiagnostic("Qualified expression without selector")

                    // if there is no selector, we still want to resolve the receiver
                    this.expression = receiver
                }

            konst firSelector = selector.toFirExpression("Incorrect selector expression")
            if (firSelector is FirQualifiedAccessExpression) {
                if (expression is KtSafeQualifiedExpression) {
                    @OptIn(FirImplementationDetail::class)
                    firSelector.replaceSource(expression.toFirSourceElement(KtFakeSourceElementKind.DesugaredSafeCallExpression))
                    return firSelector.createSafeCall(
                        receiver,
                        expression.toFirSourceElement()
                    )
                }

                return convertFirSelector(firSelector, expression.toFirSourceElement(), receiver)
            }
            if (firSelector is FirErrorExpression) {
                return buildQualifiedErrorAccessExpression {
                    this.receiver = receiver
                    this.selector = firSelector
                    source = expression.toFirSourceElement()
                    diagnostic = ConeSyntaxDiagnostic("Qualified expression with unexpected selector")
                }
            }
            return firSelector
        }

        override fun visitThisExpression(expression: KtThisExpression, data: FirElement?): FirElement {
            return buildThisReceiverExpression {
                konst sourceElement = expression.toFirSourceElement()
                source = sourceElement
                calleeReference = buildExplicitThisReference {
                    source = sourceElement.fakeElement(KtFakeSourceElementKind.ReferenceInAtomicQualifiedAccess)
                    labelName = expression.getLabelName()
                }
            }
        }

        override fun visitSuperExpression(expression: KtSuperExpression, data: FirElement?): FirElement {
            konst superType = expression.superTypeQualifier
            konst theSource = expression.toFirSourceElement()
            return buildPropertyAccessExpression {
                this.source = theSource
                calleeReference = buildExplicitSuperReference {
                    source = theSource.fakeElement(KtFakeSourceElementKind.ReferenceInAtomicQualifiedAccess)
                    labelName = expression.getLabelName()
                    superTypeRef = superType.toFirOrImplicitType()
                }
            }
        }

        override fun visitParenthesizedExpression(expression: KtParenthesizedExpression, data: FirElement?): FirElement {
            context.forwardLabelUsagePermission(expression, expression.expression)
            return expression.expression?.accept(this, data)
                ?: buildErrorExpression(
                    expression.toFirSourceElement(),
                    ConeSyntaxDiagnostic("Empty parentheses")
                )
        }

        override fun visitLabeledExpression(expression: KtLabeledExpression, data: FirElement?): FirElement {
            konst label = expression.getTargetLabel()
            var errorLabelSource: KtSourceElement? = null

            konst result = if (label != null) {
                konst rawName = label.getReferencedNameElement().node!!.text
                konst labelAndErrorSource = buildLabelAndErrorSource(rawName, label.toKtPsiSourceElement())
                errorLabelSource = labelAndErrorSource.second
                context.withNewLabel(labelAndErrorSource.first, expression.baseExpression) {
                    expression.baseExpression?.accept(this, data)
                }
            } else {
                expression.baseExpression?.accept(this, data)
            }

            return buildExpressionWithErrorLabel(result, errorLabelSource, expression.toFirSourceElement())
        }

        override fun visitAnnotatedExpression(expression: KtAnnotatedExpression, data: FirElement?): FirElement {
            konst baseExpression = expression.baseExpression
            context.forwardLabelUsagePermission(expression, baseExpression)
            konst rawResult = baseExpression?.accept(this, data)
            konst result = rawResult as? FirAnnotationContainer
                ?: buildErrorExpression(
                    expression.toFirSourceElement(),
                    ConeNotAnnotationContainer(rawResult?.render() ?: "???")
                )
            expression.extractAnnotationsTo(result)
            return result
        }

        override fun visitThrowExpression(expression: KtThrowExpression, data: FirElement?): FirElement {
            return buildThrowExpression {
                source = expression.toFirSourceElement()
                exception = expression.thrownExpression.toFirExpression("Nothing to throw")
            }
        }

        override fun visitDestructuringDeclaration(multiDeclaration: KtDestructuringDeclaration, data: FirElement?): FirElement {
            konst baseVariable = generateTemporaryVariable(
                baseModuleData,
                multiDeclaration.toFirSourceElement(),
                "destruct",
                multiDeclaration.initializer.toFirExpression { ConeSyntaxDiagnostic("Initializer required for destructuring declaration") },
                extractAnnotationsTo = { extractAnnotationsTo(it) }
            )
            return generateDestructuringBlock(
                baseModuleData,
                multiDeclaration,
                baseVariable,
                tmpVariable = true,
                extractAnnotationsTo = { extractAnnotationsTo(it) },
            ) {
                toFirOrImplicitType()
            }
        }

        override fun visitClassLiteralExpression(expression: KtClassLiteralExpression, data: FirElement?): FirElement {
            return buildGetClassCall {
                source = expression.toFirSourceElement()
                argumentList = buildUnaryArgumentList(expression.receiverExpression.toFirExpression("No receiver in class literal"))
            }
        }

        override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression, data: FirElement?): FirElement {
            return buildCallableReferenceAccess {
                source = expression.toFirSourceElement()
                calleeReference = buildSimpleNamedReference {
                    source = expression.callableReference.toFirSourceElement()
                    name = expression.callableReference.getReferencedNameAsName()
                }
                explicitReceiver = expression.receiverExpression?.toFirExpression("Incorrect receiver expression")
                hasQuestionMarkAtLHS = expression.hasQuestionMarks
            }
        }

        override fun visitCollectionLiteralExpression(expression: KtCollectionLiteralExpression, data: FirElement?): FirElement {
            return buildArrayOfCall {
                source = expression.toFirSourceElement()
                argumentList = buildArgumentList {
                    for (innerExpression in expression.getInnerExpressions()) {
                        arguments += innerExpression.toFirExpression("Incorrect collection literal argument")
                    }
                }
            }
        }

        override fun visitExpression(expression: KtExpression, data: FirElement?): FirElement {
            return buildExpressionStub {
                source = expression.toFirSourceElement()
            }
        }

        private fun MutableList<FirTypeProjection>.appendTypeArguments(args: List<KtTypeProjection>) {
            for (typeArgument in args) {
                this += typeArgument.convert<FirTypeProjection>()
            }
        }

        private fun buildErrorTopLevelDeclarationForDanglingModifierList(modifierList: KtModifierList) = buildDanglingModifierList {
            this.source = modifierList.toFirSourceElement(KtFakeSourceElementKind.DanglingModifierList)
            moduleData = baseModuleData
            origin = FirDeclarationOrigin.Source
            diagnostic = ConeDanglingModifierOnTopLevel
            symbol = FirDanglingModifierSymbol()
            for (annotationEntry in modifierList.getAnnotationEntries()) {
                annotations += annotationEntry.convert<FirAnnotation>()
            }
        }
    }
}

enum class BodyBuildingMode {
    /**
     * Build every expression and every body
     */
    NORMAL,

    /**
     * Build [org.jetbrains.kotlin.fir.expressions.impl.FirLazyBlock] for function bodies, constructors & getters/setters
     * Build [org.jetbrains.kotlin.fir.expressions.impl.FirLazyExpression] for property initializers
     */
    LAZY_BODIES;

    companion object {
        fun lazyBodies(lazyBodies: Boolean): BodyBuildingMode =
            if (lazyBodies) LAZY_BODIES else NORMAL
    }
}
