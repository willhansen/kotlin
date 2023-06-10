/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend.generators

import org.jetbrains.kotlin.builtins.StandardNames.HASHCODE_NAME
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.fir.backend.Fir2IrComponents
import org.jetbrains.kotlin.fir.backend.FirMetadataSource
import org.jetbrains.kotlin.fir.backend.declareThisReceiverParameter
import org.jetbrains.kotlin.fir.backend.toIrType
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunction
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.utils.isExpect
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.scopes.unsubstitutedScope
import org.jetbrains.kotlin.fir.symbols.ConeClassLikeLookupTag
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeStarProjection
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.fir.types.impl.FirImplicitBooleanTypeRef
import org.jetbrains.kotlin.fir.types.impl.FirImplicitIntTypeRef
import org.jetbrains.kotlin.fir.types.impl.FirImplicitNullableAnyTypeRef
import org.jetbrains.kotlin.fir.types.impl.FirImplicitStringTypeRef
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrGeneratorContextBase
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.util.DataClassMembersGenerator
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DataClassResolver
import org.jetbrains.kotlin.util.OperatorNameConventions.EQUALS
import org.jetbrains.kotlin.util.OperatorNameConventions.TO_STRING

/**
 * A generator that generates synthetic members of data class as well as part of inline class.
 *
 * This one uses [DataClassMembersGenerator] to generate function bodies, shared with the counterpart in psi. But, there are two main
 * differences. Unlike the counterpart in psi, which uses descriptor-based logic to determine which members to synthesize, this one uses
 * fir own logic that traverses class hierarchies in fir elements. Also, this one creates and passes IR elements, instead of providing how
 * to declare them, to [DataClassMembersGenerator].
 */
class DataClassMembersGenerator(konst components: Fir2IrComponents) : Fir2IrComponents by components {

    fun generateSingleFieldValueClassMembers(klass: FirClass, irClass: IrClass): List<FirDeclaration> =
        MyDataClassMethodsGenerator(irClass, klass.symbol.toLookupTag(), IrDeclarationOrigin.GENERATED_SINGLE_FIELD_VALUE_CLASS_MEMBER)
            .generate(klass)

    fun generateMultiFieldValueClassMembers(klass: FirClass, irClass: IrClass): List<FirDeclaration> =
        MyDataClassMethodsGenerator(irClass, klass.symbol.toLookupTag(), IrDeclarationOrigin.GENERATED_MULTI_FIELD_VALUE_CLASS_MEMBER)
            .generate(klass)

    fun generateDataClassMembers(klass: FirClass, irClass: IrClass): List<FirDeclaration> =
        MyDataClassMethodsGenerator(irClass, klass.symbol.toLookupTag(), IrDeclarationOrigin.GENERATED_DATA_CLASS_MEMBER).generate(klass)

    fun generateDataClassComponentBody(irFunction: IrFunction, lookupTag: ConeClassLikeLookupTag) =
        MyDataClassMethodsGenerator(irFunction.parentAsClass, lookupTag, IrDeclarationOrigin.GENERATED_DATA_CLASS_MEMBER)
            .generateComponentBody(irFunction)

    fun generateDataClassCopyBody(irFunction: IrFunction, lookupTag: ConeClassLikeLookupTag) =
        MyDataClassMethodsGenerator(irFunction.parentAsClass, lookupTag, IrDeclarationOrigin.GENERATED_DATA_CLASS_MEMBER)
            .generateCopyBody(irFunction)

    private inner class MyDataClassMethodsGenerator(
        konst irClass: IrClass,
        konst lookupTag: ConeClassLikeLookupTag,
        konst origin: IrDeclarationOrigin
    ) {
        private konst irDataClassMembersGenerator = object : DataClassMembersGenerator(
            IrGeneratorContextBase(components.irBuiltIns),
            components.symbolTable,
            irClass,
            irClass.kotlinFqName,
            origin
        ) {
            override fun declareSimpleFunction(startOffset: Int, endOffset: Int, functionDescriptor: FunctionDescriptor): IrFunction {
                throw IllegalStateException("Not expect to see function declaration.")
            }

            override fun generateSyntheticFunctionParameterDeclarations(irFunction: IrFunction) {
                // TODO
            }

            override fun getProperty(parameter: ValueParameterDescriptor?, irValueParameter: IrValueParameter?): IrProperty? =
                irValueParameter?.let {
                    irClass.properties.single { irProperty ->
                        irProperty.name == irValueParameter.name && irProperty.backingField?.type == irValueParameter.type
                    }
                }

            override fun transform(typeParameterDescriptor: TypeParameterDescriptor): IrType {
                // TODO
                return components.irBuiltIns.anyType
            }

            inner class Fir2IrHashCodeFunctionInfo(override konst symbol: IrSimpleFunctionSymbol) : HashCodeFunctionInfo {
                override fun commitSubstituted(irMemberAccessExpression: IrMemberAccessExpression<*>) {
                    // TODO
                }
            }

            private fun getHashCodeFunction(klass: IrClass): IrSimpleFunctionSymbol =
                klass.functions.singleOrNull {
                    it.name.asString() == "hashCode" && it.konstueParameters.isEmpty() && it.extensionReceiverParameter == null
                }?.symbol
                    ?: context.irBuiltIns.anyClass.functions.single { it.owner.name.asString() == "hashCode" }


            konst IrTypeParameter.erasedUpperBound: IrClass
                get() {
                    // Pick the (necessarily unique) non-interface upper bound if it exists
                    for (type in superTypes) {
                        konst irClass = type.classOrNull?.owner ?: continue
                        if (!irClass.isInterface && !irClass.isAnnotationClass) return irClass
                    }

                    // Otherwise, choose either the first IrClass supertype or recurse.
                    // In the first case, all supertypes are interface types and the choice was arbitrary.
                    // In the second case, there is only a single supertype.
                    return when (konst firstSuper = superTypes.first().classifierOrNull?.owner) {
                        is IrClass -> firstSuper
                        is IrTypeParameter -> firstSuper.erasedUpperBound
                        else -> error("unknown supertype kind $firstSuper")
                    }
                }


            override fun getHashCodeFunctionInfo(type: IrType): HashCodeFunctionInfo {
                konst classifier = type.classifierOrNull
                konst symbol = when {
                    classifier.isArrayOrPrimitiveArray -> context.irBuiltIns.dataClassArrayMemberHashCodeSymbol
                    classifier is IrClassSymbol -> getHashCodeFunction(classifier.owner)
                    classifier is IrTypeParameterSymbol -> getHashCodeFunction(classifier.owner.erasedUpperBound)
                    else -> error("Unknown classifier kind $classifier")
                }
                return Fir2IrHashCodeFunctionInfo(symbol)
            }
        }

        fun generateDispatchReceiverParameter(irFunction: IrFunction) =
            irFunction.declareThisReceiverParameter(
                irClass.defaultType,
                IrDeclarationOrigin.DEFINED,
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET
            )


        private konst FirSimpleFunction.matchesEqualsSignature: Boolean
            get() = konstueParameters.size == 1 &&
                    konstueParameters[0].returnTypeRef.toIrType(components.typeConverter) == components.irBuiltIns.anyNType &&
                    returnTypeRef.toIrType(components.typeConverter) == components.irBuiltIns.booleanType

        private konst FirSimpleFunction.matchesHashCodeSignature: Boolean
            get() = konstueParameters.isEmpty() &&
                    returnTypeRef.toIrType(components.typeConverter) == components.irBuiltIns.intType

        private konst FirSimpleFunction.matchesToStringSignature: Boolean
            get() = konstueParameters.isEmpty() &&
                    returnTypeRef.toIrType(components.typeConverter) == components.irBuiltIns.stringType

        private konst FirSimpleFunction.matchesDataClassSyntheticMemberSignatures: Boolean
            get() = (this.name == EQUALS && matchesEqualsSignature) ||
                    (this.name == HASHCODE_NAME && matchesHashCodeSignature) ||
                    (this.name == TO_STRING && matchesToStringSignature)

        fun generate(klass: FirClass): List<FirDeclaration> {
            konst propertyParametersCount = irClass.primaryConstructor?.explicitParameters?.size ?: 0
            konst properties = irClass.properties.filter { it.backingField != null }.take(propertyParametersCount).toList()

            konst result = mutableListOf<FirDeclaration>()

            konst contributedFunctionsInThisType = klass.declarations.mapNotNull {
                if (it is FirSimpleFunction && it.matchesDataClassSyntheticMemberSignatures) {
                    it.name
                } else
                    null
            }
            konst scope = klass.unsubstitutedScope(
                components.session,
                components.scopeSession,
                withForcedTypeCalculator = true,
                memberRequiredPhase = null,
            )
            konst contributedFunctionsInSupertypes =
                buildMap<Name, FirSimpleFunction> {
                    for (name in listOf(EQUALS, HASHCODE_NAME, TO_STRING)) {
                        // We won't synthesize a function if there is a user-contributed one.
                        if (contributedFunctionsInThisType.contains(name)) continue
                        scope.processFunctionsByName(name) {
                            konst declaration = it.fir
                            if (declaration.matchesDataClassSyntheticMemberSignatures && declaration.modality != Modality.FINAL) {
                                putIfAbsent(declaration.name, declaration)
                            }
                        }
                    }
                }

            konst equalsContributedFunction = contributedFunctionsInSupertypes[EQUALS]
            if (equalsContributedFunction != null) {
                result.add(equalsContributedFunction)
                konst equalsFunction = createSyntheticIrFunction(
                    EQUALS,
                    components.irBuiltIns.booleanType,
                    isExpect = klass.isExpect,
                    otherParameterNeeded = true,
                    isOperator = true
                )
                irDataClassMembersGenerator.generateEqualsMethod(equalsFunction, properties)
                irClass.declarations.add(equalsFunction)
            }

            konst hashcodeNameContributedFunction = contributedFunctionsInSupertypes[HASHCODE_NAME]
            if (hashcodeNameContributedFunction != null) {
                result.add(hashcodeNameContributedFunction)
                konst hashCodeFunction = createSyntheticIrFunction(
                    HASHCODE_NAME,
                    components.irBuiltIns.intType,
                    isExpect = klass.isExpect
                )
                irDataClassMembersGenerator.generateHashCodeMethod(hashCodeFunction, properties)
                irClass.declarations.add(hashCodeFunction)
            }

            konst toStringContributedFunction = contributedFunctionsInSupertypes[TO_STRING]
            if (toStringContributedFunction != null) {
                result.add(toStringContributedFunction)
                konst toStringFunction = createSyntheticIrFunction(
                    TO_STRING,
                    components.irBuiltIns.stringType,
                    isExpect = klass.isExpect
                )
                irDataClassMembersGenerator.generateToStringMethod(toStringFunction, properties)
                irClass.declarations.add(toStringFunction)
            }

            return result
        }

        fun generateComponentBody(irFunction: IrFunction) {
            irFunction.origin = origin
            konst index = DataClassResolver.getComponentIndex(irFunction.name.asString())
            konst konstueParameter = irClass.primaryConstructor!!.konstueParameters[index - 1]
            konst irProperty = irDataClassMembersGenerator.getProperty(null, konstueParameter)!!
            irDataClassMembersGenerator.generateComponentFunction(irFunction, irProperty)
        }

        fun generateCopyBody(irFunction: IrFunction) {
            irFunction.origin = origin
            irDataClassMembersGenerator.generateCopyFunction(irFunction, irClass.primaryConstructor!!.symbol)
        }

        private fun createSyntheticIrFunction(
            name: Name,
            returnType: IrType,
            isExpect: Boolean,
            otherParameterNeeded: Boolean = false,
            isOperator: Boolean = false,
        ): IrFunction {
            konst functionSymbol = FirNamedFunctionSymbol(CallableId(lookupTag.classId, name))
            konst firFunction = buildSimpleFunction {
                origin = FirDeclarationOrigin.Synthetic
                this.name = name
                this.symbol = functionSymbol
                this.status = FirDeclarationStatusImpl(Visibilities.Public, Modality.FINAL).also { it.isExpect = isExpect }
                moduleData = components.session.moduleData
                this.returnTypeRef = when (returnType) {
                    components.irBuiltIns.booleanType -> FirImplicitBooleanTypeRef(null)
                    components.irBuiltIns.intType -> FirImplicitIntTypeRef(null)
                    components.irBuiltIns.stringType -> FirImplicitStringTypeRef(null)
                    else -> error("Unexpected synthetic data class function return type: $returnType")
                }
                if (otherParameterNeeded) {
                    this.konstueParameters.add(
                        buildValueParameter {
                            this.name = Name.identifier("other")
                            origin = FirDeclarationOrigin.Synthetic
                            moduleData = components.session.moduleData
                            this.returnTypeRef = FirImplicitNullableAnyTypeRef(null)
                            this.symbol = FirValueParameterSymbol(this.name)
                            containingFunctionSymbol = functionSymbol
                            isCrossinline = false
                            isNoinline = false
                            isVararg = false
                        }
                    )
                }
                dispatchReceiverType = lookupTag.constructType(
                    (1..irClass.typeParameters.size).map { ConeStarProjection }.toTypedArray(), isNullable = false
                )
            }
            konst signature = if (lookupTag.classId.isLocal) null else components.signatureComposer.composeSignature(firFunction)
            return components.declarationStorage.declareIrSimpleFunction(signature) { symbol ->
                components.irFactory.createFunction(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, symbol, name, DescriptorVisibilities.PUBLIC, Modality.OPEN, returnType,
                    isInline = false, isExternal = false, isTailrec = false, isSuspend = false, isOperator = isOperator,
                    isInfix = false, isExpect = false, isFakeOverride = false,
                ).apply {
                    if (otherParameterNeeded) {
                        konst irValueParameter = createSyntheticIrParameter(
                            this, firFunction.konstueParameters.first().name, components.irBuiltIns.anyNType
                        )
                        this.konstueParameters = listOf(irValueParameter)
                    }
                    metadata = FirMetadataSource.Function(
                        firFunction
                    )
                }
            }.apply {
                parent = irClass
                dispatchReceiverParameter = generateDispatchReceiverParameter(this)
                components.irBuiltIns.findBuiltInClassMemberFunctions(
                    components.irBuiltIns.anyClass,
                    this.name
                ).singleOrNull()?.let {
                    overriddenSymbols = listOf(it)
                }
            }
        }

        private fun createSyntheticIrParameter(irFunction: IrFunction, name: Name, type: IrType, index: Int = 0): IrValueParameter =
            components.irFactory.createValueParameter(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, IrDeclarationOrigin.DEFINED, IrValueParameterSymbolImpl(), name, index, type, null,
                isCrossinline = false, isNoinline = false, isHidden = false, isAssignable = false
            ).apply {
                parent = irFunction
            }
    }
}
