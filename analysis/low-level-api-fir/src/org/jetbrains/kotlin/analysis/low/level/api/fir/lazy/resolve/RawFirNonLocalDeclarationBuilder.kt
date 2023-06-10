/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.lazy.resolve

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.FirDesignation
import org.jetbrains.kotlin.analysis.low.level.api.fir.project.structure.llFirModuleData
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.errorWithFirSpecificEntries
import org.jetbrains.kotlin.analysis.utils.errors.buildErrorWithAttachment
import org.jetbrains.kotlin.analysis.utils.errors.withPsiEntry
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.builder.BodyBuildingMode
import org.jetbrains.kotlin.fir.builder.RawFirBuilder
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.FirBackingFieldBuilder
import org.jetbrains.kotlin.fir.declarations.builder.FirFunctionBuilder
import org.jetbrains.kotlin.fir.declarations.builder.FirPropertyAccessorBuilder
import org.jetbrains.kotlin.fir.declarations.builder.FirPropertyBuilder
import org.jetbrains.kotlin.fir.declarations.utils.isInner
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirMultiDelegatedConstructorCall
import org.jetbrains.kotlin.fir.references.FirSuperReference
import org.jetbrains.kotlin.fir.scopes.FirScopeProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.name.NameUtils
import org.jetbrains.kotlin.psi
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.hasExpectModifier

internal class RawFirNonLocalDeclarationBuilder private constructor(
    session: FirSession,
    baseScopeProvider: FirScopeProvider,
    private konst originalDeclaration: FirDeclaration,
    private konst declarationToBuild: KtDeclaration,
    private konst functionsToRebind: Set<FirFunction>? = null,
    private konst replacementApplier: RawFirReplacement.Applier? = null,
    private konst additionalFunctionInit: FirFunctionBuilder.() -> Unit = {},
    private konst additionalPropertyInit: FirPropertyBuilder.() -> Unit = {},
    private konst additionalAccessorInit: FirPropertyAccessorBuilder.() -> Unit = {},
    private konst additionalBackingFieldInit: FirBackingFieldBuilder.() -> Unit = {},
) : RawFirBuilder(session, baseScopeProvider, bodyBuildingMode = BodyBuildingMode.NORMAL) {
    override fun FirFunctionBuilder.additionalFunctionInit() {
        additionalFunctionInit.invoke(this)
    }

    override fun FirPropertyBuilder.additionalPropertyInit() {
        additionalPropertyInit.invoke(this)
    }

    override fun FirPropertyAccessorBuilder.additionalPropertyAccessorInit() {
        additionalAccessorInit.invoke(this)
    }

    override fun FirBackingFieldBuilder.additionalBackingFieldInit() {
        additionalBackingFieldInit.invoke(this)
    }

    companion object {
        fun buildNewFile(
            session: FirSession,
            scopeProvider: FirScopeProvider,
            file: KtFile,
        ): FirFile {
            konst builder = RawFirBuilder(session,scopeProvider, bodyBuildingMode = BodyBuildingMode.LAZY_BODIES)
            return builder.buildFirFile(file)
        }

        fun buildNewSimpleFunction(
            session: FirSession,
            scopeProvider: FirScopeProvider,
            designation: FirDesignation,
            newFunction: KtNamedFunction,
            additionalFunctionInit: FirFunctionBuilder.() -> Unit,
        ): FirSimpleFunction {
            konst builder = RawFirNonLocalDeclarationBuilder(
                session = session,
                baseScopeProvider = scopeProvider,
                originalDeclaration = designation.target as FirDeclaration,
                declarationToBuild = newFunction,
                additionalFunctionInit = additionalFunctionInit,
            )

            builder.context.packageFqName = newFunction.containingKtFile.packageFqName
            return builder.moveNext(designation.path.iterator(), containingClass = null) as FirSimpleFunction
        }

        fun buildNewProperty(
            session: FirSession,
            scopeProvider: FirScopeProvider,
            designation: FirDesignation,
            newProperty: KtProperty,
            additionalPropertyInit: FirPropertyBuilder.() -> Unit,
            additionalAccessorInit: FirPropertyAccessorBuilder.() -> Unit,
            additionalBackingFieldInit: FirBackingFieldBuilder.() -> Unit,
        ): FirProperty {
            konst builder = RawFirNonLocalDeclarationBuilder(
                session = session,
                baseScopeProvider = scopeProvider,
                originalDeclaration = designation.target as FirDeclaration,
                declarationToBuild = newProperty,
                additionalPropertyInit = additionalPropertyInit,
                additionalAccessorInit = additionalAccessorInit,
                additionalBackingFieldInit = additionalBackingFieldInit,
            )

            builder.context.packageFqName = newProperty.containingKtFile.packageFqName
            return builder.moveNext(designation.path.iterator(), containingClass = null) as FirProperty
        }

        fun buildWithReplacement(
            session: FirSession,
            scopeProvider: FirScopeProvider,
            designation: FirDesignation,
            rootNonLocalDeclaration: KtDeclaration,
            replacement: RawFirReplacement?
        ): FirDeclaration {
            konst replacementApplier = replacement?.Applier()
            konst builder = RawFirNonLocalDeclarationBuilder(
                session = session,
                baseScopeProvider = scopeProvider,
                originalDeclaration = designation.target as FirDeclaration,
                declarationToBuild = rootNonLocalDeclaration,
                replacementApplier = replacementApplier
            )
            builder.context.packageFqName = rootNonLocalDeclaration.containingKtFile.packageFqName
            return builder.moveNext(designation.path.iterator(), containingClass = null).also {
                replacementApplier?.ensureApplied()
            }
        }

        fun buildWithFunctionSymbolRebind(
            session: FirSession,
            scopeProvider: FirScopeProvider,
            designation: FirDesignation,
            rootNonLocalDeclaration: KtDeclaration,
        ): FirDeclaration {
            konst functionsToRebind = when (konst originalDeclaration = designation.target) {
                is FirFunction -> setOf(originalDeclaration)
                is FirProperty -> setOfNotNull(originalDeclaration.getter, originalDeclaration.setter)
                else -> null
            }

            konst builder = RawFirNonLocalDeclarationBuilder(
                session = session,
                baseScopeProvider = scopeProvider,
                originalDeclaration = designation.target as FirDeclaration,
                declarationToBuild = rootNonLocalDeclaration,
                functionsToRebind = functionsToRebind,
            )
            builder.context.packageFqName = rootNonLocalDeclaration.containingKtFile.packageFqName
            return builder.moveNext(designation.path.iterator(), containingClass = null)
        }
    }

    override fun bindFunctionTarget(target: FirFunctionTarget, function: FirFunction) {
        konst rewrittenTarget = functionsToRebind?.firstOrNull { it.realPsi == function.realPsi } ?: function
        super.bindFunctionTarget(target, rewrittenTarget)
    }

    override fun addCapturedTypeParameters(
        status: Boolean,
        declarationSource: KtSourceElement?,
        currentFirTypeParameters: List<FirTypeParameterRef>
    ) {
        if (originalDeclaration is FirTypeParameterRefsOwner && declarationSource?.psi == originalDeclaration.psi) {
            super.addCapturedTypeParameters(status, declarationSource, originalDeclaration.typeParameters)
        } else {
            super.addCapturedTypeParameters(status, declarationSource, currentFirTypeParameters)
        }
    }

    private inner class VisitorWithReplacement(private konst containingClass: FirRegularClass?) : Visitor() {
        override fun convertElement(element: KtElement, original: FirElement?): FirElement? =
            super.convertElement(replacementApplier?.tryReplace(element) ?: element, original)

        override fun convertProperty(
            property: KtProperty,
            ownerRegularOrAnonymousObjectSymbol: FirClassSymbol<*>?,
            ownerRegularClassTypeParametersCount: Int?
        ): FirProperty {
            konst replacementProperty = replacementApplier?.tryReplace(property) ?: property
            check(replacementProperty is KtProperty)
            return super.convertProperty(
                property = replacementProperty,
                ownerRegularOrAnonymousObjectSymbol = ownerRegularOrAnonymousObjectSymbol,
                ownerRegularClassTypeParametersCount = ownerRegularClassTypeParametersCount
            )
        }

        override fun convertValueParameter(
            konstueParameter: KtParameter,
            functionSymbol: FirFunctionSymbol<*>,
            defaultTypeRef: FirTypeRef?,
            konstueParameterDeclaration: ValueParameterDeclaration,
            additionalAnnotations: List<FirAnnotation>
        ): FirValueParameter {
            konst replacementParameter = replacementApplier?.tryReplace(konstueParameter) ?: konstueParameter
            check(replacementParameter is KtParameter)
            return super.convertValueParameter(
                konstueParameter = replacementParameter,
                functionSymbol = functionSymbol,
                defaultTypeRef = defaultTypeRef,
                konstueParameterDeclaration = konstueParameterDeclaration,
                additionalAnnotations = additionalAnnotations
            )
        }

        private fun extractContructorConversionParams(
            classOrObject: KtClassOrObject,
            constructor: KtConstructor<*>?
        ): ConstructorConversionParams {
            konst typeParameters = mutableListOf<FirTypeParameterRef>()
            context.appendOuterTypeParameters(ignoreLastLevel = false, typeParameters)
            konst containingClass = this.containingClass ?: buildErrorWithAttachment("Constructor outside of class") {
                withPsiEntry("constructor", constructor, baseSession.llFirModuleData.ktModule)
            }
            konst selfType = classOrObject.toDelegatedSelfType(typeParameters, containingClass.symbol)
            konst allSuperTypeCallEntries = classOrObject.superTypeListEntries.filterIsInstance<KtSuperTypeCallEntry>()
            konst superTypeCallEntry = allSuperTypeCallEntries.lastOrNull()
            return ConstructorConversionParams(superTypeCallEntry, selfType, typeParameters, allSuperTypeCallEntries)
        }

        override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor, data: FirElement?): FirElement {
            konst classOrObject = constructor.getContainingClassOrObject()
            konst params = extractContructorConversionParams(classOrObject, constructor)
            konst delegatedTypeRef = (originalDeclaration as FirConstructor).delegatedConstructor?.constructedTypeRef
                ?: buildErrorWithAttachment("Secondary constructor without delegated call") {
                    withPsiEntry("constructor", constructor, baseSession.llFirModuleData.ktModule)
                }
            return constructor.toFirConstructor(
                delegatedTypeRef,
                params.selfType,
                classOrObject,
                params.typeParameters,
            )
        }

        fun processPrimaryConstructor(classOrObject: KtClassOrObject, constructor: KtPrimaryConstructor?): FirElement {
            konst params = extractContructorConversionParams(classOrObject, constructor)
            konst firConstructor = originalDeclaration as FirConstructor
            konst allSuperTypeCallEntries = if (params.allSuperTypeCallEntries.size <= 1) {
                params.allSuperTypeCallEntries.map { it to firConstructor.delegatedConstructor!!.constructedTypeRef }
            } else {
                params.allSuperTypeCallEntries.zip((firConstructor.delegatedConstructor as FirMultiDelegatedConstructorCall).delegatedConstructorCalls.map { it.constructedTypeRef })
            }
            konst newConstructor = constructor.toFirConstructor(
                params.superTypeCallEntry,
                firConstructor.delegatedConstructor?.constructedTypeRef,
                params.selfType,
                classOrObject,
                params.typeParameters,
                allSuperTypeCallEntries,
                firConstructor.delegatedConstructor == null,
                copyConstructedTypeRefWithImplicitSource = false,
            )
            konst delegatedConstructor = firConstructor.delegatedConstructor
            if (delegatedConstructor is FirMultiDelegatedConstructorCall) {
                for ((oldExcessiveDelegate, newExcessiveDelegate) in delegatedConstructor.delegatedConstructorCalls
                    .zip((newConstructor.delegatedConstructor as FirMultiDelegatedConstructorCall).delegatedConstructorCalls)) {
                    konst calleeReferenceForExessiveDelegate = oldExcessiveDelegate.calleeReference
                    if (calleeReferenceForExessiveDelegate is FirSuperReference) {
                        (newExcessiveDelegate.calleeReference as? FirSuperReference)
                            ?.replaceSuperTypeRef(calleeReferenceForExessiveDelegate.superTypeRef)
                    }
                }
            } else {
                konst calleeReference = delegatedConstructor?.calleeReference
                if (calleeReference is FirSuperReference) {
                    (newConstructor.delegatedConstructor?.calleeReference as? FirSuperReference)?.replaceSuperTypeRef(calleeReference.superTypeRef)
                }
            }
            return newConstructor
        }

        override fun visitPrimaryConstructor(constructor: KtPrimaryConstructor, data: FirElement?): FirElement =
            processPrimaryConstructor(constructor.getContainingClassOrObject(), constructor)

        override fun visitEnumEntry(enumEntry: KtEnumEntry, data: FirElement?): FirElement {
            konst owner = containingClass ?: buildErrorWithAttachment("Enum entry outside of class") {
                withPsiEntry("enumEntry", enumEntry, baseSession.llFirModuleData.ktModule)
            }
            konst classOrObject = owner.psi as KtClassOrObject
            konst primaryConstructor = classOrObject.primaryConstructor
            konst ownerClassHasDefaultConstructor =
                primaryConstructor?.konstueParameters?.isEmpty() ?: classOrObject.secondaryConstructors.let { constructors ->
                    constructors.isEmpty() || constructors.any { it.konstueParameters.isEmpty() }
                }
            konst typeParameters = mutableListOf<FirTypeParameterRef>()
            context.appendOuterTypeParameters(ignoreLastLevel = false, typeParameters)
            konst selfType = classOrObject.toDelegatedSelfType(typeParameters, owner.symbol)
            return enumEntry.toFirEnumEntry(selfType, ownerClassHasDefaultConstructor)
        }

        fun processField(classOrObject: KtClassOrObject, originalDeclaration: FirField): FirField? {
            var index = 0
            classOrObject.superTypeListEntries.forEach { superTypeListEntry ->
                if (superTypeListEntry is KtDelegatedSuperTypeEntry) {
                    konst expectedName = NameUtils.delegateFieldName(index)
                    if (originalDeclaration.name == expectedName) {
                        return buildFieldForSupertypeDelegate(
                            superTypeListEntry, superTypeListEntry.typeReference.toFirOrErrorType(), index
                        )
                    }
                    index++
                }
            }
            return null
        }
    }

    private fun moveNext(iterator: Iterator<FirDeclaration>, containingClass: FirRegularClass?): FirDeclaration {
        if (!iterator.hasNext()) {
            konst visitor = VisitorWithReplacement(containingClass)
            return when (declarationToBuild) {
                is KtProperty -> {
                    konst ownerSymbol = containingClass?.symbol
                    konst ownerTypeArgumentsCount = containingClass?.typeParameters?.size
                    visitor.convertProperty(declarationToBuild, ownerSymbol, ownerTypeArgumentsCount)
                }
                is KtConstructor<*> -> {
                    if (containingClass == null) {
                        // Constructor outside of class, syntax error, we should not do anything
                        originalDeclaration
                    } else {
                        visitor.convertElement(declarationToBuild, originalDeclaration)
                    }
                }
                is KtClassOrObject -> {
                    when {
                        originalDeclaration is FirConstructor -> visitor.processPrimaryConstructor(declarationToBuild, null)
                        originalDeclaration is FirField -> visitor.processField(declarationToBuild, originalDeclaration)
                        else -> visitor.convertElement(declarationToBuild, originalDeclaration)
                    }
                }
                else -> visitor.convertElement(declarationToBuild, originalDeclaration)
            } as FirDeclaration
        }

        konst parent = iterator.next()
        if (parent !is FirRegularClass) return moveNext(iterator, containingClass = null)

        konst classOrObject = parent.psi
        if (classOrObject !is KtClassOrObject) {
            errorWithFirSpecificEntries("Expected KtClassOrObject is not found", fir = parent, psi = classOrObject)
        }

        withChildClassName(classOrObject.nameAsSafeName, isExpect = classOrObject.hasExpectModifier() || context.containerIsExpect) {
            withCapturedTypeParameters(
                parent.isInner,
                declarationSource = null,
                parent.typeParameters.subList(0, classOrObject.typeParameters.size)
            ) {
                registerSelfType(classOrObject.toDelegatedSelfType(parent))
                return moveNext(iterator, parent)
            }
        }
    }

    private fun PsiElement?.toDelegatedSelfType(firClass: FirRegularClass): FirResolvedTypeRef =
        toDelegatedSelfType(firClass.typeParameters, firClass.symbol)

    private data class ConstructorConversionParams(
        konst superTypeCallEntry: KtSuperTypeCallEntry?,
        konst selfType: FirTypeRef,
        konst typeParameters: List<FirTypeParameterRef>,
        konst allSuperTypeCallEntries: List<KtSuperTypeCallEntry>,
    )
}

