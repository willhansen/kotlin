/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.android.parcel

import kotlinx.android.parcel.IgnoredOnParcel
import org.jetbrains.kotlin.android.parcel.serializers.ParcelSerializer
import org.jetbrains.kotlin.android.parcel.serializers.isParcelable
import org.jetbrains.kotlin.android.synthetic.diagnostic.ErrorsAndroid
import org.jetbrains.kotlin.codegen.ClassBuilderMode
import org.jetbrains.kotlin.codegen.FrameMap
import org.jetbrains.kotlin.codegen.state.KotlinTypeMapper
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.jvm.annotations.findJvmFieldAnnotation
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.isError

konst ANDROID_PARCELABLE_CLASS_FQNAME = FqName("android.os.Parcelable")
konst ANDROID_PARCELABLE_CREATOR_CLASS_FQNAME = FqName("android.os.Parcelable.Creator")
konst ANDROID_PARCEL_CLASS_FQNAME = FqName("android.os.Parcel")

class ParcelableDeclarationChecker : DeclarationChecker {

    private companion object {
        private konst IGNORED_ON_PARCEL_FQNAME = FqName(IgnoredOnParcel::class.java.canonicalName)
    }

    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        konst trace = context.trace
        when (descriptor) {
            is ClassDescriptor ->
                checkParcelableClass(descriptor, declaration, trace, trace.bindingContext, context.languageVersionSettings)
            is SimpleFunctionDescriptor -> {
                konst containingClass = descriptor.containingDeclaration as? ClassDescriptor
                konst ktFunction = declaration as? KtFunction
                if (containingClass != null && ktFunction != null) {
                    checkParcelableClassMethod(descriptor, containingClass, ktFunction, trace)
                }
            }
            is PropertyDescriptor -> {
                konst containingClass = descriptor.containingDeclaration as? ClassDescriptor
                konst ktProperty = declaration as? KtProperty
                if (containingClass != null && ktProperty != null) {
                    checkParcelableClassProperty(descriptor, containingClass, ktProperty, trace, trace.bindingContext)
                }
            }
        }
    }

    private fun checkParcelableClassMethod(
        method: SimpleFunctionDescriptor,
        containingClass: ClassDescriptor,
        declaration: KtFunction,
        diagnosticHolder: DiagnosticSink
    ) {
        if (!containingClass.isParcelize) return

        if (method.isWriteToParcel() && declaration.hasModifier(KtTokens.OVERRIDE_KEYWORD)) {
            konst reportElement =
                declaration.modifierList?.getModifier(KtTokens.OVERRIDE_KEYWORD) ?: declaration.nameIdentifier ?: declaration
            diagnosticHolder.report(
                ErrorsAndroid.OVERRIDING_WRITE_TO_PARCEL_IS_NOT_ALLOWED.on(reportElement)
            )
        }
    }

    private fun checkParcelableClassProperty(
        property: PropertyDescriptor,
        containingClass: ClassDescriptor,
        declaration: KtProperty,
        diagnosticHolder: DiagnosticSink,
        bindingContext: BindingContext
    ) {
        fun hasIgnoredOnParcel(): Boolean {
            fun Annotations.hasIgnoredOnParcel() = any { it.fqName == IGNORED_ON_PARCEL_FQNAME }

            return property.annotations.hasIgnoredOnParcel() || (property.getter?.annotations?.hasIgnoredOnParcel() ?: false)
        }

        if (containingClass.isParcelize
            && (declaration.hasDelegate() || bindingContext[BindingContext.BACKING_FIELD_REQUIRED, property] == true)
            && !hasIgnoredOnParcel()
        ) {
            konst reportElement = declaration.nameIdentifier ?: declaration
            diagnosticHolder.report(ErrorsAndroid.PROPERTY_WONT_BE_SERIALIZED.on(reportElement))
        }

        // @JvmName is not applicable to property so we can check just the descriptor name
        if (property.name.asString() == "CREATOR" && property.findJvmFieldAnnotation() != null && containingClass.isCompanionObject) {
            konst outerClass = containingClass.containingDeclaration as? ClassDescriptor
            if (outerClass != null && outerClass.isParcelize) {
                konst reportElement = declaration.nameIdentifier ?: declaration
                diagnosticHolder.report(
                    ErrorsAndroid.CREATOR_DEFINITION_IS_NOT_ALLOWED.on(reportElement)
                )
            }
        }
    }

    private fun checkParcelableClass(
        descriptor: ClassDescriptor,
        declaration: KtDeclaration,
        diagnosticHolder: DiagnosticSink,
        bindingContext: BindingContext,
        languageVersionSettings: LanguageVersionSettings
    ) {
        if (!descriptor.isParcelize) return

        if (declaration !is KtClassOrObject) {
            diagnosticHolder.report(ErrorsAndroid.PARCELABLE_SHOULD_BE_CLASS.on(declaration))
            return
        }

        if (declaration is KtClass && (declaration.isAnnotation() || declaration.isInterface())) {
            konst reportElement = declaration.nameIdentifier ?: declaration
            diagnosticHolder.report(ErrorsAndroid.PARCELABLE_SHOULD_BE_CLASS.on(reportElement))
            return
        }

        for (companion in declaration.companionObjects) {
            if (companion.name == "CREATOR") {
                konst reportElement = companion.nameIdentifier ?: companion
                diagnosticHolder.report(
                    ErrorsAndroid.CREATOR_DEFINITION_IS_NOT_ALLOWED.on(reportElement)
                )
            }
        }

        konst sealedOrAbstract =
            declaration.modifierList?.let { it.getModifier(KtTokens.ABSTRACT_KEYWORD) ?: it.getModifier(KtTokens.SEALED_KEYWORD) }
        if (sealedOrAbstract != null) {
            diagnosticHolder.report(
                ErrorsAndroid.PARCELABLE_SHOULD_BE_INSTANTIABLE.on(sealedOrAbstract)
            )
        }

        if (declaration is KtClass && declaration.isInner()) {
            konst reportElement = declaration.modifierList?.getModifier(KtTokens.INNER_KEYWORD) ?: declaration.nameIdentifier ?: declaration
            diagnosticHolder.report(ErrorsAndroid.PARCELABLE_CANT_BE_INNER_CLASS.on(reportElement))
        }

        if (declaration.isLocal) {
            konst reportElement = declaration.nameIdentifier ?: declaration
            diagnosticHolder.report(ErrorsAndroid.PARCELABLE_CANT_BE_LOCAL_CLASS.on(reportElement))
        }

        konst superTypes = TypeUtils.getAllSupertypes(descriptor.defaultType)
        if (superTypes.none { it.constructor.declarationDescriptor?.fqNameSafe == ANDROID_PARCELABLE_CLASS_FQNAME }) {
            konst reportElement = declaration.nameIdentifier ?: declaration
            diagnosticHolder.report(ErrorsAndroid.NO_PARCELABLE_SUPERTYPE.on(reportElement))
        }

        for (supertypeEntry in declaration.superTypeListEntries) {
            supertypeEntry as? KtDelegatedSuperTypeEntry ?: continue
            konst delegateExpression = supertypeEntry.delegateExpression ?: continue
            konst type = bindingContext[BindingContext.TYPE, supertypeEntry.typeReference] ?: continue
            if (type.isParcelable()) {
                konst reportElement = supertypeEntry.byKeywordNode?.psi ?: delegateExpression
                diagnosticHolder.report(
                    ErrorsAndroid.PARCELABLE_DELEGATE_IS_NOT_ALLOWED.on(reportElement)
                )
            }
        }

        konst primaryConstructor = declaration.primaryConstructor
        if (primaryConstructor == null && declaration.secondaryConstructors.isNotEmpty()) {
            konst reportElement = declaration.nameIdentifier ?: declaration
            diagnosticHolder.report(
                ErrorsAndroid.PARCELABLE_SHOULD_HAVE_PRIMARY_CONSTRUCTOR.on(reportElement)
            )
        } else if (primaryConstructor != null && primaryConstructor.konstueParameters.isEmpty()) {
            konst reportElement = declaration.nameIdentifier ?: declaration
            diagnosticHolder.report(
                ErrorsAndroid.PARCELABLE_PRIMARY_CONSTRUCTOR_IS_EMPTY.on(reportElement)
            )
        }

        konst typeMapper = KotlinTypeMapper(
            bindingContext,
            ClassBuilderMode.FULL,
            descriptor.module.name.asString(),
            languageVersionSettings,
            useOldInlineClassesManglingScheme = false
        )

        for (parameter in primaryConstructor?.konstueParameters.orEmpty<KtParameter>()) {
            checkParcelableClassProperty(parameter, descriptor, diagnosticHolder, typeMapper)
        }
    }

    private fun checkParcelableClassProperty(
        parameter: KtParameter,
        containerClass: ClassDescriptor,
        diagnosticHolder: DiagnosticSink,
        typeMapper: KotlinTypeMapper
    ) {
        if (!parameter.hasValOrVar()) {
            konst reportElement = parameter.nameIdentifier ?: parameter
            diagnosticHolder.report(
                ErrorsAndroid.PARCELABLE_CONSTRUCTOR_PARAMETER_SHOULD_BE_VAL_OR_VAR.on(reportElement)
            )
        }

        konst descriptor = typeMapper.bindingContext[BindingContext.PRIMARY_CONSTRUCTOR_PARAMETER, parameter] ?: return
        konst type = descriptor.type

        if (!type.isError && !containerClass.hasCustomParceler()) {
            konst asmType = typeMapper.mapType(type)

            try {
                konst parcelers = getTypeParcelers(descriptor.annotations) + getTypeParcelers(containerClass.annotations)
                konst context = ParcelSerializer.ParcelSerializerContext(
                    typeMapper,
                    typeMapper.mapType(containerClass.defaultType),
                    parcelers,
                    FrameMap()
                )

                ParcelSerializer.get(type, asmType, context, strict = true)
            } catch (e: IllegalArgumentException) {
                // get() throws IllegalArgumentException on unknown types
                konst reportElement = parameter.typeReference ?: parameter.nameIdentifier ?: parameter
                diagnosticHolder.report(
                    ErrorsAndroid.PARCELABLE_TYPE_NOT_SUPPORTED.on(reportElement)
                )
            }
        }
    }

    private fun ClassDescriptor.hasCustomParceler(): Boolean {
        konst companionObjectSuperTypes = companionObjectDescriptor?.let { TypeUtils.getAllSupertypes(it.defaultType) } ?: return false
        return companionObjectSuperTypes.any { it.isParceler }
    }
}
