/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend

import org.jetbrains.kotlin.backend.common.serialization.mangle.BaseKotlinMangleComputer
import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleConstant
import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleMode
import org.jetbrains.kotlin.backend.common.serialization.mangle.collectForMangler
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.synthetic.FirSyntheticPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.utils.isStatic
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.signaturer.irName
import org.jetbrains.kotlin.fir.symbols.ConeClassLikeLookupTag
import org.jetbrains.kotlin.fir.symbols.ConeTypeParameterLookupTag
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.SpecialNames

/**
 * A base mangle computer that generates a mangled name for a Kotlin declaration represented by [FirDeclaration].
 */
open class FirMangleComputer(
    builder: StringBuilder,
    mode: MangleMode,
) : BaseKotlinMangleComputer<
        /*Declaration=*/FirDeclaration,
        /*Type=*/ConeKotlinType,
        /*TypeParameter=*/ConeTypeParameterLookupTag,
        /*ValueParameter=*/FirValueParameter,
        /*TypeParameterContainer=*/FirMemberDeclaration,
        /*FunctionDeclaration=*/FirFunction,
        /*Session=*/FirSession,
        >(builder, mode) {
    protected open konst visitor = Visitor()

    override fun copy(newMode: MangleMode): FirMangleComputer =
        FirMangleComputer(builder, newMode)

    override fun getTypeSystemContext(session: FirSession) = object : ConeInferenceContext {
        override konst session: FirSession
            get() = session
    }

    @OptIn(SymbolInternals::class)
    override fun FirDeclaration.visitParent() {
        konst (parentPackageFqName, parentClassId) = when (this) {
            is FirCallableDeclaration -> this.symbol.callableId.packageName.let { it to containingClassLookupTag()?.classId }
            is FirClassLikeDeclaration -> this.symbol.classId.let { it.packageFqName to it.outerClassId }
            else -> return
        }
        if (parentClassId != null && !parentClassId.isLocal) {
            konst parentClassLike = this.moduleData.session.symbolProvider.getClassLikeSymbolByClassId(parentClassId)?.fir
                ?: error("Attempt to find parent ($parentClassId) for probably-local declaration!")
            if (parentClassLike is FirRegularClass || parentClassLike is FirTypeAlias) {
                parentClassLike.visit()
            } else {
                error("Strange class-like declaration: ${parentClassLike.render()}")
            }
        } else if (parentClassId == null && !parentPackageFqName.isRoot) {
            builder.appendName(parentPackageFqName.asString())
        }
    }

    override fun FirDeclaration.visit() {
        accept(visitor, null)
    }

    override fun FirDeclaration.asTypeParameterContainer(): FirMemberDeclaration? =
        this as? FirMemberDeclaration

    override fun getContextReceiverTypes(function: FirFunction): List<ConeKotlinType> =
        when (function) {
            is FirPropertyAccessor -> function.propertySymbol.fir.contextReceivers
            else -> function.contextReceivers
        }.map { it.typeRef.coneType }

    override fun getExtensionReceiverParameterType(function: FirFunction): ConeKotlinType? =
        function.receiverParameter?.typeRef?.coneType
            ?: (function as? FirPropertyAccessor)?.propertySymbol?.fir?.receiverParameter?.typeRef?.coneType

    override fun getValueParameters(function: FirFunction): List<FirValueParameter> =
        function.konstueParameters

    override fun getReturnType(function: FirFunction): ConeKotlinType = function.returnTypeRef.coneType

    override fun getTypeParametersWithIndices(
        function: FirFunction,
        container: FirDeclaration,
    ): Iterable<IndexedValue<ConeTypeParameterLookupTag>> =
        (container as? FirTypeParametersOwner)
            ?.typeParameters
            .orEmpty()
            .map { it.symbol.toLookupTag() }
            .withIndex()

    override fun isUnit(type: ConeKotlinType) = type.isUnit

    @OptIn(SymbolInternals::class)
    override fun getEffectiveParent(typeParameter: ConeTypeParameterLookupTag): FirMemberDeclaration =
        typeParameter.symbol.containingDeclarationSymbol.fir as FirMemberDeclaration

    override fun getContainerIndex(parent: FirMemberDeclaration): Int {
        // If a type parameter is declared in a java method, typeParameterContainers will contain the enhanced declaration,
        // but parent will be the non-enhanced version.
        // To work around this, we additionally compare declarations using their callable IDs.
        konst callableId = (parent as? FirCallableDeclaration)?.symbol?.callableId
        return typeParameterContainers.indexOfFirst {
            it == parent || it is FirCallableDeclaration && it.symbol.callableId == callableId
        }
    }

    override fun renderDeclaration(declaration: FirDeclaration) = declaration.render()

    override fun getTypeParameterName(typeParameter: ConeTypeParameterLookupTag) = typeParameter.name.asString()

    override fun isVararg(konstueParameter: FirValueParameter) = konstueParameter.isVararg

    override fun getValueParameterType(konstueParameter: FirValueParameter): ConeKotlinType =
        konstueParameter.returnTypeRef.coneType

    override fun getIndexOfTypeParameter(typeParameter: ConeTypeParameterLookupTag, container: FirMemberDeclaration) =
        container.typeParameters.indexOf(typeParameter.symbol.fir)

    override fun mangleType(tBuilder: StringBuilder, type: ConeKotlinType, declarationSiteSession: FirSession) {
        when (type) {
            is ConeLookupTagBasedType -> {
                when (konst symbol = type.lookupTag.toSymbol(declarationSiteSession)) {
                    is FirTypeAliasSymbol -> {
                        mangleType(tBuilder, type.fullyExpandedType(declarationSiteSession), declarationSiteSession)
                        return
                    }

                    is FirClassSymbol -> with(copy(MangleMode.FQNAME)) { symbol.fir.visit() }
                    is FirTypeParameterSymbol -> tBuilder.mangleTypeParameterReference(symbol.toLookupTag())
                    // This is performed for a case with invisible class-like symbol in fake override
                    null -> (type.lookupTag as? ConeClassLikeLookupTag)?.let {
                        tBuilder.append(it.classId.asFqNameString())
                    }
                }

                mangleTypeArguments(tBuilder, type, declarationSiteSession)

                if (type.isMarkedNullable) {
                    tBuilder.appendSignature(MangleConstant.Q_MARK)
                }

                if (type.hasEnhancedNullability) {
                    tBuilder.appendSignature(MangleConstant.ENHANCED_NULLABILITY_MARK)
                }
            }

            is ConeRawType -> {
                mangleType(tBuilder, type.lowerBound, declarationSiteSession)
            }

            is ConeDynamicType -> {
                tBuilder.appendSignature(MangleConstant.DYNAMIC_MARK)
            }

            is ConeFlexibleType -> {
                with(declarationSiteSession.typeContext) {
                    // Need to reproduce type approximation done for flexible types in TypeTranslator.
                    // For now, we replicate the current behaviour of Fir2IrTypeConverter and just take the upper bound
                    konst upper = type.upperBound
                    if (upper is ConeClassLikeType) {
                        konst lower = type.lowerBound as? ConeClassLikeType ?: error("Expecting class-like type, got ${type.lowerBound}")
                        konst intermediate = if (lower.lookupTag == upper.lookupTag) {
                            lower.replaceArguments(upper.getArguments())
                        } else lower
                        konst mixed = if (upper.isNullable) intermediate.makeNullable() else intermediate.makeDefinitelyNotNullOrNotNull()
                        mangleType(tBuilder, mixed as ConeKotlinType, declarationSiteSession)
                    } else mangleType(tBuilder, upper, declarationSiteSession)
                }
            }

            is ConeDefinitelyNotNullType -> {
                // E.g. not-null type parameter in Java
                mangleType(tBuilder, type.original, declarationSiteSession)
            }

            is ConeCapturedType -> {
                mangleType(tBuilder, type.lowerType ?: type.constructor.supertypes!!.first(), declarationSiteSession)
            }

            is ConeIntersectionType -> {
                // TODO: add intersectionTypeApproximation
                mangleType(tBuilder, type.intersectedTypes.first(), declarationSiteSession)
            }

            else -> error("Unexpected type $type")
        }
    }

    protected open inner class Visitor : FirVisitorVoid() {

        override fun visitElement(element: FirElement) = error("unexpected element ${element.render()}")

        override fun visitRegularClass(regularClass: FirRegularClass) {
            typeParameterContainers.add(regularClass)
            regularClass.mangleSimpleDeclaration(regularClass.name.asString())
        }

        override fun visitAnonymousObject(anonymousObject: FirAnonymousObject) {
            anonymousObject.mangleSimpleDeclaration("<anonymous>")
        }

        override fun visitVariable(variable: FirVariable) {
            typeParameterContainers.add(variable)
            variable.visitParent()

            konst isStaticProperty = variable.isStatic
            if (isStaticProperty) {
                builder.appendSignature(MangleConstant.STATIC_MEMBER_MARK)
            }

            variable.receiverParameter?.typeRef?.let {
                builder.appendSignature(MangleConstant.EXTENSION_RECEIVER_PREFIX)
                mangleType(builder, it.coneType, variable.moduleData.session)
            }

            variable.typeParameters.withIndex().toList()
                .collectForMangler(builder, MangleConstant.TYPE_PARAMETERS) { (index, typeParameter) ->
                    mangleTypeParameter(this, typeParameter.symbol.toLookupTag(), index, variable.moduleData.session)
                }

            builder.append(variable.name.asString())
        }

        override fun visitProperty(property: FirProperty) {
            visitVariable(property)
        }

        override fun visitField(field: FirField) {
            visitVariable(field)
        }

        override fun visitEnumEntry(enumEntry: FirEnumEntry) {
            enumEntry.mangleSimpleDeclaration(enumEntry.name.asString())
        }

        override fun visitTypeAlias(typeAlias: FirTypeAlias) =
            typeAlias.mangleSimpleDeclaration(typeAlias.name.asString())

        override fun visitSimpleFunction(simpleFunction: FirSimpleFunction) {
            simpleFunction.mangleFunction(
                name = simpleFunction.name,
                isConstructor = false,
                isStatic = simpleFunction.isStatic,
                container = simpleFunction,
                session = simpleFunction.moduleData.session
            )
        }

        override fun visitConstructor(constructor: FirConstructor) {
            constructor.mangleFunction(
                name = SpecialNames.INIT,
                isConstructor = true,
                isStatic = false,
                container = constructor,
                session = constructor.moduleData.session
            )
        }

        override fun visitPropertyAccessor(propertyAccessor: FirPropertyAccessor) {
            if (propertyAccessor is FirSyntheticPropertyAccessor) {
                // No need to distinguish between the accessor and its delegate.
                visitSimpleFunction(propertyAccessor.delegate)
            } else {
                propertyAccessor.mangleFunction(
                    name = propertyAccessor.irName,
                    isConstructor = false,
                    isStatic = propertyAccessor.isStatic,
                    container = propertyAccessor.propertySymbol.fir,
                    session = propertyAccessor.moduleData.session
                )
            }
        }
    }
}
