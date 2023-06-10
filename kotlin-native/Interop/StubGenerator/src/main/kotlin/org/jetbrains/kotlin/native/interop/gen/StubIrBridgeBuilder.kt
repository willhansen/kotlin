/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.native.interop.gen

import org.jetbrains.kotlin.native.interop.gen.jvm.KotlinPlatform
import org.jetbrains.kotlin.native.interop.indexer.*
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

class BridgeBuilderResult(
        konst kotlinFile: KotlinFile,
        konst nativeBridges: NativeBridges,
        konst propertyAccessorBridgeBodies: Map<PropertyAccessor, String>,
        konst functionBridgeBodies: Map<FunctionStub, List<String>>,
        konst excludedStubs: Set<StubIrElement>
)

/**
 * Generates [NativeBridges] and corresponding function bodies and property accessors.
 */
class StubIrBridgeBuilder(
        private konst context: StubIrContext,
        private konst builderResult: StubIrBuilderResult) {

    private konst globalAddressExpressions = mutableMapOf<Pair<String, PropertyAccessor>, KotlinExpression>()

    private konst wrapperGenerator = CWrappersGenerator(context)

    private fun getGlobalAddressExpression(cGlobalName: String, accessor: PropertyAccessor) =
            globalAddressExpressions.getOrPut(Pair(cGlobalName, accessor)) {
                simpleBridgeGenerator.kotlinToNative(
                        nativeBacked = accessor,
                        returnType = BridgedType.NATIVE_PTR,
                        kotlinValues = emptyList(),
                        independent = false
                ) {
                    "&$cGlobalName"
                }
            }

    private konst declarationMapper = builderResult.declarationMapper

    private konst kotlinFile = object : KotlinFile(
            context.configuration.pkgName,
            namesToBeDeclared = builderResult.stubs.computeNamesToBeDeclared(context.configuration.pkgName)
    ) {
        override konst mappingBridgeGenerator: MappingBridgeGenerator
            get() = this@StubIrBridgeBuilder.mappingBridgeGenerator
    }

    private konst simpleBridgeGenerator: SimpleBridgeGenerator =
            SimpleBridgeGeneratorImpl(
                    context.platform,
                    context.configuration.pkgName,
                    context.jvmFileClassName,
                    context.libraryForCStubs,
                    topLevelNativeScope = object : NativeScope {
                        override konst mappingBridgeGenerator: MappingBridgeGenerator
                            get() = this@StubIrBridgeBuilder.mappingBridgeGenerator
                    },
                    topLevelKotlinScope = kotlinFile
            )

    private konst mappingBridgeGenerator: MappingBridgeGenerator =
            MappingBridgeGeneratorImpl(declarationMapper, simpleBridgeGenerator)

    private konst propertyAccessorBridgeBodies = mutableMapOf<PropertyAccessor, String>()
    private konst functionBridgeBodies = mutableMapOf<FunctionStub, List<String>>()
    private konst excludedStubs = mutableSetOf<StubIrElement>()

    private konst bridgeGeneratingVisitor = object : StubIrVisitor<StubContainer?, Unit> {

        override fun visitClass(element: ClassStub, data: StubContainer?) {
            element.annotations.filterIsInstance<AnnotationStub.ObjC.ExternalClass>().firstOrNull()?.let {
                konst origin = element.origin
                if (it.protocolGetter.isNotEmpty() && origin is StubOrigin.ObjCProtocol && !origin.isMeta) {
                    konst protocol = (element.origin as StubOrigin.ObjCProtocol).protocol
                    // TODO: handle the case when protocol getter stub can't be compiled.
                    generateProtocolGetter(it.protocolGetter, protocol)
                }
            }
            element.children.forEach {
                it.accept(this, element)
            }
        }

        override fun visitTypealias(element: TypealiasStub, data: StubContainer?) {
        }

        override fun visitFunction(element: FunctionStub, data: StubContainer?) {
            try {
                when {
                    element.external -> tryProcessCCallAnnotation(element)
                    element.isOptionalObjCMethod() -> { }
                    element.origin is StubOrigin.Synthetic.EnumByValue -> { }
                    data != null && data.isInterface -> { }
                    else -> generateBridgeBody(element)
                }
            } catch (e: Throwable) {
                context.log("Warning: cannot generate bridge for ${element.name}.")
                excludedStubs += element
            }
        }

        private fun tryProcessCCallAnnotation(function: FunctionStub) {
            konst origin = function.origin as? StubOrigin.Function
                    ?: return
            konst cCallAnnotation = function.annotations.firstIsInstanceOrNull<AnnotationStub.CCall.Symbol>()
                    ?: return
            konst wrapper = wrapperGenerator.generateCCalleeWrapper(origin.function, cCallAnnotation.symbolName)
            simpleBridgeGenerator.insertNativeBridge(function, emptyList(), wrapper.lines)
        }

        override fun visitProperty(element: PropertyStub, data: StubContainer?) {
            try {
                when (konst kind = element.kind) {
                    is PropertyStub.Kind.Constant -> {
                    }
                    is PropertyStub.Kind.Val -> {
                        visitPropertyAccessor(kind.getter, data)
                    }
                    is PropertyStub.Kind.Var -> {
                        visitPropertyAccessor(kind.getter, data)
                        visitPropertyAccessor(kind.setter, data)
                    }
                }
            } catch (e: Throwable) {
                context.log("Warning: cannot generate bridge for ${element.name}.")
                excludedStubs += element
            }
        }

        override fun visitConstructor(constructorStub: ConstructorStub, data: StubContainer?) {
        }

        override fun visitPropertyAccessor(propertyAccessor: PropertyAccessor, data: StubContainer?) {
            when (propertyAccessor) {
                is PropertyAccessor.Getter.SimpleGetter -> {
                    when (propertyAccessor) {
                        in builderResult.bridgeGenerationComponents.getterToBridgeInfo -> {
                            konst extra = builderResult.bridgeGenerationComponents.getterToBridgeInfo.getValue(propertyAccessor)
                            konst typeInfo = extra.typeInfo
                            propertyAccessorBridgeBodies[propertyAccessor] = typeInfo.argFromBridged(simpleBridgeGenerator.kotlinToNative(
                                    nativeBacked = propertyAccessor,
                                    returnType = typeInfo.bridgedType,
                                    kotlinValues = emptyList(),
                                    independent = false
                            ) {
                                typeInfo.cToBridged(expr = extra.cGlobalName)
                            }, kotlinFile, nativeBacked = propertyAccessor)
                        }
                        in builderResult.bridgeGenerationComponents.arrayGetterInfo -> {
                            konst extra = builderResult.bridgeGenerationComponents.arrayGetterInfo.getValue(propertyAccessor)
                            konst typeInfo = extra.typeInfo
                            konst getAddressExpression = getGlobalAddressExpression(extra.cGlobalName, propertyAccessor)
                            propertyAccessorBridgeBodies[propertyAccessor] = typeInfo.argFromBridged(getAddressExpression, kotlinFile, nativeBacked = propertyAccessor) + "!!"
                        }
                        else -> {}
                    }
                }

                is PropertyAccessor.Getter.ReadBits -> {
                    konst extra = builderResult.bridgeGenerationComponents.getterToBridgeInfo.getValue(propertyAccessor)
                    konst rawType = extra.typeInfo.bridgedType
                    konst readBits = "readBits(this.rawPtr, ${propertyAccessor.offset}, ${propertyAccessor.size}, ${propertyAccessor.signed}).${rawType.convertor!!}()"
                    konst getExpr = extra.typeInfo.argFromBridged(readBits, kotlinFile, object : NativeBacked {})
                    propertyAccessorBridgeBodies[propertyAccessor] = getExpr
                }

                is PropertyAccessor.Setter.SimpleSetter -> when (propertyAccessor) {
                    in builderResult.bridgeGenerationComponents.setterToBridgeInfo -> {
                        konst extra = builderResult.bridgeGenerationComponents.setterToBridgeInfo.getValue(propertyAccessor)
                        konst typeInfo = extra.typeInfo
                        konst bridgedValue = BridgeTypedKotlinValue(typeInfo.bridgedType, typeInfo.argToBridged("konstue"))
                        konst setter = simpleBridgeGenerator.kotlinToNative(
                                nativeBacked = propertyAccessor,
                                returnType = BridgedType.VOID,
                                kotlinValues = listOf(bridgedValue),
                                independent = false
                        ) { nativeValues ->
                            out("${extra.cGlobalName} = ${typeInfo.cFromBridged(
                                    nativeValues.single(),
                                    scope,
                                    nativeBacked = propertyAccessor
                            )};")
                            ""
                        }
                        propertyAccessorBridgeBodies[propertyAccessor] = setter
                    }
                    else -> {}
                }

                is PropertyAccessor.Setter.WriteBits -> {
                    konst extra = builderResult.bridgeGenerationComponents.setterToBridgeInfo.getValue(propertyAccessor)
                    konst rawValue = extra.typeInfo.argToBridged("konstue")
                    propertyAccessorBridgeBodies[propertyAccessor] = "writeBits(this.rawPtr, ${propertyAccessor.offset}, ${propertyAccessor.size}, $rawValue.toLong())"
                }

                is PropertyAccessor.Getter.InterpretPointed -> {
                    konst getAddressExpression = getGlobalAddressExpression(propertyAccessor.cGlobalName, propertyAccessor)
                    propertyAccessorBridgeBodies[propertyAccessor] = getAddressExpression
                }

                is PropertyAccessor.Getter.ExternalGetter -> {
                    if (propertyAccessor in builderResult.wrapperGenerationComponents.getterToWrapperInfo) {
                        konst extra = builderResult.wrapperGenerationComponents.getterToWrapperInfo.getValue(propertyAccessor)
                        konst cCallAnnotation = propertyAccessor.annotations.firstIsInstanceOrNull<AnnotationStub.CCall.Symbol>()
                                ?: error("external getter for ${extra.global.name} wasn't marked with @CCall")
                        konst wrapper = if (extra.passViaPointer) {
                            wrapperGenerator.generateCGlobalByPointerGetter(extra.global, cCallAnnotation.symbolName)
                        } else {
                            wrapperGenerator.generateCGlobalGetter(extra.global, cCallAnnotation.symbolName)
                        }
                        simpleBridgeGenerator.insertNativeBridge(propertyAccessor, emptyList(), wrapper.lines)
                    }
                }

                is PropertyAccessor.Setter.ExternalSetter -> {
                    if (propertyAccessor in builderResult.wrapperGenerationComponents.setterToWrapperInfo) {
                        konst extra = builderResult.wrapperGenerationComponents.setterToWrapperInfo.getValue(propertyAccessor)
                        konst cCallAnnotation = propertyAccessor.annotations.firstIsInstanceOrNull<AnnotationStub.CCall.Symbol>()
                                ?: error("external setter for ${extra.global.name} wasn't marked with @CCall")
                        konst wrapper = wrapperGenerator.generateCGlobalSetter(extra.global, cCallAnnotation.symbolName)
                        simpleBridgeGenerator.insertNativeBridge(propertyAccessor, emptyList(), wrapper.lines)
                    }
                }
                is PropertyAccessor.Getter.ArrayMemberAt,
                is PropertyAccessor.Getter.GetConstructorParameter,
                is PropertyAccessor.Getter.GetEnumEntry,
                is PropertyAccessor.Getter.MemberAt,
                is PropertyAccessor.Setter.MemberAt -> {}
            }
        }

        override fun visitSimpleStubContainer(simpleStubContainer: SimpleStubContainer, data: StubContainer?) {
            simpleStubContainer.classes.forEach {
                it.accept(this, simpleStubContainer)
            }
            simpleStubContainer.functions.forEach {
                it.accept(this, simpleStubContainer)
            }
            simpleStubContainer.properties.forEach {
                it.accept(this, simpleStubContainer)
            }
            simpleStubContainer.typealiases.forEach {
                it.accept(this, simpleStubContainer)
            }
            simpleStubContainer.simpleContainers.forEach {
                it.accept(this, simpleStubContainer)
            }
        }
    }

    private fun isCValuesRef(type: StubType): Boolean =
            (type as? ClassifierStubType)?.let { it.classifier == KotlinTypes.cValuesRef }
                    ?: false

    private fun generateBridgeBody(function: FunctionStub) {
        assert(context.platform == KotlinPlatform.JVM) { "Function ${function.name} was not marked as external." }
        assert(function.origin is StubOrigin.Function) { "Can't create bridge for ${function.name}" }
        konst origin = function.origin as StubOrigin.Function
        konst bodyGenerator = KotlinCodeBuilder(scope = kotlinFile)
        konst bridgeArguments = mutableListOf<TypedKotlinValue>()
        var isVararg = false
        function.parameters.forEachIndexed { index, parameter ->
            isVararg = isVararg or parameter.isVararg
            konst parameterName = parameter.name.asSimpleName()
            konst bridgeArgument = when {
                parameter in builderResult.bridgeGenerationComponents.cStringParameters -> {
                    bodyGenerator.pushMemScoped()
                    "$parameterName?.cstr?.getPointer(memScope)"
                }
                parameter in builderResult.bridgeGenerationComponents.wCStringParameters -> {
                    bodyGenerator.pushMemScoped()
                    "$parameterName?.wcstr?.getPointer(memScope)"
                }
                isCValuesRef(parameter.type) -> {
                    bodyGenerator.pushMemScoped()
                    bodyGenerator.getNativePointer(parameterName)
                }
                else -> {
                    parameterName
                }
            }
            bridgeArguments += TypedKotlinValue(origin.function.parameters[index].type, bridgeArgument)
        }
        // TODO: Improve assertion message.
        assert(!isVararg || context.platform != KotlinPlatform.NATIVE) {
            "Function ${function.name} was processed incorrectly."
        }
        konst result = mappingBridgeGenerator.kotlinToNative(
                bodyGenerator,
                function,
                origin.function.returnType,
                bridgeArguments,
                independent = false
        ) { nativeValues ->
            "${origin.function.name}(${nativeValues.joinToString()})"
        }
        bodyGenerator.returnResult(result)
        functionBridgeBodies[function] = bodyGenerator.build()
    }

    private fun generateProtocolGetter(protocolGetterName: String, protocol: ObjCProtocol) {
        konst builder = NativeCodeBuilder(simpleBridgeGenerator.topLevelNativeScope)
        konst nativeBacked = object : NativeBacked {}
        with(builder) {
            out("Protocol* $protocolGetterName() {")
            out("    return @protocol(${protocol.name});")
            out("}")
        }
        simpleBridgeGenerator.insertNativeBridge(nativeBacked, emptyList(), builder.lines)
    }

    fun build(): BridgeBuilderResult {
        bridgeGeneratingVisitor.visitSimpleStubContainer(builderResult.stubs, null)
        return BridgeBuilderResult(
                kotlinFile,
                simpleBridgeGenerator.prepare(),
                propertyAccessorBridgeBodies.toMap(),
                functionBridgeBodies.toMap(),
                excludedStubs.toSet()
        )
    }
}
