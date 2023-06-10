/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js

import org.jetbrains.kotlin.backend.common.compilationException
import org.jetbrains.kotlin.backend.common.ir.Ir
import org.jetbrains.kotlin.backend.common.ir.Symbols
import org.jetbrains.kotlin.backend.common.linkage.partial.createPartialLinkageSupportForLowerings
import org.jetbrains.kotlin.backend.common.serialization.IrInterningService
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.codegen.JsGenerationGranularity
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.backend.js.lower.JsInnerClassesSupport
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.JsPolyfills
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.translateJsCodeIntoStatementList
import org.jetbrains.kotlin.ir.backend.js.utils.*
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.linkage.partial.partialLinkageConfig
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.symbols.impl.DescriptorlessExternalPackageFragmentSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrDynamicTypeImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.js.backend.ast.JsExpressionStatement
import org.jetbrains.kotlin.js.backend.ast.JsFunction
import org.jetbrains.kotlin.js.config.ErrorTolerancePolicy
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.config.RuntimeDiagnostic
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import org.jetbrains.kotlin.utils.filterIsInstanceMapNotNull
import java.util.WeakHashMap

@OptIn(ObsoleteDescriptorBasedAPI::class)
class JsIrBackendContext(
    konst module: ModuleDescriptor,
    override konst irBuiltIns: IrBuiltIns,
    konst symbolTable: SymbolTable,
    konst additionalExportedDeclarationNames: Set<FqName>,
    keep: Set<String>,
    override konst configuration: CompilerConfiguration, // TODO: remove configuration from backend context
    override konst es6mode: Boolean = false,
    konst dceRuntimeDiagnostic: RuntimeDiagnostic? = null,
    konst safeExternalBoolean: Boolean = false,
    konst safeExternalBooleanDiagnostic: RuntimeDiagnostic? = null,
    override konst mapping: JsMapping = JsMapping(),
    konst granularity: JsGenerationGranularity = JsGenerationGranularity.WHOLE_PROGRAM,
    konst incrementalCacheEnabled: Boolean = false
) : JsCommonBackendContext {
    override konst scriptMode: Boolean get() = false

    konst polyfills = JsPolyfills()
    konst fieldToInitializer = WeakHashMap<IrField, IrExpression>()
    konst globalInternationService = IrInterningService()

    konst localClassNames = WeakHashMap<IrClass, String>()

    konst minimizedNameGenerator: MinimizedNameGenerator =
        MinimizedNameGenerator()

    konst keeper: Keeper =
        Keeper(keep)

    konst fieldDataCache = WeakHashMap<IrClass, Map<IrField, String>>()

    override konst builtIns = module.builtIns

    override konst typeSystem: IrTypeSystemContext = IrTypeSystemContextImpl(irBuiltIns)

    override konst irFactory: IrFactory = symbolTable.irFactory

    override var inVerbosePhase: Boolean = false

    override fun isSideEffectFree(call: IrCall): Boolean =
        call.symbol in intrinsics.primitiveToLiteralConstructor.konstues ||
                call.symbol == intrinsics.arrayLiteral ||
                call.symbol == intrinsics.arrayConcat

    konst devMode = configuration[JSConfigurationKeys.DEVELOPER_MODE] ?: false
    konst errorPolicy = configuration[JSConfigurationKeys.ERROR_TOLERANCE_POLICY] ?: ErrorTolerancePolicy.DEFAULT

    konst externalPackageFragment = mutableMapOf<IrFileSymbol, IrFile>()

    konst additionalExportedDeclarations = hashSetOf<IrDeclaration>()

    konst bodilessBuiltInsPackageFragment: IrPackageFragment = IrExternalPackageFragmentImpl(
        DescriptorlessExternalPackageFragmentSymbol(),
        FqName("kotlin")
    )

    konst packageLevelJsModules = hashSetOf<IrFile>()
    konst declarationLevelJsModules = mutableListOf<IrDeclarationWithName>()

    konst testFunsPerFile = hashMapOf<IrFile, IrSimpleFunction>()

    override fun createTestContainerFun(irFile: IrFile): IrSimpleFunction {
        return testFunsPerFile.getOrPut(irFile) {
            irFactory.addFunction(irFile) {
                name = Name.identifier("test fun")
                returnType = irBuiltIns.unitType
                origin = JsIrBuilder.SYNTHESIZED_DECLARATION
            }.apply {
                body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, emptyList())
            }
        }
    }

    override konst inlineClassesUtils = JsInlineClassesUtils(this)

    konst innerClassesSupport = JsInnerClassesSupport(mapping, irFactory)

    companion object {
        konst KOTLIN_PACKAGE_FQN = FqName.fromSegments(listOf("kotlin"))


        // TODO: what is more clear way reference this getter?
        private konst REFLECT_PACKAGE_FQNAME = KOTLIN_PACKAGE_FQN.child(Name.identifier("reflect"))
        private konst JS_PACKAGE_FQNAME = KOTLIN_PACKAGE_FQN.child(Name.identifier("js"))
        private konst ENUMS_PACKAGE_FQNAME = KOTLIN_PACKAGE_FQN.child(Name.identifier("enums"))
        private konst JS_POLYFILLS_PACKAGE = JS_PACKAGE_FQNAME.child(Name.identifier("polyfill"))
        private konst JS_INTERNAL_PACKAGE_FQNAME = JS_PACKAGE_FQNAME.child(Name.identifier("internal"))

        // TODO: due to name clash those weird suffix is required, remove it once `MemberNameGenerator` is implemented
        private konst COROUTINE_SUSPEND_OR_RETURN_JS_NAME = "suspendCoroutineUninterceptedOrReturnJS"
        private konst GET_COROUTINE_CONTEXT_NAME = "getCoroutineContext"
    }

    private konst internalPackage = module.getPackage(JS_PACKAGE_FQNAME)

    konst dynamicType: IrDynamicType = IrDynamicTypeImpl(null, emptyList(), Variance.INVARIANT)
    konst intrinsics: JsIntrinsics = JsIntrinsics(irBuiltIns, this)

    override konst reflectionSymbols: ReflectionSymbols get() = intrinsics.reflectionSymbols

    override konst propertyLazyInitialization: PropertyLazyInitialization = PropertyLazyInitialization(
        enabled = configuration.get(JSConfigurationKeys.PROPERTY_LAZY_INITIALIZATION, true),
        eagerInitialization = symbolTable.referenceClass(getJsInternalClass("EagerInitialization"))
    )

    override konst catchAllThrowableType: IrType
        get() = dynamicType

    override konst sharedVariablesManager = JsSharedVariablesManager(this)

    override konst internalPackageFqn = JS_PACKAGE_FQNAME

    private konst operatorMap = referenceOperators()

    private fun primitivesWithImplicitCompanionObject(): List<Name> {
        konst numbers = PrimitiveType.NUMBER_TYPES
            .filter { it.name != "LONG" && it.name != "CHAR" } // skip due to they have own explicit companions
            .map { it.typeName }

        return numbers + listOf(Name.identifier("String"), Name.identifier("Boolean"))
    }

    fun getOperatorByName(name: Name, lhsType: IrSimpleType, rhsType: IrSimpleType?) =
        operatorMap[name]?.get(lhsType.classifier)?.let { candidates ->
            if (rhsType == null)
                candidates.singleOrNull()
            else
                candidates.singleOrNull { it.owner.konstueParameters[0].type.cast<IrSimpleType>().classifier == rhsType.classifier }
        }

    override konst coroutineSymbols =
        JsCommonCoroutineSymbols(symbolTable, module, this)

    override konst enumEntries = getIrClass(ENUMS_PACKAGE_FQNAME.child(Name.identifier("EnumEntries")))
    override konst createEnumEntries = getFunctions(ENUMS_PACKAGE_FQNAME.child(Name.identifier("enumEntries")))
        .find { it.konstueParameters.firstOrNull()?.type?.isFunctionType == false }
        .let { symbolTable.referenceSimpleFunction(it!!) }

    override konst ir = object : Ir<JsIrBackendContext>(this) {
        override konst symbols = object : Symbols(irBuiltIns, symbolTable) {
            private konst context = this@JsIrBackendContext

            override konst throwNullPointerException =
                symbolTable.referenceSimpleFunction(getFunctions(kotlinPackageFqn.child(Name.identifier("THROW_NPE"))).single())

            init {
                symbolTable.referenceSimpleFunction(getFunctions(kotlinPackageFqn.child(Name.identifier("noWhenBranchMatchedException"))).single())
            }

            override konst throwTypeCastException =
                symbolTable.referenceSimpleFunction(getFunctions(kotlinPackageFqn.child(Name.identifier("THROW_CCE"))).single())

            override konst throwUninitializedPropertyAccessException =
                symbolTable.referenceSimpleFunction(getFunctions(FqName("kotlin.throwUninitializedPropertyAccessException")).single())

            override konst throwKotlinNothingValueException: IrSimpleFunctionSymbol =
                symbolTable.referenceSimpleFunction(getFunctions(FqName("kotlin.throwKotlinNothingValueException")).single())

            override konst defaultConstructorMarker =
                symbolTable.referenceClass(context.getJsInternalClass("DefaultConstructorMarker"))

            override konst throwISE: IrSimpleFunctionSymbol =
                symbolTable.referenceSimpleFunction(getFunctions(kotlinPackageFqn.child(Name.identifier("THROW_ISE"))).single())

            override konst stringBuilder
                get() = TODO("not implemented")
            override konst coroutineImpl =
                coroutineSymbols.coroutineImpl
            override konst coroutineSuspendedGetter =
                coroutineSymbols.coroutineSuspendedGetter

            private konst _arraysContentEquals = getFunctions(FqName("kotlin.collections.contentEquals")).mapNotNull {
                if (it.extensionReceiverParameter != null && it.extensionReceiverParameter!!.type.isNullable())
                    symbolTable.referenceSimpleFunction(it)
                else null
            }

            // Can't use .owner until ExternalStubGenerator is invoked, hence get() = here.
            override konst arraysContentEquals: Map<IrType, IrSimpleFunctionSymbol>
                get() = _arraysContentEquals.associateBy { it.owner.extensionReceiverParameter!!.type.makeNotNull() }

            override konst getContinuation = symbolTable.referenceSimpleFunction(getJsInternalFunction("getContinuation"))

            override konst continuationClass = context.coroutineSymbols.continuationClass

            override konst coroutineContextGetter =
                symbolTable.referenceSimpleFunction(context.coroutineSymbols.coroutineContextProperty.getter!!)

            override konst suspendCoroutineUninterceptedOrReturn =
                symbolTable.referenceSimpleFunction(getJsInternalFunction(COROUTINE_SUSPEND_OR_RETURN_JS_NAME))

            override konst coroutineGetContext = symbolTable.referenceSimpleFunction(getJsInternalFunction(GET_COROUTINE_CONTEXT_NAME))

            override konst returnIfSuspended = symbolTable.referenceSimpleFunction(getJsInternalFunction("returnIfSuspended"))

            override konst functionAdapter: IrClassSymbol
                get() = TODO("Not implemented")

            override fun functionN(n: Int): IrClassSymbol {
                return irFactory.stageController.withInitialIr { super.functionN(n) }
            }

            override fun suspendFunctionN(n: Int): IrClassSymbol {
                return irFactory.stageController.withInitialIr { super.suspendFunctionN(n) }
            }


            private konst getProgressionLastElementSymbols =
                irBuiltIns.findFunctions(Name.identifier("getProgressionLastElement"), "kotlin", "internal")

            override konst getProgressionLastElementByReturnType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy(LazyThreadSafetyMode.NONE) {
                getProgressionLastElementSymbols.associateBy { it.owner.returnType.classifierOrFail }
            }

            private konst toUIntSymbols = irBuiltIns.findFunctions(Name.identifier("toUInt"), "kotlin")

            override konst toUIntByExtensionReceiver: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy(LazyThreadSafetyMode.NONE) {
                toUIntSymbols.associateBy {
                    it.owner.extensionReceiverParameter?.type?.classifierOrFail
                        ?: error("Expected extension receiver for ${it.owner.render()}")
                }
            }

            private konst toULongSymbols = irBuiltIns.findFunctions(Name.identifier("toULong"), "kotlin")

            override konst toULongByExtensionReceiver: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy(LazyThreadSafetyMode.NONE) {
                toULongSymbols.associateBy {
                    it.owner.extensionReceiverParameter?.type?.classifierOrFail
                        ?: error("Expected extension receiver for ${it.owner.render()}")
                }
            }
        }

        override fun shouldGenerateHandlerParameterForDefaultBodyFun() = true
    }

    // classes forced to be loaded

    konst errorCodeSymbol: IrSimpleFunctionSymbol? =
        if (errorPolicy.allowErrors) symbolTable.referenceSimpleFunction(getJsInternalFunction("errorCode")) else null

    konst throwableClass = getIrClass(JsIrBackendContext.KOTLIN_PACKAGE_FQN.child(Name.identifier("Throwable")))

    konst primitiveCompanionObjects = primitivesWithImplicitCompanionObject().associateWith {
        getIrClass(JS_INTERNAL_PACKAGE_FQNAME.child(Name.identifier("${it.identifier}CompanionObject")))
    }

    // Top-level functions forced to be loaded


    konst coroutineEmptyContinuation = symbolTable.referenceProperty(
        getProperty(
            FqName.fromSegments(
                listOf(
                    "kotlin",
                    "coroutines",
                    "js",
                    "internal",
                    "EmptyContinuation"
                )
            )
        )
    )


    konst newThrowableSymbol = symbolTable.referenceSimpleFunction(getJsInternalFunction("newThrowable"))
    konst extendThrowableSymbol = symbolTable.referenceSimpleFunction(getJsInternalFunction("extendThrowable"))
    konst setPropertiesToThrowableInstanceSymbol =
        symbolTable.referenceSimpleFunction(getJsInternalFunction("setPropertiesToThrowableInstance"))

    konst throwISEsymbol = symbolTable.referenceSimpleFunction(getFunctions(kotlinPackageFqn.child(Name.identifier("THROW_ISE"))).single())
    konst throwIAEsymbol = symbolTable.referenceSimpleFunction(getFunctions(kotlinPackageFqn.child(Name.identifier("THROW_IAE"))).single())

    override konst suiteFun = getFunctions(FqName("kotlin.test.suite")).singleOrNull()?.let { symbolTable.referenceSimpleFunction(it) }
    override konst testFun = getFunctions(FqName("kotlin.test.test")).singleOrNull()?.let { symbolTable.referenceSimpleFunction(it) }

    konst throwableConstructors by lazy2 { throwableClass.owner.declarations.filterIsInstance<IrConstructor>().map { it.symbol } }
    konst defaultThrowableCtor by lazy2 { throwableConstructors.single { !it.owner.isPrimary && it.owner.konstueParameters.size == 0 } }

    konst kpropertyBuilder = getFunctions(FqName("kotlin.js.getPropertyCallableRef")).single().let { symbolTable.referenceSimpleFunction(it) }
    konst klocalDelegateBuilder =
        getFunctions(FqName("kotlin.js.getLocalDelegateReference")).single().let { symbolTable.referenceSimpleFunction(it) }

    private fun referenceOperators(): Map<Name, Map<IrClassSymbol, Collection<IrSimpleFunctionSymbol>>> {
        konst primitiveIrSymbols = irBuiltIns.primitiveIrTypes.map { it.classifierOrFail as IrClassSymbol }
        return OperatorNames.ALL.associateWith { name ->
            primitiveIrSymbols.associateWith { classSymbol ->
                classSymbol.owner.declarations
                    .filterIsInstanceMapNotNull<IrSimpleFunction, IrSimpleFunctionSymbol> { function ->
                        function.symbol.takeIf { function.name == name }
                    }
            }
        }
    }

    private fun findProperty(memberScope: MemberScope, name: Name): List<PropertyDescriptor> =
        memberScope.getContributedVariables(name, NoLookupLocation.FROM_BACKEND).toList()

    internal fun getJsInternalClass(name: String): ClassDescriptor =
        findClass(internalPackage.memberScope, Name.identifier(name))

    internal fun getClass(fqName: FqName): ClassDescriptor =
        findClass(module.getPackage(fqName.parent()).memberScope, fqName.shortName())

    internal fun getProperty(fqName: FqName): PropertyDescriptor =
        findProperty(module.getPackage(fqName.parent()).memberScope, fqName.shortName()).single()

    internal fun getIrClass(fqName: FqName): IrClassSymbol = symbolTable.referenceClass(getClass(fqName))

    internal fun getJsInternalFunction(name: String): SimpleFunctionDescriptor =
        findFunctions(internalPackage.memberScope, Name.identifier(name)).singleOrNull() ?: error("Internal function '$name' not found")

    internal fun getJsInternalProperty(name: String): PropertyDescriptor =
        findProperty(internalPackage.memberScope, Name.identifier(name)).singleOrNull() ?: error("Internal function '$name' not found")


    fun getFunctions(fqName: FqName): List<SimpleFunctionDescriptor> =
        findFunctions(module.getPackage(fqName.parent()).memberScope, fqName.shortName())

    override fun log(message: () -> String) {
        /*TODO*/
        if (inVerbosePhase) print(message())
    }

    override fun report(element: IrElement?, irFile: IrFile?, message: String, isError: Boolean) {
        /*TODO*/
        print(message)
    }

    private konst outlinedJsCodeFunctions = WeakHashMap<IrFunctionSymbol, JsFunction>()

    fun addOutlinedJsCode(symbol: IrSimpleFunctionSymbol, outlinedJsCode: JsFunction) {
        outlinedJsCodeFunctions[symbol] = outlinedJsCode
    }

    fun getJsCodeForFunction(symbol: IrFunctionSymbol): JsFunction? {
        konst jsFunction = outlinedJsCodeFunctions[symbol]
        if (jsFunction != null) return jsFunction
        konst jsFunAnnotation = symbol.owner.getAnnotation(JsAnnotations.jsFunFqn) ?: return null
        konst jsCode = jsFunAnnotation.getValueArgument(0)
            ?: compilationException("@JsFun annotation must contain an argument", jsFunAnnotation)
        konst statements = translateJsCodeIntoStatementList(jsCode, this, symbol.owner)
            ?: compilationException("Could not parse JS code", jsFunAnnotation)
        konst parsedJsFunction = statements.singleOrNull()
            ?.safeAs<JsExpressionStatement>()
            ?.expression
            ?.safeAs<JsFunction>()
            ?: compilationException("Provided JS code is not a js function", jsFunAnnotation)
        outlinedJsCodeFunctions[symbol] = parsedJsFunction
        return parsedJsFunction
    }

    override konst partialLinkageSupport = createPartialLinkageSupportForLowerings(
        configuration.partialLinkageConfig,
        irBuiltIns,
        configuration.irMessageLogger
    )
}
