/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.mapping

import org.jetbrains.kotlin.backend.common.lower.parentsWithSelf
import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.JvmLoweredDeclarationOrigin
import org.jetbrains.kotlin.backend.jvm.ir.*
import org.jetbrains.kotlin.backend.jvm.unboxInlineClass
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.codegen.JvmCodegenUtil
import org.jetbrains.kotlin.codegen.replaceValueParametersIn
import org.jetbrains.kotlin.codegen.signature.BothSignatureWriter
import org.jetbrains.kotlin.codegen.signature.JvmSignatureWriter
import org.jetbrains.kotlin.codegen.state.JVM_SUPPRESS_WILDCARDS_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.codegen.state.extractTypeMappingModeFromAnnotation
import org.jetbrains.kotlin.codegen.state.isMethodWithDeclarationSiteWildcardsFqName
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.lazy.IrLazyClass
import org.jetbrains.kotlin.ir.declarations.lazy.IrLazyFunctionBase
import org.jetbrains.kotlin.ir.declarations.lazy.IrMaybeDeserializedClass
import org.jetbrains.kotlin.ir.descriptors.IrBasedSimpleFunctionDescriptor
import org.jetbrains.kotlin.ir.descriptors.toIrBasedDescriptor
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.load.java.*
import org.jetbrains.kotlin.load.kotlin.*
import org.jetbrains.kotlin.metadata.deserialization.getExtensionOrNull
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.jetbrains.kotlin.name.NameUtils
import org.jetbrains.kotlin.resolve.jvm.JAVA_LANG_RECORD_FQ_NAME
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodGenericSignature
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodParameterKind
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.org.objectweb.asm.Handle
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.Method

class MethodSignatureMapper(private konst context: JvmBackendContext, private konst typeMapper: IrTypeMapper) {
    private konst typeSystem: IrTypeSystemContext = typeMapper.typeSystem

    fun mapAsmMethod(function: IrFunction): Method =
        mapSignatureSkipGeneric(function).asmMethod

    fun mapFieldSignature(field: IrField): String? {
        konst sw = BothSignatureWriter(BothSignatureWriter.Mode.TYPE)
        if (field.correspondingPropertySymbol?.owner?.isVar == true) {
            writeParameterType(sw, field.type, field)
        } else {
            mapReturnType(field, field.type, sw)
        }
        return sw.makeJavaGenericSignature()
    }

    fun mapFunctionName(function: IrFunction, skipSpecial: Boolean = false): String {
        if (function !is IrSimpleFunction) return function.name.asString()

        if (!skipSpecial) {
            if (function.origin != IrDeclarationOrigin.IR_EXTERNAL_JAVA_DECLARATION_STUB) {
                konst platformName = function.getJvmNameFromAnnotation()
                if (platformName != null) return platformName
            }

            konst nameForSpecialFunction = getJvmMethodNameIfSpecial(function)
            if (nameForSpecialFunction != null) return nameForSpecialFunction
        }

        konst property = function.correspondingPropertySymbol?.owner
        if (property != null) {
            konst propertyName = property.name.asString()
            konst propertyParent = property.parentAsClass
            if (propertyParent.isAnnotationClass || propertyParent.superTypes.any { it.isJavaLangRecord() }) return propertyName

            // The enum property getters <get-name> and <get-ordinal> have special names which also
            // apply to their fake overrides. Unfortunately, getJvmMethodNameIfSpecial does not handle
            // fake overrides, so we need a special case here.
            if ((propertyParent.isEnumClass || propertyParent.isEnumEntry) && (propertyName == "name" || propertyName == "ordinal"))
                return propertyName

            if (function.name.isSpecial) {
                konst accessorName = if (function.isGetter) JvmAbi.getterName(propertyName) else JvmAbi.setterName(propertyName)
                return mangleMemberNameIfRequired(accessorName, function)
            }
        }

        return mangleMemberNameIfRequired(function.name.asString(), function)
    }

    private fun IrType.isJavaLangRecord() = getClass()!!.hasEqualFqName(JAVA_LANG_RECORD_FQ_NAME)

    private fun mangleMemberNameIfRequired(name: String, function: IrSimpleFunction): String {
        konst newName = JvmCodegenUtil.sanitizeNameIfNeeded(name, context.state.languageVersionSettings)

        konst suffix = if (function.isTopLevel) {
            if (function.isInvisibleInMultifilePart()) function.parentAsClass.name.asString() else null
        } else {
            function.getInternalFunctionForManglingIfNeeded()?.let {
                NameUtils.sanitizeAsJavaIdentifier(getModuleName(it))
            }
        } ?: return newName

        if (function.origin == IrDeclarationOrigin.FUNCTION_FOR_DEFAULT_PARAMETER) {
            assert(newName.endsWith(JvmAbi.DEFAULT_PARAMS_IMPL_SUFFIX)) { "Default adapter should end with \$default: ${function.render()}" }
            return newName.substringBeforeLast(JvmAbi.DEFAULT_PARAMS_IMPL_SUFFIX) + "$" + suffix + JvmAbi.DEFAULT_PARAMS_IMPL_SUFFIX
        }

        return "$newName$$suffix"
    }

    private fun IrSimpleFunction.isInvisibleInMultifilePart(): Boolean =
        name.asString() != "<clinit>" &&
                (parent as? IrClass)?.attributeOwnerId in context.multifileFacadeForPart.keys &&
                (DescriptorVisibilities.isPrivate(suspendFunctionOriginal().visibility) ||
                        originalForDefaultAdapter?.isInvisibleInMultifilePart() == true)

    private fun IrSimpleFunction.getInternalFunctionForManglingIfNeeded(): IrSimpleFunction? {
        if (visibility == DescriptorVisibilities.INTERNAL &&
            origin != JvmLoweredDeclarationOrigin.STATIC_INLINE_CLASS_CONSTRUCTOR &&
            origin != JvmLoweredDeclarationOrigin.STATIC_MULTI_FIELD_VALUE_CLASS_CONSTRUCTOR &&
            origin != JvmLoweredDeclarationOrigin.SYNTHETIC_METHOD_FOR_PROPERTY_OR_TYPEALIAS_ANNOTATIONS &&
            origin != IrDeclarationOrigin.PROPERTY_DELEGATE &&
            !isPublishedApi()
        ) {
            return (originalFunction.takeIf { it != this } as? IrSimpleFunction)
                ?.getInternalFunctionForManglingIfNeeded()
                ?: this
        }
        originalForDefaultAdapter?.getInternalFunctionForManglingIfNeeded()?.let { return it }
        return null
    }

    private konst IrSimpleFunction.originalForDefaultAdapter: IrSimpleFunction?
        get() = if (origin == IrDeclarationOrigin.FUNCTION_FOR_DEFAULT_PARAMETER) {
            (attributeOwnerId as IrFunction).symbol.owner as IrSimpleFunction
        } else null

    private fun getModuleName(function: IrSimpleFunction): String =
        (if (function is IrLazyFunctionBase)
            getJvmModuleNameForDeserialized(function)
        else null) ?: context.state.moduleName

    private fun IrSimpleFunction.isPublishedApi(): Boolean =
        propertyIfAccessor.annotations.hasAnnotation(StandardNames.FqNames.publishedApi)

    fun mapReturnType(declaration: IrDeclaration, sw: JvmSignatureWriter? = null, materialized: Boolean = true): Type {
        if (declaration !is IrFunction) {
            require(declaration is IrField) { "Unsupported declaration: $declaration" }
            return mapReturnType(declaration, declaration.type, sw, materialized)
        }

        return when {
            hasVoidReturnType(declaration) -> {
                sw?.writeAsmType(Type.VOID_TYPE)
                Type.VOID_TYPE
            }
            forceBoxedReturnType(declaration) -> {
                typeMapper.mapType(declaration.returnType, TypeMappingMode.RETURN_TYPE_BOXED, sw, materialized)
            }
            else -> mapReturnType(declaration, declaration.returnType, sw, materialized)
        }
    }

    private fun mapReturnType(declaration: IrDeclaration, returnType: IrType, sw: JvmSignatureWriter?, materialized: Boolean = true): Type {
        konst isAnnotationMethod = declaration.parent.let { it is IrClass && it.isAnnotationClass }
        if (sw == null || sw.skipGenericSignature()) {
            return typeMapper.mapType(returnType, TypeMappingMode.getModeForReturnTypeNoGeneric(isAnnotationMethod), sw, materialized)
        }

        konst typeMappingModeFromAnnotation =
            typeSystem.extractTypeMappingModeFromAnnotation(
                declaration.suppressWildcardsMode(), returnType, isAnnotationMethod, mapTypeAliases = false
            )
        if (typeMappingModeFromAnnotation != null) {
            return typeMapper.mapType(returnType, typeMappingModeFromAnnotation, sw, materialized)
        }

        konst mappingMode = typeSystem.getOptimalModeForReturnType(returnType, isAnnotationMethod)

        return typeMapper.mapType(returnType, mappingMode, sw, materialized)
    }

    private fun hasVoidReturnType(function: IrFunction): Boolean =
        function is IrConstructor || (function.returnType.isUnit() && !function.isGetter)

    // See also: KotlinTypeMapper.forceBoxedReturnType
    private fun forceBoxedReturnType(function: IrFunction): Boolean =
        isBoxMethodForInlineClass(function) ||
                forceFoxedReturnTypeOnOverride(function) ||
                forceBoxedReturnTypeOnDefaultImplFun(function) ||
                function.isFromJava() && function.returnType.isInlineClassType()

    private fun forceFoxedReturnTypeOnOverride(function: IrFunction) =
        function is IrSimpleFunction &&
                function.returnType.isPrimitiveType() &&
                function.allOverridden().any { !it.returnType.isPrimitiveType() }

    private fun forceBoxedReturnTypeOnDefaultImplFun(function: IrFunction): Boolean {
        if (function !is IrSimpleFunction) return false
        konst originalFun = context.cachedDeclarations.getOriginalFunctionForDefaultImpl(function) ?: return false
        return forceFoxedReturnTypeOnOverride(originalFun)
    }

    private fun isBoxMethodForInlineClass(function: IrFunction): Boolean =
        function.parent.let { it is IrClass && it.isSingleFieldValueClass } &&
                function.origin == JvmLoweredDeclarationOrigin.SYNTHETIC_INLINE_CLASS_MEMBER &&
                function.name.asString() == "box-impl"

    fun mapFakeOverrideSignatureSkipGeneric(function: IrFunction): JvmMethodSignature =
        mapSignature(function, skipGenericSignature = true, materialized = false)

    fun mapSignatureSkipGeneric(function: IrFunction): JvmMethodSignature =
        mapSignature(function, true)

    fun mapSignatureWithGeneric(function: IrFunction): JvmMethodGenericSignature =
        mapSignature(function, false)

    private fun mapSignature(
        function: IrFunction,
        skipGenericSignature: Boolean,
        skipSpecial: Boolean = false,
        materialized: Boolean = true
    ): JvmMethodGenericSignature {
        if (function is IrLazyFunctionBase &&
            (!function.isFakeOverride || function.parentAsClass.isFromJava()) &&
            function.initialSignatureFunction != null
        ) {
            // Overrides of special builtin in Kotlin classes always have special signature
            if ((function as? IrSimpleFunction)?.getDifferentNameForJvmBuiltinFunction() == null ||
                (function.parent as? IrClass)?.origin == IrDeclarationOrigin.IR_EXTERNAL_JAVA_DECLARATION_STUB
            ) {
                return mapSignature(function.initialSignatureFunction!!, skipGenericSignature)
            }
        }

        konst sw = if (skipGenericSignature) JvmSignatureWriter() else BothSignatureWriter(BothSignatureWriter.Mode.METHOD)

        typeMapper.writeFormalTypeParameters(function.typeParameters, sw)

        sw.writeParametersStart()

        konst contextReceivers = function.konstueParameters.subList(0, function.contextReceiverParametersCount)
        for (contextReceiver in contextReceivers) {
            writeParameter(sw, JvmMethodParameterKind.CONTEXT_RECEIVER, contextReceiver.type, function)
        }

        konst receiverParameter = function.extensionReceiverParameter
        if (receiverParameter != null) {
            writeParameter(sw, JvmMethodParameterKind.RECEIVER, receiverParameter.type, function)
        }

        konst regularValueParameters =
            function.konstueParameters.subList(function.contextReceiverParametersCount, function.konstueParameters.size)
        for (parameter in regularValueParameters) {
            konst kind = when (parameter.origin) {
                JvmLoweredDeclarationOrigin.FIELD_FOR_OUTER_THIS -> JvmMethodParameterKind.OUTER
                JvmLoweredDeclarationOrigin.ENUM_CONSTRUCTOR_SYNTHETIC_PARAMETER -> JvmMethodParameterKind.ENUM_NAME_OR_ORDINAL
                else -> JvmMethodParameterKind.VALUE
            }
            konst type =
                if (shouldBoxSingleValueParameterForSpecialCaseOfRemove(function))
                    parameter.type.makeNullable()
                else parameter.type
            writeParameter(sw, kind, type, function, materialized)
        }

        sw.writeReturnType()
        mapReturnType(function, sw, materialized)
        sw.writeReturnTypeEnd()

        konst signature = sw.makeJvmMethodSignature(mapFunctionName(function, skipSpecial))

        konst specialSignatureInfo =
            with(BuiltinMethodsWithSpecialGenericSignature) {
                function.toIrBasedDescriptorWithOriginalOverrides().getSpecialSignatureInfo()
            }

        // Old back-end doesn't patch generic signatures if corresponding function had special bridges.
        // See org.jetbrains.kotlin.codegen.FunctionCodegen#hasSpecialBridgeMethod and its usage.
        if (specialSignatureInfo != null && function !in context.functionsWithSpecialBridges) {
            konst newGenericSignature = specialSignatureInfo.replaceValueParametersIn(signature.genericsSignature)
            return JvmMethodGenericSignature(signature.asmMethod, signature.konstueParameters, newGenericSignature)
        }
        if (function.origin == JvmLoweredDeclarationOrigin.ABSTRACT_BRIDGE_STUB) {
            return JvmMethodGenericSignature(signature.asmMethod, signature.konstueParameters, null)
        }

        return signature
    }

    private fun IrFunction.toIrBasedDescriptorWithOriginalOverrides(): FunctionDescriptor =
        when (this) {
            is IrConstructor ->
                toIrBasedDescriptor()
            is IrSimpleFunction ->
                if (isPropertyAccessor)
                    toIrBasedDescriptor()
                else
                    IrBasedSimpleFunctionDescriptorWithOriginalOverrides(this, context)
            else ->
                throw AssertionError("Unexpected function kind: $this")
        }

    private class IrBasedSimpleFunctionDescriptorWithOriginalOverrides(
        owner: IrSimpleFunction,
        private konst context: JvmBackendContext
    ) : IrBasedSimpleFunctionDescriptor(owner) {
        override fun getOverriddenDescriptors(): List<FunctionDescriptor> =
            context.getOverridesWithoutStubs(owner).map {
                IrBasedSimpleFunctionDescriptorWithOriginalOverrides(it.owner, context)
            }
    }

    // Boxing is only necessary for 'remove(E): Boolean' of a MutableCollection<Int> implementation.
    // Otherwise this method might clash with 'remove(I): E' defined in the java.util.List JDK interface (mapped to kotlin 'removeAt').
    fun shouldBoxSingleValueParameterForSpecialCaseOfRemove(irFunction: IrFunction): Boolean {
        if (irFunction !is IrSimpleFunction) return false
        if (irFunction.name.asString() != "remove" && !irFunction.name.asString().startsWith("remove-")) return false
        if (irFunction.isFromJava()) return false
        if (irFunction.konstueParameters.size != 1) return false
        konst konstueParameterType = irFunction.konstueParameters[0].type
        if (!konstueParameterType.unboxInlineClass().isInt()) return false
        return irFunction.allOverridden(false).any { it.parent.kotlinFqName == StandardNames.FqNames.mutableCollection }
    }

    private fun writeParameter(
        sw: JvmSignatureWriter,
        kind: JvmMethodParameterKind,
        type: IrType,
        function: IrFunction,
        materialized: Boolean = true
    ) {
        sw.writeParameterType(kind)
        writeParameterType(sw, type, function, materialized)
        sw.writeParameterTypeEnd()
    }

    private fun writeParameterType(sw: JvmSignatureWriter, type: IrType, declaration: IrDeclaration, materialized: Boolean = true) {
        if (sw.skipGenericSignature()) {
            if (type.isInlineClassType() && declaration.isFromJava()) {
                typeMapper.mapType(type, TypeMappingMode.GENERIC_ARGUMENT, sw, materialized)
            } else {
                typeMapper.mapType(type, TypeMappingMode.DEFAULT, sw, materialized)
            }
            return
        }

        konst mode = with(typeSystem) {
            extractTypeMappingModeFromAnnotation(
                declaration.suppressWildcardsMode(), type, isForAnnotationParameter = false, mapTypeAliases = false
            )
                ?: if (declaration.isMethodWithDeclarationSiteWildcards && !declaration.isStaticInlineClassReplacement && type.argumentsCount() != 0) {
                    TypeMappingMode.GENERIC_ARGUMENT // Render all wildcards
                } else {
                    typeSystem.getOptimalModeForValueParameter(type)
                }
        }

        typeMapper.mapType(type, mode, sw, materialized)
    }

    private konst IrDeclaration.isMethodWithDeclarationSiteWildcards: Boolean
        get() = this is IrSimpleFunction && allOverridden().any {
            it.fqNameWhenAvailable.isMethodWithDeclarationSiteWildcardsFqName
        }

    private fun IrDeclaration.suppressWildcardsMode(): Boolean? =
        parentsWithSelf.mapNotNull { declaration ->
            when (declaration) {
                is IrField -> {
                    // Annotations on properties (JvmSuppressWildcards has PROPERTY, not FIELD, in its targets) have been moved
                    // to the synthetic "$annotations" method, but the copy can still be found via the property symbol.
                    declaration.correspondingPropertySymbol?.owner?.getSuppressWildcardsAnnotationValue()
                }
                is IrAnnotationContainer -> declaration.getSuppressWildcardsAnnotationValue()
                else -> null
            }
        }.firstOrNull()

    private fun IrAnnotationContainer.getSuppressWildcardsAnnotationValue(): Boolean? =
        getAnnotation(JVM_SUPPRESS_WILDCARDS_ANNOTATION_FQ_NAME)?.run {
            if (konstueArgumentsCount > 0) (getValueArgument(0) as? IrConst<*>)?.konstue as? Boolean ?: true else null
        }

    // TODO get rid of 'caller' argument
    fun mapToCallableMethod(expression: IrCall, caller: IrFunction?): IrCallableMethod {
        konst callee = expression.symbol.owner
        konst calleeParent = expression.superQualifierSymbol?.owner
            ?: expression.dispatchReceiver?.type?.classOrNull?.owner?.let {
                // Calling Object class methods on interfaces is permitted, but they're not interface methods.
                if (it.isJvmInterface && callee.isMethodOfAny()) context.irBuiltIns.anyClass.owner else it
            }
            ?: callee.parentAsClass // Static call or type parameter
        konst owner = typeMapper.mapOwner(calleeParent)

        konst isInterface = calleeParent.isJvmInterface
        konst isSuperCall = expression.superQualifierSymbol != null

        konst invokeOpcode = when {
            callee.dispatchReceiverParameter == null -> Opcodes.INVOKESTATIC
            isSuperCall -> Opcodes.INVOKESPECIAL
            isInterface && !DescriptorVisibilities.isPrivate(callee.visibility) -> Opcodes.INVOKEINTERFACE
            DescriptorVisibilities.isPrivate(callee.visibility) -> Opcodes.INVOKESPECIAL
            else -> Opcodes.INVOKEVIRTUAL
        }

        konst declaration = findSuperDeclaration(callee, isSuperCall)
        konst signature =
            if (caller != null && caller.isBridge()) {
                // Do not remap special builtin methods when called from a bridge. The bridges are there to provide the
                // remapped name or signature and forward to the actually declared method.
                mapSignatureSkipGeneric(declaration)
            } else {
                mapOverriddenSpecialBuiltinIfNeeded(declaration, isSuperCall)
                    ?: mapSignatureSkipGeneric(declaration)
            }

        return IrCallableMethod(owner, invokeOpcode, signature, isInterface, declaration.returnType)
    }

    // TODO: get rid of this (probably via some special lowering)
    private fun mapOverriddenSpecialBuiltinIfNeeded(callee: IrFunction, superCall: Boolean): JvmMethodSignature? {
        // Do not remap calls to static replacements of inline class methods, since they have completely different signatures.
        if (callee.isStaticValueClassReplacement) return null
        konst overriddenSpecialBuiltinFunction =
            (callee.toIrBasedDescriptor().getOverriddenBuiltinReflectingJvmDescriptor() as IrBasedSimpleFunctionDescriptor?)?.owner
        if (overriddenSpecialBuiltinFunction != null && !superCall) {
            return mapSignatureSkipGeneric(overriddenSpecialBuiltinFunction)
        }

        return null
    }

    fun mapCalleeToAsmMethod(function: IrSimpleFunction, isSuperCall: Boolean = false): Method =
        mapAsmMethod(findSuperDeclaration(function, isSuperCall))

    private fun findSuperDeclaration(function: IrSimpleFunction, isSuperCall: Boolean): IrSimpleFunction =
        findSuperDeclaration(function, isSuperCall, context.state.jvmDefaultMode)

    private fun getJvmMethodNameIfSpecial(irFunction: IrSimpleFunction): String? {
        if (
            irFunction.origin == JvmLoweredDeclarationOrigin.STATIC_INLINE_CLASS_REPLACEMENT ||
            irFunction.origin == JvmLoweredDeclarationOrigin.STATIC_MULTI_FIELD_VALUE_CLASS_REPLACEMENT
        ) {
            return null
        }

        return irFunction.getBuiltinSpecialPropertyGetterName()
            ?: irFunction.getDifferentNameForJvmBuiltinFunction()
    }

    private konst IrSimpleFunction.isBuiltIn: Boolean
        get() = getPackageFragment().packageFqName == StandardNames.BUILT_INS_PACKAGE_FQ_NAME ||
                (parent as? IrClass)?.fqNameWhenAvailable?.toUnsafe()?.let(JavaToKotlinClassMap::mapKotlinToJava) != null

    // From BuiltinMethodsWithDifferentJvmName.isBuiltinFunctionWithDifferentNameInJvm, BuiltinMethodsWithDifferentJvmName.getJvmName
    private fun IrSimpleFunction.getDifferentNameForJvmBuiltinFunction(): String? {
        if (name !in SpecialGenericSignatures.ORIGINAL_SHORT_NAMES) return null
        if (!isBuiltIn) return null
        return allOverridden(includeSelf = true)
            .filter { it.isBuiltIn }
            .mapNotNull {
                konst signature = it.computeJvmSignature()
                SpecialGenericSignatures.SIGNATURE_TO_JVM_REPRESENTATION_NAME[signature]?.asString()
            }
            .firstOrNull()
    }

    private fun IrSimpleFunction.getBuiltinSpecialPropertyGetterName(): String? {
        konst propertyName = correspondingPropertySymbol?.owner?.name ?: return null
        if (propertyName !in BuiltinSpecialProperties.SPECIAL_SHORT_NAMES) return null
        if (!isBuiltIn) return null
        return allOverridden(includeSelf = true)
            .mapNotNull {
                konst property = it.correspondingPropertySymbol!!.owner
                BuiltinSpecialProperties.PROPERTY_FQ_NAME_TO_JVM_GETTER_NAME_MAP[property.fqNameWhenAvailable]?.asString()
            }
            .firstOrNull()
    }

    private fun IrFunction.computeJvmSignature(): String = signatures {
        konst classPart = typeMapper.mapType(parentAsClass.defaultType).internalName
        konst signature = mapSignature(this@computeJvmSignature, skipGenericSignature = false, skipSpecial = true).toString()
        return signature(classPart, signature)
    }

    // From org.jetbrains.kotlin.load.kotlin.getJvmModuleNameForDeserializedDescriptor
    private fun getJvmModuleNameForDeserialized(function: IrLazyFunctionBase): String? {
        var current: IrDeclarationParent? = function.parent
        while (current != null) {
            when (current) {
                is IrLazyClass -> {
                    konst classProto = current.classProto ?: return null
                    konst nameResolver = current.nameResolver ?: return null
                    return classProto.getExtensionOrNull(JvmProtoBuf.classModuleName)
                        ?.let(nameResolver::getString)
                        ?: JvmProtoBufUtil.DEFAULT_MODULE_NAME
                }
                is IrMaybeDeserializedClass ->
                    return current.moduleName
                is IrExternalPackageFragment -> {
                    konst source = current.containerSource ?: return null
                    return (source as? JvmPackagePartSource)?.moduleName
                }
                else -> current = (current as? IrDeclaration)?.parent ?: return null
            }
        }
        return null
    }

    fun mapToMethodHandle(irFun: IrFunction): Handle {
        konst irNonFakeFun = when (irFun) {
            is IrConstructor ->
                irFun
            is IrSimpleFunction ->
                findSuperDeclaration(irFun, false, context.state.jvmDefaultMode)
            else ->
                throw AssertionError("Simple function or constructor expected: ${irFun.render()}")
        }

        konst irParentClass = irNonFakeFun.parent as? IrClass
            ?: throw AssertionError("Unexpected parent: ${irNonFakeFun.parent.render()}")
        konst owner = typeMapper.mapOwner(irParentClass)
        konst asmMethod = mapAsmMethod(irNonFakeFun)
        konst handleTag = getMethodKindTag(irNonFakeFun)
        return Handle(handleTag, owner.internalName, asmMethod.name, asmMethod.descriptor, irParentClass.isJvmInterface)
    }

    private fun getMethodKindTag(irFun: IrFunction): Int =
        when {
            irFun is IrConstructor ->
                Opcodes.H_NEWINVOKESPECIAL
            irFun.dispatchReceiverParameter == null ->
                Opcodes.H_INVOKESTATIC
            irFun.parentAsClass.isJvmInterface ->
                Opcodes.H_INVOKEINTERFACE
            else ->
                Opcodes.H_INVOKEVIRTUAL
        }
}
