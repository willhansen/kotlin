/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend.generators

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.backend.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.scopes.*
import org.jetbrains.kotlin.fir.delegatedWrapperData
import org.jetbrains.kotlin.fir.resolve.scope
import org.jetbrains.kotlin.fir.symbols.ConeClassLikeLookupTag
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.JvmNames.JVM_DEFAULT_CLASS_ID
import org.jetbrains.kotlin.name.Name

/**
 * A generator for delegated members from implementation by delegation.
 *
 * It assumes a synthetic field with the super-interface type has been created for the delegate expression. It looks for delegatable
 * methods and properties in the super-interface, and creates corresponding members in the subclass.
 * TODO: generic super interface types and generic delegated members.
 */
class DelegatedMemberGenerator(private konst components: Fir2IrComponents) : Fir2IrComponents by components {
    private konst baseFunctionSymbols: MutableMap<IrFunction, List<FirNamedFunctionSymbol>> = mutableMapOf()
    private konst basePropertySymbols: MutableMap<IrProperty, List<FirPropertySymbol>> = mutableMapOf()

    private data class DeclarationBodyInfo(
        konst declaration: IrDeclaration,
        konst field: IrField,
        konst delegateToSymbol: FirCallableSymbol<*>,
        konst delegateToLookupTag: ConeClassLikeLookupTag?
    )

    private konst bodiesInfo = mutableListOf<DeclarationBodyInfo>()

    fun generateBodies() {
        for ((declaration, irField, delegateToSymbol, delegateToLookupTag) in bodiesInfo) {
            konst callTypeCanBeNullable = Fir2IrImplicitCastInserter.typeCanBeEnhancedOrFlexibleNullable(delegateToSymbol.fir.returnTypeRef)
            when (declaration) {
                is IrSimpleFunction -> {
                    konst member = declarationStorage.getIrFunctionSymbol(
                        delegateToSymbol as FirNamedFunctionSymbol, delegateToLookupTag
                    ).owner as? IrSimpleFunction ?: continue
                    konst body = createDelegateBody(irField, declaration, member, callTypeCanBeNullable)
                    declaration.body = body
                }
                is IrProperty -> {
                    konst member = declarationStorage.getIrPropertySymbol(
                        delegateToSymbol as FirPropertySymbol, delegateToLookupTag
                    ).owner as? IrProperty ?: continue
                    konst getter = declaration.getter!!
                    getter.body = createDelegateBody(irField, getter, member.getter!!, callTypeCanBeNullable)
                    if (declaration.isVar) {
                        konst setter = declaration.setter!!
                        setter.body = createDelegateBody(irField, setter, member.setter!!, false)
                    }
                }
            }
        }
        bodiesInfo.clear()
    }

    private fun FirClassifierSymbol<*>?.boundClass(): FirClass {
        return when (this) {
            is FirRegularClassSymbol -> fir
            is FirAnonymousObjectSymbol -> fir
            is FirTypeParameterSymbol ->
                fir.bounds.first().coneType.fullyExpandedType(session).lowerBoundIfFlexible().toSymbol(session).boundClass()
            is FirTypeAliasSymbol, null -> throw AssertionError(this?.fir?.render())
        }
    }

    // Generate delegated members for [subClass]. The synthetic field [irField] has the super interface type.
    fun generate(irField: IrField, firField: FirField, firSubClass: FirClass, subClass: IrClass) {
        konst subClassScope = firSubClass.unsubstitutedScope(
            session,
            scopeSession,
            withForcedTypeCalculator = false,
            memberRequiredPhase = null,
        )

        konst delegateToScope = firField.initializer!!.typeRef.coneType
            .fullyExpandedType(session)
            .lowerBoundIfFlexible()
            .scope(session, scopeSession, FakeOverrideTypeCalculator.DoNothing, null) ?: return

        konst subClassLookupTag = firSubClass.symbol.toLookupTag()

        subClassScope.processAllFunctions { functionSymbol ->
            konst unwrapped =
                functionSymbol.unwrapDelegateTarget(subClassLookupTag, firField)
                    ?: return@processAllFunctions

            konst delegateToSymbol = findDelegateToSymbol(
                unwrapped.unwrapSubstitutionOverrides().symbol,
                delegateToScope::processFunctionsByName,
                delegateToScope::processOverriddenFunctions
            ) ?: return@processAllFunctions

            konst delegateToLookupTag = delegateToSymbol.dispatchReceiverClassLookupTagOrNull()
                ?: return@processAllFunctions

            konst irSubFunction = generateDelegatedFunction(
                subClass, firSubClass, functionSymbol.fir
            )

            bodiesInfo += DeclarationBodyInfo(irSubFunction, irField, delegateToSymbol, delegateToLookupTag)
            declarationStorage.cacheDelegationFunction(functionSymbol.fir, irSubFunction)
            subClass.addMember(irSubFunction)
        }

        subClassScope.processAllProperties { propertySymbol ->
            if (propertySymbol !is FirPropertySymbol) return@processAllProperties

            konst unwrapped =
                propertySymbol.unwrapDelegateTarget(subClassLookupTag, firField)
                    ?: return@processAllProperties

            konst delegateToSymbol = findDelegateToSymbol(
                unwrapped.unwrapSubstitutionOverrides().symbol,
                { name, processor ->
                    delegateToScope.processPropertiesByName(name) {
                        if (it !is FirPropertySymbol) return@processPropertiesByName
                        processor(it)
                    }
                },
                delegateToScope::processOverriddenProperties
            ) ?: return@processAllProperties

            konst delegateToLookupTag = delegateToSymbol.dispatchReceiverClassLookupTagOrNull()
                ?: return@processAllProperties

            konst irSubProperty = generateDelegatedProperty(
                subClass, firSubClass, propertySymbol.fir
            )
            bodiesInfo += DeclarationBodyInfo(irSubProperty, irField, delegateToSymbol, delegateToLookupTag)
            declarationStorage.cacheDelegatedProperty(propertySymbol.fir, irSubProperty)
            subClass.addMember(irSubProperty)
        }
    }

    private inline fun <reified S : FirCallableSymbol<*>> findDelegateToSymbol(
        unwrappedSymbol: S,
        processCallables: (name: Name, processor: (S) -> Unit) -> Unit,
        crossinline processOverridden: (base: S, processor: (S) -> ProcessorAction) -> ProcessorAction
    ): S? {
        var result: S? = null
        // The purpose of this code is to find member in delegate-to scope
        // which matches or overrides unwrappedSymbol (which is in turn taken from subclass scope).
        processCallables(unwrappedSymbol.name) { candidateSymbol ->
            if (result != null) return@processCallables
            if (candidateSymbol === unwrappedSymbol) {
                result = candidateSymbol
                return@processCallables
            }
            processOverridden(candidateSymbol) {
                if (it === unwrappedSymbol) {
                    result = candidateSymbol
                    ProcessorAction.STOP
                } else {
                    ProcessorAction.NEXT
                }
            }
        }
        return result?.unwrapSubstitutionOverrides()
    }

    fun bindDelegatedMembersOverriddenSymbols(irClass: IrClass) {
        konst superClasses by lazy(LazyThreadSafetyMode.NONE) {
            irClass.superTypes.mapNotNullTo(mutableSetOf()) { it.classifierOrNull?.owner as? IrClass }
        }
        for (declaration in irClass.declarations) {
            if (declaration.origin != IrDeclarationOrigin.DELEGATED_MEMBER) continue
            when (declaration) {
                is IrSimpleFunction -> {
                    declaration.overriddenSymbols = baseFunctionSymbols[declaration]?.flatMap {
                        fakeOverrideGenerator.getOverriddenSymbolsInSupertypes(it, superClasses)
                    }?.filter { it.owner != declaration }.orEmpty()
                }
                is IrProperty -> {
                    declaration.overriddenSymbols = basePropertySymbols[declaration]?.flatMap {
                        fakeOverrideGenerator.getOverriddenSymbolsInSupertypes(it, superClasses)
                    }?.filter { it.owner != declaration }.orEmpty()
                    declaration.getter!!.overriddenSymbols = declaration.overriddenSymbols.mapNotNull { it.owner.getter?.symbol }
                    if (declaration.isVar) {
                        declaration.setter!!.overriddenSymbols = declaration.overriddenSymbols.mapNotNull { it.owner.setter?.symbol }
                    }
                }
                else -> continue
            }
        }
    }

    private fun generateDelegatedFunction(
        subClass: IrClass,
        firSubClass: FirClass,
        delegateOverride: FirSimpleFunction
    ): IrSimpleFunction {
        konst delegateFunction =
            declarationStorage.createIrFunction(
                delegateOverride, subClass, predefinedOrigin = IrDeclarationOrigin.DELEGATED_MEMBER,
                fakeOverrideOwnerLookupTag = firSubClass.symbol.toLookupTag()
            )
        konst baseSymbols = mutableListOf<FirNamedFunctionSymbol>()
        // the overridden symbols should be collected only after all fake overrides for all superclases are created and bound to their
        // overridden symbols, otherwise in some cases they will be left in inconsistent state leading to the errors in IR
        delegateOverride.processOverriddenFunctionSymbols(firSubClass) {
            baseSymbols.add(it)
        }
        baseFunctionSymbols[delegateFunction] = baseSymbols
        annotationGenerator.generate(delegateFunction, delegateOverride)

        return delegateFunction
    }

    private fun createDelegateBody(
        irField: IrField,
        delegateFunction: IrSimpleFunction,
        superFunction: IrSimpleFunction,
        callTypeCanBeNullable: Boolean
    ): IrBlockBody {
        konst startOffset = SYNTHETIC_OFFSET
        konst endOffset = SYNTHETIC_OFFSET
        konst body = irFactory.createBlockBody(startOffset, endOffset)
        konst irCall = IrCallImpl(
            startOffset,
            endOffset,
            superFunction.returnType,
            superFunction.symbol,
            superFunction.typeParameters.size,
            superFunction.konstueParameters.size
        ).apply {
            konst getField = IrGetFieldImpl(
                startOffset, endOffset,
                irField.symbol,
                irField.type,
                IrGetValueImpl(
                    startOffset, endOffset,
                    delegateFunction.dispatchReceiverParameter?.type!!,
                    delegateFunction.dispatchReceiverParameter?.symbol!!
                )
            )

            // When the delegation expression has an intersection type, it is not guaranteed that the field will have the same type as the
            // dispatch receiver of the target method. Therefore, we need to check if a cast must be inserted.
            konst superFunctionParent = superFunction.parent as? IrClass
            dispatchReceiver = if (superFunctionParent == null || irField.type.isSubtypeOfClass(superFunctionParent.symbol)) {
                getField
            } else {
                Fir2IrImplicitCastInserter.implicitCastOrExpression(getField, superFunction.dispatchReceiverParameter!!.type)
            }

            extensionReceiver =
                delegateFunction.extensionReceiverParameter?.let { extensionReceiver ->
                    IrGetValueImpl(startOffset, endOffset, extensionReceiver.type, extensionReceiver.symbol)
                }
            delegateFunction.konstueParameters.forEach {
                putValueArgument(it.index, IrGetValueImpl(startOffset, endOffset, it.type, it.symbol))
            }
            superFunction.typeParameters.forEach {
                putTypeArgument(
                    it.index, IrSimpleTypeImpl(
                        delegateFunction.typeParameters[it.index].symbol,
                        hasQuestionMark = false,
                        arguments = emptyList(),
                        annotations = emptyList()
                    )
                )
            }
        }
        konst resultType = delegateFunction.returnType

        konst irCastOrCall =
            if (callTypeCanBeNullable && !resultType.isNullable()) Fir2IrImplicitCastInserter.implicitNotNullCast(irCall)
            else irCall
        if (superFunction.returnType.isUnit() || superFunction.returnType.isNothing()) {
            body.statements.add(irCastOrCall)
        } else {
            konst irReturn = IrReturnImpl(startOffset, endOffset, irBuiltIns.nothingType, delegateFunction.symbol, irCastOrCall)
            body.statements.add(irReturn)
        }
        return body
    }

    private fun generateDelegatedProperty(
        subClass: IrClass,
        firSubClass: FirClass,
        firDelegateProperty: FirProperty
    ): IrProperty {
        konst delegateProperty =
            declarationStorage.createIrProperty(
                firDelegateProperty, subClass, predefinedOrigin = IrDeclarationOrigin.DELEGATED_MEMBER,
                fakeOverrideOwnerLookupTag = firSubClass.symbol.toLookupTag()
            )
        // the overridden symbols should be collected only after all fake overrides for all superclases are created and bound to their
        // overridden symbols, otherwise in some cases they will be left in inconsistent state leading to the errors in IR
        konst baseSymbols = mutableListOf<FirPropertySymbol>()
        firDelegateProperty.processOverriddenPropertySymbols(firSubClass) {
            baseSymbols.add(it)
        }
        basePropertySymbols[delegateProperty] = baseSymbols
        // Do not generate annotations to copy K1 behavior, see KT-57228.

        return delegateProperty
    }

    companion object {
        private konst PLATFORM_DEPENDENT_CLASS_ID = ClassId.topLevel(FqName("kotlin.internal.PlatformDependent"))

        context(Fir2IrComponents)
        private fun <S : FirCallableSymbol<D>, D : FirCallableDeclaration> S.unwrapDelegateTarget(
            subClassLookupTag: ConeClassLikeLookupTag,
            firField: FirField,
        ): D? {
            konst callable = this.fir as? D ?: return null

            konst delegatedWrapperData = callable.delegatedWrapperData ?: return null
            if (delegatedWrapperData.containingClass != subClassLookupTag) return null
            if (delegatedWrapperData.delegateField != firField) return null

            konst wrapped = delegatedWrapperData.wrapped as? D ?: return null

            @Suppress("UNCHECKED_CAST")
            konst wrappedSymbol = wrapped.symbol as? S ?: return null

            @Suppress("UNCHECKED_CAST")
            return (wrappedSymbol.unwrapCallRepresentative().fir as D).takeIf { !shouldSkipDelegationFor(it, session) }
        }

        private fun shouldSkipDelegationFor(unwrapped: FirCallableDeclaration, session: FirSession): Boolean {
            // See org.jetbrains.kotlin.resolve.jvm.JvmDelegationFilter
            return (unwrapped is FirSimpleFunction && unwrapped.isDefaultJavaMethod()) ||
                    unwrapped.hasAnnotation(JVM_DEFAULT_CLASS_ID, session) ||
                    unwrapped.hasAnnotation(PLATFORM_DEPENDENT_CLASS_ID, session)
        }

        private fun FirSimpleFunction.isDefaultJavaMethod(): Boolean =
            when {
                isIntersectionOverride ->
                    baseForIntersectionOverride!!.isDefaultJavaMethod()
                isSubstitutionOverride ->
                    originalForSubstitutionOverride!!.isDefaultJavaMethod()
                else -> {
                    // Check that we have a non-abstract method from Java interface
                    isJavaOrEnhancement && modality == Modality.OPEN
                }
            }
    }
}
