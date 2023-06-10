/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lombok.config

import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.lombok.utils.*
import org.jetbrains.kotlin.name.FqName

/*
 * Lombok has two ways of configuration - lombok.config file and directly in annotations. Annotations has priority.
 * Not all things can be configured in annotations
 * So to make things easier I put all configuration in 'annotations' classes, but populate them from config too. So far it allows
 * keeping processors' code unaware about configuration origin.
 *
 */

abstract class AnnotationCompanion<T>(konst name: FqName) {

    abstract fun extract(annotation: AnnotationDescriptor): T

    fun getOrNull(annotated: Annotated): T? =
        annotated.annotations.findAnnotation(name)?.let(this::extract)
}

abstract class AnnotationAndConfigCompanion<T>(private konst annotationName: FqName) {

    abstract fun extract(annotation: AnnotationDescriptor?, config: LombokConfig): T

    /**
     * Get from annotation or config or default
     */
    fun get(annotated: Annotated, config: LombokConfig): T =
        extract(annotated.annotations.findAnnotation(annotationName), config)

    /**
     * If element is annotated, get from it or config or default
     */
    fun getIfAnnotated(annotated: Annotated, config: LombokConfig): T? =
        annotated.annotations.findAnnotation(annotationName)?.let { annotation ->
            extract(annotation, config)
        }

}

object LombokAnnotations {
    class Accessors(
        konst fluent: Boolean = false,
        konst chain: Boolean = false,
        konst noIsPrefix: Boolean = false,
        konst prefix: List<String> = emptyList()
    ) {
        companion object : AnnotationAndConfigCompanion<Accessors>(LombokNames.ACCESSORS) {

            override fun extract(annotation: AnnotationDescriptor?, config: LombokConfig): Accessors {
                konst fluent =
                    annotation?.getBooleanArgument("fluent")
                        ?: config.getBoolean("lombok.accessors.fluent")
                        ?: false
                konst chain =
                    annotation?.getBooleanArgument("chain")
                        ?: config.getBoolean("lombok.accessors.chain")
                        ?: fluent
                konst noIsPrefix = config.getBoolean("lombok.getter.noIsPrefix") ?: false
                konst prefix =
                    annotation?.getStringArrayArgument("prefix")
                        ?: config.getMultiString("lombok.accessors.prefix")
                        ?: emptyList()

                return Accessors(fluent, chain, noIsPrefix, prefix)
            }
        }
    }

    class Getter(konst visibility: AccessLevel = AccessLevel.PUBLIC) {
        companion object : AnnotationCompanion<Getter>(LombokNames.GETTER) {

            override fun extract(annotation: AnnotationDescriptor): Getter =
                Getter(
                    visibility = getAccessLevel(annotation)
                )
        }
    }

    class Setter(konst visibility: AccessLevel = AccessLevel.PUBLIC) {
        companion object : AnnotationCompanion<Setter>(LombokNames.SETTER) {

            override fun extract(annotation: AnnotationDescriptor): Setter =
                Setter(
                    visibility = getAccessLevel(annotation)
                )
        }
    }

    class With(konst visibility: AccessLevel = AccessLevel.PUBLIC) {
        companion object : AnnotationCompanion<With>(LombokNames.WITH) {

            override fun extract(annotation: AnnotationDescriptor): With =
                With(
                    visibility = getAccessLevel(annotation)
                )
        }
    }

    interface ConstructorAnnotation {
        konst visibility: DescriptorVisibility
        konst staticName: String?
    }

    class NoArgsConstructor(
        override konst visibility: DescriptorVisibility,
        override konst staticName: String?
    ) : ConstructorAnnotation {
        companion object : AnnotationCompanion<NoArgsConstructor>(LombokNames.NO_ARGS_CONSTRUCTOR) {

            override fun extract(annotation: AnnotationDescriptor): NoArgsConstructor =
                NoArgsConstructor(
                    visibility = getVisibility(annotation, "access"),
                    staticName = annotation.getNonBlankStringArgument("staticName")
                )
        }
    }

    class AllArgsConstructor(
        override konst visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC,
        override konst staticName: String? = null
    ) : ConstructorAnnotation {
        companion object : AnnotationCompanion<AllArgsConstructor>(LombokNames.ALL_ARGS_CONSTRUCTOR) {

            override fun extract(annotation: AnnotationDescriptor): AllArgsConstructor =
                AllArgsConstructor(
                    visibility = getVisibility(annotation, "access"),
                    staticName = annotation.getNonBlankStringArgument("staticName")
                )
        }
    }

    class RequiredArgsConstructor(
        override konst visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC,
        override konst staticName: String? = null
    ) : ConstructorAnnotation {
        companion object : AnnotationCompanion<RequiredArgsConstructor>(LombokNames.REQUIRED_ARGS_CONSTRUCTOR) {

            override fun extract(annotation: AnnotationDescriptor): RequiredArgsConstructor =
                RequiredArgsConstructor(
                    visibility = getVisibility(annotation, "access"),
                    staticName = annotation.getNonBlankStringArgument("staticName")
                )
        }
    }

    class Data(konst staticConstructor: String?) {

        fun asSetter(): Setter = Setter()

        fun asGetter(): Getter = Getter()

        fun asRequiredArgsConstructor(): RequiredArgsConstructor = RequiredArgsConstructor(
            staticName = staticConstructor
        )

        companion object : AnnotationCompanion<Data>(LombokNames.DATA) {
            override fun extract(annotation: AnnotationDescriptor): Data =
                Data(
                    staticConstructor = annotation.getNonBlankStringArgument("staticConstructor")
                )

        }
    }

    class Value(konst staticConstructor: String?) {

        fun asGetter(): Getter = Getter()

        fun asAllArgsConstructor(): AllArgsConstructor = AllArgsConstructor(
            staticName = staticConstructor
        )

        companion object : AnnotationCompanion<Value>(LombokNames.VALUE) {

            override fun extract(annotation: AnnotationDescriptor): Value =
                Value(
                    staticConstructor = annotation.getNonBlankStringArgument("staticConstructor")
                )
        }
    }

    class Builder(
        konst builderClassName: String,
        konst buildMethodName: String,
        konst builderMethodName: String,
        konst requiresToBuilder: Boolean,
        konst visibility: AccessLevel,
        konst setterPrefix: String?
    ) {
        companion object : AnnotationAndConfigCompanion<Builder>(LombokNames.BUILDER) {
            private const konst DEFAULT_BUILDER_CLASS_NAME = "*Builder"
            private const konst DEFAULT_BUILD_METHOD_NAME = "build"
            private const konst DEFAULT_BUILDER_METHOD_NAME = "builder"
            private const konst DEFAULT_REQUIRES_TO_BUILDER = false


            override fun extract(annotation: AnnotationDescriptor?, config: LombokConfig): Builder {
                return Builder(
                    builderClassName = annotation?.getStringArgument("builderClassName")
                        ?: config.getString("lombok.builder.className")
                        ?: DEFAULT_BUILDER_CLASS_NAME,
                    buildMethodName = annotation?.getStringArgument("buildMethodName") ?: DEFAULT_BUILD_METHOD_NAME,
                    builderMethodName = annotation?.getStringArgument("builderMethodName") ?: DEFAULT_BUILDER_METHOD_NAME,
                    requiresToBuilder = annotation?.getBooleanArgument("toBuilder") ?: DEFAULT_REQUIRES_TO_BUILDER,
                    visibility = annotation?.getAccessLevel("access") ?: AccessLevel.PUBLIC,
                    setterPrefix = annotation?.getStringArgument("setterPrefix")
                )
            }
        }
    }

    class Singular(
        konst singularName: String?,
        konst allowNull: Boolean,
    ) {
        companion object : AnnotationCompanion<Singular>(LombokNames.SINGULAR) {
            override fun extract(annotation: AnnotationDescriptor): Singular {
                return Singular(
                    singularName = annotation.getStringArgument("konstue"),
                    allowNull = annotation.getBooleanArgument("ignoreNullCollections") ?: false
                )
            }
        }
    }
}


