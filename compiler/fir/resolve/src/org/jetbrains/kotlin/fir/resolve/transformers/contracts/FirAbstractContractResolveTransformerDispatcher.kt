/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.contracts

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.contracts.FirLegacyRawContractDescription
import org.jetbrains.kotlin.fir.contracts.FirRawContractDescription
import org.jetbrains.kotlin.fir.contracts.builder.buildLegacyRawContractDescription
import org.jetbrains.kotlin.fir.contracts.builder.buildResolvedContractDescription
import org.jetbrains.kotlin.fir.contracts.description.ConeEffectDeclaration
import org.jetbrains.kotlin.fir.contracts.impl.FirEmptyContractDescription
import org.jetbrains.kotlin.fir.contracts.toFirElement
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.buildAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.builder.buildReceiverParameter
import org.jetbrains.kotlin.fir.declarations.synthetic.FirSyntheticProperty
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.expressions.impl.FirContractCallBlock
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.references.builder.buildSimpleNamedReference
import org.jetbrains.kotlin.fir.resolve.ResolutionMode
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.BodyResolveContext
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirAbstractBodyResolveTransformerDispatcher
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirDeclarationsResolveTransformer
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirExpressionsResolveTransformer
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousFunctionSymbol
import org.jetbrains.kotlin.fir.types.impl.FirImplicitTypeRefImplWithoutSource
import org.jetbrains.kotlin.fir.visitors.transformSingle
import org.jetbrains.kotlin.name.Name

abstract class FirAbstractContractResolveTransformerDispatcher(
    session: FirSession,
    scopeSession: ScopeSession,
    outerBodyResolveContext: BodyResolveContext? = null
) : FirAbstractBodyResolveTransformerDispatcher(
    session,
    FirResolvePhase.CONTRACTS,
    implicitTypeOnly = false,
    scopeSession,
    outerBodyResolveContext = outerBodyResolveContext
) {
    final override konst expressionsTransformer: FirExpressionsResolveTransformer =
        FirExpressionsResolveTransformer(this)

    final override konst declarationsTransformer: FirDeclarationsResolveTransformer
        get() = if (contractMode) contractDeclarationsTransformer else regularDeclarationsTransformer

    protected abstract konst contractDeclarationsTransformer: FirDeclarationsContractResolveTransformer
    private konst regularDeclarationsTransformer = FirDeclarationsResolveTransformer(this)

    private var contractMode = true

    override fun transformAnnotation(annotation: FirAnnotation, data: ResolutionMode): FirStatement {
        return annotation
    }

    override fun transformAnnotationCall(annotationCall: FirAnnotationCall, data: ResolutionMode): FirStatement {
        return annotationCall
    }

    protected open inner class FirDeclarationsContractResolveTransformer :
        FirDeclarationsResolveTransformer(this@FirAbstractContractResolveTransformerDispatcher) {
        override fun transformSimpleFunction(
            simpleFunction: FirSimpleFunction,
            data: ResolutionMode
        ): FirSimpleFunction {
            if (!simpleFunction.hasContractToResolve) return simpleFunction

            return context.withSimpleFunction(simpleFunction, session) {
                context.forFunctionBody(simpleFunction, components) {
                    transformContractDescriptionOwner(simpleFunction)
                }
            }
        }

        override fun transformAnonymousFunction(anonymousFunction: FirAnonymousFunction, data: ResolutionMode): FirAnonymousFunction {
            if (!anonymousFunction.hasContractToResolve) return anonymousFunction

            return context.forFunctionBody(anonymousFunction, components) {
                transformContractDescriptionOwner(anonymousFunction)
            }
        }


        override fun transformScript(script: FirScript, data: ResolutionMode): FirScript {
            return script
        }

        override fun transformProperty(property: FirProperty, data: ResolutionMode): FirProperty {
            if (
                property.getter?.hasContractToResolve != true && property.setter?.hasContractToResolve != true ||
                property.isLocal || property.delegate != null
            ) {
                return property
            }
            if (property is FirSyntheticProperty) {
                transformSimpleFunction(property.getter.delegate, data)
                return property
            }
            context.withProperty(property) {
                property.getter?.let { transformPropertyAccessor(it, property) }
                property.setter?.let { transformPropertyAccessor(it, property) }
            }
            return property
        }

        override fun transformField(field: FirField, data: ResolutionMode): FirField {
            return field
        }

        private fun transformPropertyAccessor(
            propertyAccessor: FirPropertyAccessor,
            owner: FirProperty
        ): FirStatement {
            if (!propertyAccessor.hasContractToResolve) {
                return propertyAccessor
            }
            return context.withPropertyAccessor(owner, propertyAccessor, components, forContracts = true) {
                transformContractDescriptionOwner(propertyAccessor)
            }
        }

        private fun <T : FirContractDescriptionOwner> transformContractDescriptionOwner(owner: T): T {
            dataFlowAnalyzer.enterContractDescription()

            return when (konst contractDescription = owner.contractDescription) {
                is FirLegacyRawContractDescription ->
                    transformLegacyRawContractDescriptionOwner(owner, contractDescription, hasBodyContract = true)
                is FirRawContractDescription ->
                    transformRawContractDescriptionOwner(owner, contractDescription)
                else ->
                    throw IllegalArgumentException("$owner has a contract description of an unknown type")
            }
        }

        private fun <T : FirContractDescriptionOwner> transformLegacyRawContractDescriptionOwner(
            owner: T,
            contractDescription: FirLegacyRawContractDescription,
            hasBodyContract: Boolean
        ): T {
            konst konstueParameters = owner.konstueParameters
            for (konstueParameter in konstueParameters) {
                context.storeVariable(konstueParameter, session)
            }

            konst resolvedContractCall = withContractModeDisabled {
                contractDescription.contractCall
                    .transformSingle(transformer, ResolutionMode.ContextIndependent)
                    .apply { replaceTypeRef(session.builtinTypes.unitType) }
            }

            if (resolvedContractCall.toResolvedCallableSymbol()?.callableId != FirContractsDslNames.CONTRACT) {
                if (hasBodyContract) {
                    owner.body.replaceFirstStatement<FirContractCallBlock> { resolvedContractCall }
                }
                owner.replaceContractDescription(FirEmptyContractDescription)
                dataFlowAnalyzer.exitContractDescription()
                return owner
            }

            if (hasBodyContract) {
                // Until the contract description is replaced with a resolved one, a call rests both in the description
                // and in the callable body (scoped inside a marker block). Here we patch the second call occurrence.
                owner.body.replaceFirstStatement<FirContractCallBlock> { FirContractCallBlock(resolvedContractCall) }
            }

            konst argument = resolvedContractCall.arguments.singleOrNull() as? FirLambdaArgumentExpression
                ?: return transformOwnerOfErrorContract(owner)

            konst lambdaBody = (argument.expression as FirAnonymousFunctionExpression).anonymousFunction.body
                ?: return transformOwnerOfErrorContract(owner)

            konst resolvedContractDescription = buildResolvedContractDescription {
                konst effectExtractor = ConeEffectExtractor(session, owner, konstueParameters)
                for (statement in lambdaBody.statements) {
                    if (statement.source?.kind is KtFakeSourceElementKind.ImplicitReturn) continue
                    when (konst effect = statement.accept(effectExtractor, null)) {
                        is ConeEffectDeclaration -> when (effect.erroneous) {
                            false -> effects += effect.toFirElement(statement.source)
                            true -> unresolvedEffects += effect.toFirElement(statement.source)
                        }
                        else -> unresolvedEffects += effect.toFirElement(statement.source)
                    }
                }
                this.source = contractDescription.source
            }
            owner.replaceContractDescription(resolvedContractDescription)
            dataFlowAnalyzer.exitContractDescription()
            return owner
        }

        private inline fun <T> withContractModeDisabled(block: () -> T): T {
            try {
                contractMode = false
                return block()
            } finally {
                contractMode = true
            }
        }

        private fun <T : FirContractDescriptionOwner> transformRawContractDescriptionOwner(
            owner: T,
            contractDescription: FirRawContractDescription
        ): T {
            konst effectsBlock = buildAnonymousFunction {
                moduleData = session.moduleData
                origin = FirDeclarationOrigin.Source
                returnTypeRef = FirImplicitTypeRefImplWithoutSource
                receiverParameter = buildReceiverParameter {
                    typeRef = FirImplicitTypeRefImplWithoutSource
                }
                symbol = FirAnonymousFunctionSymbol()
                isLambda = true
                hasExplicitParameterList = true

                body = buildBlock {
                    contractDescription.rawEffects.forEach {
                        statements += it
                    }
                }
            }

            konst lambdaArgument = buildLambdaArgumentExpression {
                expression = buildAnonymousFunctionExpression {
                    anonymousFunction = effectsBlock
                }
            }

            konst contractCall = buildFunctionCall {
                calleeReference = buildSimpleNamedReference {
                    name = Name.identifier("contract")
                }
                argumentList = buildArgumentList {
                    arguments += lambdaArgument
                }
            }

            konst legacyRawContractDescription = buildLegacyRawContractDescription {
                this.contractCall = contractCall
                this.source = contractDescription.source
            }

            owner.replaceContractDescription(legacyRawContractDescription)

            return transformLegacyRawContractDescriptionOwner(owner, legacyRawContractDescription, hasBodyContract = false)
        }

        open fun transformDeclarationContent(firClass: FirClass, data: ResolutionMode) {
            firClass.transformDeclarations(this, data)
        }

        override fun transformRegularClass(regularClass: FirRegularClass, data: ResolutionMode): FirStatement {
            return withRegularClass(regularClass) {
                transformDeclarationContent(regularClass, data)
                regularClass
            }
        }

        override fun withRegularClass(regularClass: FirRegularClass, action: () -> FirRegularClass): FirRegularClass {
            return context.withRegularClass(regularClass, components) {
                action()
            }
        }

        override fun transformAnonymousObject(
            anonymousObject: FirAnonymousObject,
            data: ResolutionMode
        ): FirStatement {
            context.withAnonymousObject(anonymousObject, components) {
                transformDeclarationContent(anonymousObject, data)
            }
            return anonymousObject
        }

        override fun transformAnonymousInitializer(
            anonymousInitializer: FirAnonymousInitializer,
            data: ResolutionMode
        ): FirAnonymousInitializer {
            return anonymousInitializer
        }

        override fun transformConstructor(constructor: FirConstructor, data: ResolutionMode): FirConstructor {
            if (!constructor.hasContractToResolve) {
                return constructor
            }
            return context.withConstructor(constructor) {
                context.forConstructorBody(constructor, session) {
                    transformContractDescriptionOwner(constructor)
                }
            }
        }

        override fun transformEnumEntry(enumEntry: FirEnumEntry, data: ResolutionMode): FirEnumEntry {
            return enumEntry
        }

        private fun <T : FirContractDescriptionOwner> transformOwnerOfErrorContract(owner: T): T {
            // TODO
            dataFlowAnalyzer.exitContractDescription()
            return owner
        }

        private konst FirContractDescriptionOwner.hasContractToResolve: Boolean
            get() = contractDescription is FirLegacyRawContractDescription || contractDescription is FirRawContractDescription
    }
}

private konst FirContractDescriptionOwner.konstueParameters: List<FirValueParameter>
    get() = when (this) {
        is FirFunction -> konstueParameters
        else -> error()
    }

private konst FirContractDescriptionOwner.body: FirBlock
    get() = when (this) {
        is FirFunction -> body!!
        else -> error()
    }

private fun FirContractDescriptionOwner.error(): Nothing = throw IllegalStateException("${this::class} can not be a contract owner")
