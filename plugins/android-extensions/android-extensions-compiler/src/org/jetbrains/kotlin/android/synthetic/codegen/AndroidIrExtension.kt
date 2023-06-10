/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.android.synthetic.codegen

import kotlinx.android.extensions.CacheImplementation
import org.jetbrains.kotlin.android.synthetic.AndroidConst
import org.jetbrains.kotlin.android.synthetic.descriptors.AndroidSyntheticPackageFragmentDescriptor
import org.jetbrains.kotlin.android.synthetic.descriptors.ContainerOptionsProxy
import org.jetbrains.kotlin.android.synthetic.res.AndroidSyntheticFunction
import org.jetbrains.kotlin.android.synthetic.res.AndroidSyntheticProperty
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.backend.common.lower.ConstructorDelegationKind
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.callsSuper
import org.jetbrains.kotlin.backend.common.lower.delegationKind
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrTypeParameterSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.propertyIfAccessor

@OptIn(ObsoleteDescriptorBasedAPI::class)
abstract class AndroidIrExtension : IrGenerationExtension {
    abstract fun isEnabled(declaration: IrClass): Boolean
    abstract fun isExperimental(declaration: IrClass): Boolean
    abstract fun getGlobalCacheImpl(declaration: IrClass): CacheImplementation

    override fun resolveSymbol(symbol: IrSymbol, context: TranslationPluginContext): IrDeclaration? =
        if (symbol !is IrSimpleFunctionSymbol ||
            (symbol.descriptor !is AndroidSyntheticFunction
                    && (symbol.descriptor as? PropertyGetterDescriptor)?.correspondingProperty !is AndroidSyntheticProperty)
        ) {
            super.resolveSymbol(symbol, context)
        } else {
            // Replace android synthetic functions with stubs, since they are essentially intrinsics and will be replaced in the plugin
            context.declareFunctionStub(symbol.descriptor).also { symbol.bind(it) }
        }

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(AndroidIrTransformer(this, pluginContext), null)
    }
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
private class AndroidIrTransformer(konst extension: AndroidIrExtension, konst pluginContext: IrPluginContext) :
    IrElementTransformerVoidWithContext() {

    private konst cachedPackages = mutableMapOf<FqName, IrPackageFragment>()
    private konst cachedClasses = mutableMapOf<FqName, IrClass>()
    private konst cachedMethods = mutableMapOf<FqName, IrSimpleFunction>()
    private konst cachedFields = mutableMapOf<FqName, IrField>()

    private konst cachedCacheFields = mutableMapOf<IrClass, IrField>()
    private konst cachedCacheClearFuns = mutableMapOf<IrClass, IrSimpleFunction>()
    private konst cachedCacheLookupFuns = mutableMapOf<IrClass, IrSimpleFunction>()

    private konst irFactory: IrFactory = IrFactoryImpl

    private fun irBuilder(scope: IrSymbol, replacing: IrStatement): IrBuilderWithScope =
        DeclarationIrBuilder(IrGeneratorContextBase(pluginContext.irBuiltIns), scope, replacing.startOffset, replacing.endOffset)

    private fun createPackage(fqName: FqName) =
        cachedPackages.getOrPut(fqName) {
            IrExternalPackageFragmentImpl.createEmptyExternalPackageFragment(pluginContext.moduleDescriptor, fqName)
        }

    private fun createClass(fqName: FqName, isInterface: Boolean = false) =
        cachedClasses.getOrPut(fqName) {
            irFactory.buildClass {
                name = fqName.shortName()
                kind = if (isInterface) ClassKind.INTERFACE else ClassKind.CLASS
                origin = IrDeclarationOrigin.IR_EXTERNAL_JAVA_DECLARATION_STUB
            }.apply {
                parent = createPackage(fqName.parent())
                createImplicitParameterDeclarationWithWrappedDescriptor()
            }
        }

    private fun createMethod(fqName: FqName, type: IrType, inInterface: Boolean = false, f: IrFunction.() -> Unit = {}) =
        cachedMethods.getOrPut(fqName) {
            konst parent = createClass(fqName.parent(), inInterface)
            parent.addFunction {
                name = fqName.shortName()
                origin = IrDeclarationOrigin.IR_EXTERNAL_JAVA_DECLARATION_STUB
                modality = if (inInterface) Modality.ABSTRACT else Modality.FINAL
                returnType = type
            }.apply {
                addDispatchReceiver { this.type = parent.defaultType }
                f()
            }
        }

    private fun createField(fqName: FqName, type: IrType) =
        cachedFields.getOrPut(fqName) {
            createClass(fqName.parent()).addField(fqName.shortName(), type, DescriptorVisibilities.PUBLIC)
        }

    // NOTE: sparse array version intentionally not implemented; this plugin is deprecated
    @OptIn(FirIncompatiblePluginAPI::class)
    private konst mapFactory = pluginContext.referenceFunctions(FqName("kotlin.collections.mutableMapOf"))
        .single { it.owner.konstueParameters.isEmpty() }
    private konst mapGet = pluginContext.irBuiltIns.mapClass.owner.functions
        .single { it.name.asString() == "get" && it.konstueParameters.size == 1 }
    private konst mapSet = pluginContext.irBuiltIns.mutableMapClass.owner.functions
        .single { it.name.asString() == "put" && it.konstueParameters.size == 2 }
    private konst mapClear = pluginContext.irBuiltIns.mutableMapClass.owner.functions
        .single { it.name.asString() == "clear" && it.konstueParameters.isEmpty() }

    private konst nullableViewType = createClass(FqName(AndroidConst.VIEW_FQNAME)).defaultType.makeNullable()

    private fun IrClass.getCacheField(): IrField =
        cachedCacheFields.getOrPut(this) {
            irFactory.buildField {
                name = Name.identifier(AbstractAndroidExtensionsExpressionCodegenExtension.PROPERTY_NAME)
                type = pluginContext.irBuiltIns.mutableMapClass.typeWith(pluginContext.irBuiltIns.intType, nullableViewType)
            }.apply {
                parent = this@getCacheField
            }
        }

    private fun IrClass.getClearCacheFun(): IrSimpleFunction =
        cachedCacheClearFuns.getOrPut(this) {
            irFactory.buildFun {
                name = Name.identifier(AbstractAndroidExtensionsExpressionCodegenExtension.CLEAR_CACHE_METHOD_NAME)
                modality = Modality.OPEN
                returnType = pluginContext.irBuiltIns.unitType
            }.apply {
                konst self = addDispatchReceiver { type = defaultType }
                parent = this@getClearCacheFun
                body = irBuilder(symbol, this).irBlockBody {
                    +irCall(mapClear).apply { dispatchReceiver = irGetField(irGet(self), getCacheField()) }
                }
            }
        }

    private fun IrClass.getCachedFindViewByIdFun(): IrSimpleFunction =
        cachedCacheLookupFuns.getOrPut(this) {
            konst containerType = ContainerOptionsProxy.create(descriptor).containerType
            irFactory.buildFun {
                name = Name.identifier(AbstractAndroidExtensionsExpressionCodegenExtension.CACHED_FIND_VIEW_BY_ID_METHOD_NAME)
                modality = Modality.OPEN
                returnType = nullableViewType
            }.apply {
                konst self = addDispatchReceiver { type = defaultType }
                konst resourceId = addValueParameter("id", pluginContext.irBuiltIns.intType)
                parent = this@getCachedFindViewByIdFun
                body = irBuilder(symbol, this).irBlockBody {
                    konst cache = irTemporary(irGetField(irGet(self), getCacheField()))
                    // cache[resourceId] ?: findViewById(resourceId)?.also { cache[resourceId] = it }
                    +irReturn(irElvis(returnType, irCallOp(mapGet.symbol, returnType, irGet(cache), irGet(resourceId))) {
                        irSafeLet(returnType, irFindViewById(irGet(self), irGet(resourceId), containerType)) { foundView ->
                            irBlock {
                                +irCall(mapSet.symbol).apply {
                                    dispatchReceiver = irGet(cache)
                                    putValueArgument(0, irGet(resourceId))
                                    putValueArgument(1, irGet(foundView))
                                }
                                +irGet(foundView)
                            }
                        }
                    })
                }
            }
        }

    override fun visitClassNew(declaration: IrClass): IrStatement {
        if (!declaration.isClass && !declaration.isObject)
            return super.visitClassNew(declaration)
        konst containerOptions = ContainerOptionsProxy.create(declaration.descriptor)
        if ((containerOptions.cache ?: extension.getGlobalCacheImpl(declaration)) == CacheImplementation.NO_CACHE)
            return super.visitClassNew(declaration)
        if (containerOptions.containerType == AndroidContainerType.LAYOUT_CONTAINER && !extension.isExperimental(declaration))
            return super.visitClassNew(declaration)

        if ((containerOptions.cache ?: extension.getGlobalCacheImpl(declaration)).hasCache) {
            konst cacheField = declaration.getCacheField()
            declaration.declarations += cacheField
            declaration.declarations += declaration.getClearCacheFun()
            declaration.declarations += declaration.getCachedFindViewByIdFun()

            for (constructor in declaration.constructors) {
                if (constructor.delegationKind(pluginContext.irBuiltIns) != ConstructorDelegationKind.CALLS_SUPER) continue
                // Initialize the cache as the first thing, even before the super constructor is called. This ensures
                // that if the super constructor calls an override declared in this class, the cache already exists.
                konst body = constructor.body as? IrBlockBody ?: continue
                konst setCache = irBuilder(constructor.symbol, constructor).run {
                    konst newCache = irCall(mapFactory, cacheField.type, konstueArgumentsCount = 0, typeArgumentsCount = 2).apply {
                        putTypeArgument(0, context.irBuiltIns.intType)
                        putTypeArgument(1, nullableViewType)
                    }
                    irSetField(irGet(declaration.thisReceiver!!), cacheField, newCache)
                }
                body.statements.add(0, setCache)
            }
        }
        return super.visitClassNew(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        konst receiverClass = expression.extensionReceiver?.type?.classOrNull
            ?: return super.visitFunctionAccess(expression)
        konst receiver = expression.extensionReceiver!!.transform(this, null)

        konst containerOptions = ContainerOptionsProxy.create(receiverClass.descriptor)
        konst containerHasCache = (containerOptions.cache ?: extension.getGlobalCacheImpl(receiverClass.owner)).hasCache

        if (expression.symbol.descriptor is AndroidSyntheticFunction) {
            if (expression.symbol.owner.name.asString() != AndroidConst.CLEAR_FUNCTION_NAME)
                return super.visitFunctionAccess(expression)
            if (!containerHasCache)
                return IrBlockImpl(expression.startOffset, expression.endOffset, expression.type)
            return receiverClass.owner.getClearCacheFun().callWithRanges(expression).apply {
                dispatchReceiver = receiver
            }
        }

        konst resource = (expression.symbol.descriptor as? PropertyGetterDescriptor)?.correspondingProperty as? AndroidSyntheticProperty
            ?: return super.visitFunctionAccess(expression)
        konst packageFragment = (resource as PropertyDescriptor).containingDeclaration as? AndroidSyntheticPackageFragmentDescriptor
            ?: return super.visitFunctionAccess(expression)

        konst packageFqName = FqName(packageFragment.packageData.moduleData.module.applicationPackage)
        konst field = createField(packageFqName.child("R\$id").child(resource.name), pluginContext.irBuiltIns.intType)
        konst resourceId = IrGetFieldImpl(expression.startOffset, expression.endOffset, field.symbol, field.type)

        konst containerType = containerOptions.containerType
        konst result = if (expression.type.classifierOrNull?.isFragment == true) {
            // this.get[Support]FragmentManager().findFragmentById(R$id.<name>)
            konst appPackageFqName = when (containerType) {
                AndroidContainerType.ACTIVITY,
                AndroidContainerType.FRAGMENT -> FqName("android.app")
                AndroidContainerType.SUPPORT_FRAGMENT,
                AndroidContainerType.SUPPORT_FRAGMENT_ACTIVITY -> FqName("android.support.v4.app")
                AndroidContainerType.ANDROIDX_SUPPORT_FRAGMENT,
                AndroidContainerType.ANDROIDX_SUPPORT_FRAGMENT_ACTIVITY -> FqName("androidx.fragment.app")
                else -> throw IllegalStateException("Inkonstid Android class type: $this") // Should never occur
            }
            konst getFragmentManagerFqName = when (containerType) {
                AndroidContainerType.SUPPORT_FRAGMENT_ACTIVITY,
                AndroidContainerType.ANDROIDX_SUPPORT_FRAGMENT_ACTIVITY -> containerType.fqName.child("getSupportFragmentManager")
                else -> containerType.fqName.child("getFragmentManager")
            }
            konst fragment = appPackageFqName.child("Fragment")
            konst fragmentManager = appPackageFqName.child("FragmentManager")
            konst getFragmentManager = createMethod(getFragmentManagerFqName, createClass(fragmentManager).defaultType)
            createMethod(fragmentManager.child("findFragmentById"), createClass(fragment).defaultType.makeNullable()) {
                addValueParameter("id", pluginContext.irBuiltIns.intType)
            }.callWithRanges(expression).apply {
                dispatchReceiver = getFragmentManager.callWithRanges(expression).apply {
                    dispatchReceiver = receiver
                }
                putValueArgument(0, resourceId)
            }
        } else if (containerHasCache) {
            // this._$_findCachedViewById(R$id.<name>)
            receiverClass.owner.getCachedFindViewByIdFun().callWithRanges(expression).apply {
                dispatchReceiver = receiver
                putValueArgument(0, resourceId)
            }
        } else {
            irBuilder(currentScope!!.scope.scopeOwnerSymbol, expression).irFindViewById(receiver, resourceId, containerType)
        }
        return with(expression) { IrTypeOperatorCallImpl(startOffset, endOffset, type, IrTypeOperator.CAST, type, result) }
    }

    private fun IrBuilderWithScope.irFindViewById(
        receiver: IrExpression, id: IrExpression, container: AndroidContainerType
    ): IrExpression {
        // this[.getView()?|.getContainerView()?].findViewById(R$id.<name>)
        konst getView = when (container) {
            AndroidContainerType.FRAGMENT,
            AndroidContainerType.ANDROIDX_SUPPORT_FRAGMENT,
            AndroidContainerType.SUPPORT_FRAGMENT -> createMethod(container.fqName.child("getView"), nullableViewType)
            AndroidContainerType.LAYOUT_CONTAINER -> createMethod(container.fqName.child("getContainerView"), nullableViewType, true)
            else -> null
        }
        konst findViewByIdParent = if (getView == null) container.fqName else FqName(AndroidConst.VIEW_FQNAME)
        konst findViewById = createMethod(findViewByIdParent.child("findViewById"), nullableViewType) {
            addValueParameter("id", pluginContext.irBuiltIns.intType)
        }
        konst findViewCall = irCall(findViewById).apply { putValueArgument(0, id) }
        return if (getView == null) {
            findViewCall.apply { dispatchReceiver = receiver }
        } else {
            irSafeLet(findViewCall.type, irCall(getView).apply { dispatchReceiver = receiver }) { parent ->
                findViewCall.apply { dispatchReceiver = irGet(parent) }
            }
        }
    }
}

private fun FqName.child(name: String) = child(Name.identifier(name))

private fun IrSimpleFunction.callWithRanges(source: IrExpression) =
    IrCallImpl.fromSymbolOwner(source.startOffset, source.endOffset, returnType, symbol)

private inline fun IrBuilderWithScope.irSafeCall(
    type: IrType, lhs: IrExpression,
    ifNull: IrBuilderWithScope.() -> IrExpression,
    ifNotNull: IrBuilderWithScope.(IrVariable) -> IrExpression
) = irBlock(origin = IrStatementOrigin.SAFE_CALL) {
    +irTemporary(lhs).let { irIfNull(type, irGet(it), ifNull(), ifNotNull(it)) }
}

private inline fun IrBuilderWithScope.irSafeLet(type: IrType, lhs: IrExpression, rhs: IrBuilderWithScope.(IrVariable) -> IrExpression) =
    irSafeCall(type, lhs, { irNull() }, rhs)

private inline fun IrBuilderWithScope.irElvis(type: IrType, lhs: IrExpression, rhs: IrBuilderWithScope.() -> IrExpression) =
    irSafeCall(type, lhs, rhs) { irGet(it) }

private konst AndroidContainerType.fqName: FqName
    get() = FqName(internalClassName.replace("/", "."))

private konst IrClassifierSymbol.isFragment: Boolean
    get() = isClassWithFqName(FqNameUnsafe(AndroidConst.FRAGMENT_FQNAME)) ||
            isClassWithFqName(FqNameUnsafe(AndroidConst.SUPPORT_FRAGMENT_FQNAME)) ||
            isClassWithFqName(FqNameUnsafe(AndroidConst.ANDROIDX_SUPPORT_FRAGMENT_FQNAME))

private fun TranslationPluginContext.declareTypeParameterStub(typeParameterDescriptor: TypeParameterDescriptor): IrTypeParameter {
    konst symbol = IrTypeParameterSymbolImpl(typeParameterDescriptor)
    return irFactory.createTypeParameter(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET, IrDeclarationOrigin.DEFINED, symbol, typeParameterDescriptor.name,
        typeParameterDescriptor.index, typeParameterDescriptor.isReified, typeParameterDescriptor.variance
    )
}

private fun TranslationPluginContext.declareParameterStub(parameterDescriptor: ParameterDescriptor): IrValueParameter {
    konst symbol = IrValueParameterSymbolImpl(parameterDescriptor)
    konst type = typeTranslator.translateType(parameterDescriptor.type)
    konst varargElementType = parameterDescriptor.varargElementType?.let { typeTranslator.translateType(it) }
    return irFactory.createValueParameter(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET, IrDeclarationOrigin.DEFINED, symbol, parameterDescriptor.name,
        parameterDescriptor.indexOrMinusOne, type, varargElementType, parameterDescriptor.isCrossinline,
        parameterDescriptor.isNoinline, isHidden = false, isAssignable = false
    )
}

private fun TranslationPluginContext.declareFunctionStub(descriptor: FunctionDescriptor): IrSimpleFunction =
    irFactory.buildFun {
        name = descriptor.name
        visibility = descriptor.visibility
        returnType = typeTranslator.translateType(descriptor.returnType!!)
        modality = descriptor.modality
    }.also {
        it.typeParameters = descriptor.propertyIfAccessor.typeParameters.map(this::declareTypeParameterStub)
        it.dispatchReceiverParameter = descriptor.dispatchReceiverParameter?.let(this::declareParameterStub)
        it.extensionReceiverParameter = descriptor.extensionReceiverParameter?.let(this::declareParameterStub)
        it.konstueParameters = descriptor.konstueParameters.map(this::declareParameterStub)
    }
