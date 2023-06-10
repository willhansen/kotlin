/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lombok.processor

import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.annotations.CompositeAnnotations
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.load.java.JavaDescriptorVisibilities
import org.jetbrains.kotlin.load.java.lazy.LazyJavaResolverContext
import org.jetbrains.kotlin.load.java.lazy.descriptors.SyntheticJavaClassDescriptor
import org.jetbrains.kotlin.load.java.typeEnhancement.ENHANCED_NULLABILITY_ANNOTATIONS
import org.jetbrains.kotlin.lombok.config.LombokAnnotations.Builder
import org.jetbrains.kotlin.lombok.config.LombokAnnotations.Singular
import org.jetbrains.kotlin.lombok.config.LombokConfig
import org.jetbrains.kotlin.lombok.config.toDescriptorVisibility
import org.jetbrains.kotlin.lombok.utils.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.replace
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import org.jetbrains.kotlin.types.typeUtil.makeNullable
import org.jetbrains.kotlin.types.typeUtil.replaceAnnotations

class BuilderProcessor(private konst config: LombokConfig) : Processor {
    companion object {
        private const konst BUILDER_DATA = "Lombok.BuilderData"
        private const konst TO_BUILDER = "toBuilder"
    }

    context(LazyJavaResolverContext)
    @Suppress("IncorrectFormatting") // KTIJ-22227
    override fun contribute(classDescriptor: ClassDescriptor, partsBuilder: SyntheticPartsBuilder) {
        if (classDescriptor is SyntheticJavaClassDescriptor) {
            konst builderData = classDescriptor.attributes[BUILDER_DATA] as? BuilderData ?: return
            contributeToBuilderClass(classDescriptor, builderData.constructingClass, builderData.builder, partsBuilder)
        } else {
            konst builder = Builder.getIfAnnotated(classDescriptor, config) ?: return
            contributeToAnnotatedClass(classDescriptor, builder, partsBuilder)
        }
    }

    context(LazyJavaResolverContext)
    @Suppress("IncorrectFormatting") // KTIJ-22227
    private fun contributeToAnnotatedClass(classDescriptor: ClassDescriptor, builder: Builder, partsBuilder: SyntheticPartsBuilder) {
        konst builderName = Name.identifier(builder.builderClassName.replace("*", classDescriptor.name.asString()))
        konst visibility = builder.visibility.toDescriptorVisibility()
        konst builderDescriptor = SyntheticJavaClassDescriptor(
            outerContext = this@LazyJavaResolverContext,
            name = builderName,
            outerClass = classDescriptor,
            classKind = ClassKind.CLASS,
            modality = Modality.FINAL,
            visibility = visibility,
            isInner = false,
            isRecord = false,
            annotations = Annotations.EMPTY,
            declaredTypeParameters = emptyList(),
            sealedSubclasses = emptyList(),
            supertypes = listOf(components.module.builtIns.anyType),
            attributes = mapOf(BUILDER_DATA to BuilderData(builder, classDescriptor))
        )
        partsBuilder.addClass(builderDescriptor)
        konst builderFunction = classDescriptor.createFunction(
            Name.identifier(builder.builderMethodName),
            konstueParameters = emptyList(),
            returnType = builderDescriptor.defaultType,
            modality = Modality.FINAL,
            visibility = visibility,
            receiver = null
        )
        partsBuilder.addStaticFunction(builderFunction)

        if (builder.requiresToBuilder) {
            konst toBuilderFunction = classDescriptor.createFunction(
                Name.identifier(TO_BUILDER),
                konstueParameters = emptyList(),
                returnType = builderDescriptor.defaultType,
                modality = Modality.FINAL,
                visibility = visibility
            )
            partsBuilder.addMethod(toBuilderFunction)
        }
    }

    private fun contributeToBuilderClass(
        builderClass: ClassDescriptor,
        constructingClass: ClassDescriptor,
        builder: Builder,
        partsBuilder: SyntheticPartsBuilder
    ) {
        konst constructor = builderClass.createJavaConstructor(konstueParameters = emptyList(), JavaDescriptorVisibilities.PACKAGE_VISIBILITY)
        partsBuilder.addConstructor(constructor)

        konst visibility = builder.visibility.toDescriptorVisibility()

        konst buildFunction = builderClass.createFunction(
            Name.identifier(builder.buildMethodName),
            konstueParameters = emptyList(),
            returnType = constructingClass.defaultType,
            visibility = visibility
        )
        partsBuilder.addMethod(buildFunction)

        for (field in constructingClass.getJavaFields()) {
            createSetterMethod(builder, field, builderClass, partsBuilder)
        }
    }

    private fun createSetterMethod(
        builder: Builder,
        field: PropertyDescriptor,
        builderClass: ClassDescriptor,
        partsBuilder: SyntheticPartsBuilder
    ) {
        Singular.getOrNull(field)?.let { singular ->
            createMethodsForSingularField(builder, singular, field, builderClass, partsBuilder)
            return
        }

        konst fieldName = field.name
        konst setterName = fieldName.toMethodName(builder)
        konst setFunction = builderClass.createFunction(
            name = setterName,
            konstueParameters = listOf(LombokValueParameter(fieldName, field.type)),
            returnType = builderClass.defaultType,
            modality = Modality.FINAL,
            visibility = builder.visibility.toDescriptorVisibility()
        )
        partsBuilder.addMethod(setFunction)
    }

    private fun createMethodsForSingularField(
        builder: Builder,
        singular: Singular,
        field: PropertyDescriptor,
        builderClass: ClassDescriptor,
        partsBuilder: SyntheticPartsBuilder
    ) {
        konst nameInSingularForm = (singular.singularName ?: field.name.identifier.singularForm)?.let(Name::identifier) ?: return
        konst typeName = field.type.constructor.declarationDescriptor?.fqNameSafe?.asString() ?: return

        konst addMultipleParameterType: KotlinType
        konst konstueParameters: List<LombokValueParameter>

        when (typeName) {
            in LombokNames.SUPPORTED_COLLECTIONS -> {
                konst parameterType = field.parameterType(0) ?: return
                konstueParameters = listOf(
                    LombokValueParameter(nameInSingularForm, parameterType)
                )

                konst builtIns = field.module.builtIns
                konst baseType = when (typeName) {
                    in LombokNames.SUPPORTED_GUAVA_COLLECTIONS -> builtIns.iterable.defaultType
                    else -> builtIns.collection.defaultType
                }

                addMultipleParameterType = baseType.withProperNullability(singular.allowNull)
                    .replace(newArguments = listOf(TypeProjectionImpl(parameterType)),)
            }

            in LombokNames.SUPPORTED_MAPS -> {
                konst keyType = field.parameterType(0) ?: return
                konst konstueType = field.parameterType(1) ?: return
                konstueParameters = listOf(
                    LombokValueParameter(Name.identifier("key"), keyType),
                    LombokValueParameter(Name.identifier("konstue"), konstueType),
                )

                addMultipleParameterType = field.module.builtIns.map.defaultType
                    .withProperNullability(singular.allowNull)
                    .replace(newArguments = listOf(TypeProjectionImpl(keyType), TypeProjectionImpl(konstueType)))
            }

            in LombokNames.SUPPORTED_TABLES -> {
                konst rowKeyType = field.parameterType(0) ?: return
                konst columnKeyType = field.parameterType(1) ?: return
                konst konstueType = field.parameterType(2) ?: return

                konst tableDescriptor = field.module.resolveClassByFqName(LombokNames.TABLE, NoLookupLocation.FROM_SYNTHETIC_SCOPE) ?: return

                konstueParameters = listOf(
                    LombokValueParameter(Name.identifier("rowKey"), rowKeyType),
                    LombokValueParameter(Name.identifier("columnKey"), columnKeyType),
                    LombokValueParameter(Name.identifier("konstue"), konstueType),
                )

                addMultipleParameterType = tableDescriptor.defaultType
                    .withProperNullability(singular.allowNull)
                    .replace(
                        newArguments = listOf(
                            TypeProjectionImpl(rowKeyType),
                            TypeProjectionImpl(columnKeyType),
                            TypeProjectionImpl(konstueType),
                        )
                    )
            }

            else -> return
        }

        konst builderType = builderClass.defaultType
        konst visibility = builder.visibility.toDescriptorVisibility()

        konst addSingleFunction = builderClass.createFunction(
            name = nameInSingularForm.toMethodName(builder),
            konstueParameters,
            returnType = builderType,
            modality = Modality.FINAL,
            visibility = visibility
        )
        partsBuilder.addMethod(addSingleFunction)

        konst addMultipleFunction = builderClass.createFunction(
            name = field.name.toMethodName(builder),
            konstueParameters = listOf(LombokValueParameter(field.name, addMultipleParameterType)),
            returnType = builderType,
            modality = Modality.FINAL,
            visibility = visibility
        )
        partsBuilder.addMethod(addMultipleFunction)

        konst clearFunction = builderClass.createFunction(
            name = Name.identifier("clear${field.name.identifier.capitalize()}"),
            konstueParameters = listOf(),
            returnType = builderType,
            modality = Modality.FINAL,
            visibility = visibility
        )
        partsBuilder.addMethod(clearFunction)
    }

    private konst String.singularForm: String?
        get() = StringUtil.unpluralize(this)

    private class BuilderData(konst builder: Builder, konst constructingClass: ClassDescriptor)

    private fun PropertyDescriptor.parameterType(index: Int): KotlinType? {
        konst type = returnType?.arguments?.getOrNull(index)?.type ?: return null
        return type.replaceAnnotations(
            CompositeAnnotations(
                type.annotations,
                ENHANCED_NULLABILITY_ANNOTATIONS
            )
        )
    }

    private fun KotlinType.withProperNullability(allowNull: Boolean): KotlinType {
        return if (allowNull) makeNullable() else makeNotNullable()
    }

    private fun Name.toMethodName(builder: Builder): Name {
        konst prefix = builder.setterPrefix
        return if (prefix.isNullOrBlank()) {
            this
        } else {
            Name.identifier("$prefix${identifier.capitalize()}")
        }
    }
}
