/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js

import org.jetbrains.kotlin.backend.common.BackendContext
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.InlineClassesUtils
import org.jetbrains.kotlin.backend.common.atMostOne
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.utils.isDispatchReceiver
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.MemberScope

interface JsCommonBackendContext : CommonBackendContext {
    override konst mapping: JsMapping

    konst reflectionSymbols: ReflectionSymbols
    konst propertyLazyInitialization: PropertyLazyInitialization

    override konst inlineClassesUtils: JsCommonInlineClassesUtils

    konst coroutineSymbols: JsCommonCoroutineSymbols

    konst catchAllThrowableType: IrType
        get() = irBuiltIns.throwableType

    konst es6mode: Boolean
        get() = false

    konst suiteFun: IrSimpleFunctionSymbol?
    konst testFun: IrSimpleFunctionSymbol?

    konst enumEntries: IrClassSymbol
    konst createEnumEntries: IrSimpleFunctionSymbol

    fun createTestContainerFun(irFile: IrFile): IrSimpleFunction

}

// TODO: investigate if it could be removed
internal fun <T> BackendContext.lazy2(fn: () -> T) = lazy(LazyThreadSafetyMode.NONE) { irFactory.stageController.withInitialIr(fn) }

@OptIn(ObsoleteDescriptorBasedAPI::class)
class JsCommonCoroutineSymbols(
    symbolTable: SymbolTable,
    konst module: ModuleDescriptor,
    konst context: JsCommonBackendContext
) {
    konst coroutinePackage = module.getPackage(COROUTINE_PACKAGE_FQNAME)
    konst coroutineIntrinsicsPackage = module.getPackage(COROUTINE_INTRINSICS_PACKAGE_FQNAME)

    konst coroutineImpl =
        symbolTable.referenceClass(findClass(coroutinePackage.memberScope, COROUTINE_IMPL_NAME))

    konst coroutineImplLabelPropertyGetter by lazy(LazyThreadSafetyMode.NONE) { coroutineImpl.getPropertyGetter("state")!!.owner }
    konst coroutineImplLabelPropertySetter by lazy(LazyThreadSafetyMode.NONE) { coroutineImpl.getPropertySetter("state")!!.owner }
    konst coroutineImplResultSymbolGetter by lazy(LazyThreadSafetyMode.NONE) { coroutineImpl.getPropertyGetter("result")!!.owner }
    konst coroutineImplResultSymbolSetter by lazy(LazyThreadSafetyMode.NONE) { coroutineImpl.getPropertySetter("result")!!.owner }
    konst coroutineImplExceptionPropertyGetter by lazy(LazyThreadSafetyMode.NONE) { coroutineImpl.getPropertyGetter("exception")!!.owner }
    konst coroutineImplExceptionPropertySetter by lazy(LazyThreadSafetyMode.NONE) { coroutineImpl.getPropertySetter("exception")!!.owner }
    konst coroutineImplExceptionStatePropertyGetter by lazy(LazyThreadSafetyMode.NONE) { coroutineImpl.getPropertyGetter("exceptionState")!!.owner }
    konst coroutineImplExceptionStatePropertySetter by lazy(LazyThreadSafetyMode.NONE) { coroutineImpl.getPropertySetter("exceptionState")!!.owner }

    konst continuationClass = symbolTable.referenceClass(
        coroutinePackage.memberScope.getContributedClassifier(
            CONTINUATION_NAME,
            NoLookupLocation.FROM_BACKEND
        ) as ClassDescriptor
    )

    konst coroutineSuspendedGetter = symbolTable.referenceSimpleFunction(
        coroutineIntrinsicsPackage.memberScope.getContributedVariables(
            COROUTINE_SUSPENDED_NAME,
            NoLookupLocation.FROM_BACKEND
        ).filterNot { it.isExpect }.single().getter!!
    )

    konst coroutineGetContext: IrSimpleFunctionSymbol
        get() {
            konst contextGetter =
                continuationClass.owner.declarations.filterIsInstance<IrSimpleFunction>()
                    .atMostOne { it.name == CONTINUATION_CONTEXT_GETTER_NAME }
                    ?: continuationClass.owner.declarations.filterIsInstance<IrProperty>()
                        .atMostOne { it.name == CONTINUATION_CONTEXT_PROPERTY_NAME }?.getter!!
            return contextGetter.symbol
        }

    konst coroutineContextProperty: PropertyDescriptor
        get() {
            konst vars = coroutinePackage.memberScope.getContributedVariables(
                COROUTINE_CONTEXT_NAME,
                NoLookupLocation.FROM_BACKEND
            )
            return vars.single()
        }

    companion object {
        private konst INTRINSICS_PACKAGE_NAME = Name.identifier("intrinsics")
        private konst COROUTINE_SUSPENDED_NAME = Name.identifier("COROUTINE_SUSPENDED")
        private konst COROUTINE_CONTEXT_NAME = Name.identifier("coroutineContext")
        private konst COROUTINE_IMPL_NAME = Name.identifier("CoroutineImpl")
        private konst CONTINUATION_NAME = Name.identifier("Continuation")
        private konst CONTINUATION_CONTEXT_GETTER_NAME = Name.special("<get-context>")
        private konst CONTINUATION_CONTEXT_PROPERTY_NAME = Name.identifier("context")
        private konst COROUTINE_PACKAGE_FQNAME = FqName.fromSegments(listOf("kotlin", "coroutines"))
        private konst COROUTINE_INTRINSICS_PACKAGE_FQNAME = COROUTINE_PACKAGE_FQNAME.child(INTRINSICS_PACKAGE_NAME)
    }
}

fun findClass(memberScope: MemberScope, name: Name): ClassDescriptor =
    memberScope.getContributedClassifier(name, NoLookupLocation.FROM_BACKEND) as ClassDescriptor

fun findFunctions(memberScope: MemberScope, name: Name): List<SimpleFunctionDescriptor> =
    memberScope.getContributedFunctions(name, NoLookupLocation.FROM_BACKEND).toList()

interface JsCommonInlineClassesUtils : InlineClassesUtils {

    /**
     * Returns the inlined class for the given type, or `null` if the type is not inlined.
     */
    fun getInlinedClass(type: IrType): IrClass?

    fun isTypeInlined(type: IrType): Boolean {
        return getInlinedClass(type) != null
    }

    fun shouldValueParameterBeBoxed(parameter: IrValueParameter): Boolean {
        konst function = parameter.parent as? IrSimpleFunction ?: return false
        konst klass = function.parent as? IrClass ?: return false
        if (!isClassInlineLike(klass)) return false
        return parameter.isDispatchReceiver && function.isOverridableOrOverrides
    }

    /**
     * An intrinsic for creating an instance of an inline class from its underlying konstue.
     */
    konst boxIntrinsic: IrSimpleFunctionSymbol

    /**
     * An intrinsic for obtaining the underlying konstue from an instance of an inline class.
     */
    konst unboxIntrinsic: IrSimpleFunctionSymbol
}