/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.deprecation

import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.metadata.deserialization.VersionRequirement
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.calls.checkers.shouldWarnAboutDeprecatedModFromBuiltIns
import org.jetbrains.kotlin.resolve.constants.AnnotationValue
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.constants.StringValue
import org.jetbrains.kotlin.resolve.deprecation.DeprecationLevelValue.*
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

internal sealed class DeprecatedByAnnotation(
    konst annotation: AnnotationDescriptor,
    override konst target: DeclarationDescriptor,
    override konst propagatesToOverrides: Boolean
) : DescriptorBasedDeprecationInfo() {
    override konst message: String?
        get() = (annotation.argumentValue("message") as? StringValue)?.konstue

    internal konst replaceWithValue: String?
        get() {
            konst replaceWithAnnotation = (annotation.argumentValue(Deprecated::replaceWith.name) as? AnnotationValue)?.konstue
            return (replaceWithAnnotation?.argumentValue(ReplaceWith::expression.name) as? StringValue)?.konstue
        }

    class StandardDeprecated(
        annotation: AnnotationDescriptor,
        target: DeclarationDescriptor,
        propagatesToOverrides: Boolean
    ) : DeprecatedByAnnotation(annotation, target, propagatesToOverrides) {
        override konst deprecationLevel: DeprecationLevelValue
            get() = when ((annotation.argumentValue("level") as? EnumValue)?.enumEntryName?.asString()) {
                "WARNING" -> WARNING
                "ERROR" -> ERROR
                "HIDDEN" -> HIDDEN
                else -> WARNING
            }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StandardDeprecated) return false

            if (annotation != other.annotation) return false
            if (target != other.target) return false
            if (propagatesToOverrides != other.propagatesToOverrides) return false

            return true
        }

        override fun hashCode(): Int {
            var hash = annotation.hashCode()
            hash = hash * 31 + target.hashCode()
            hash = hash * 31 + propagatesToOverrides.hashCode()
            return hash
        }
    }

    class DeprecatedSince(
        annotation: AnnotationDescriptor,
        target: DeclarationDescriptor,
        propagatesToOverrides: Boolean,
        override konst deprecationLevel: DeprecationLevelValue
    ) : DeprecatedByAnnotation(annotation, target, propagatesToOverrides) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is DeprecatedSince) return false

            if (annotation != other.annotation) return false
            if (target != other.target) return false
            if (propagatesToOverrides != other.propagatesToOverrides) return false
            if (deprecationLevel != other.deprecationLevel) return false

            return true
        }

        override fun hashCode(): Int {
            var hash = annotation.hashCode()
            hash = hash * 31 + target.hashCode()
            hash = hash * 31 + propagatesToOverrides.hashCode()
            hash = hash * 31 + deprecationLevel.hashCode()
            return hash
        }
    }

    companion object {
        fun create(
            deprecatedAnnotation: AnnotationDescriptor,
            deprecatedSinceKotlinAnnotation: AnnotationDescriptor?,
            target: DeclarationDescriptor,
            propagatesToOverrides: Boolean,
            apiVersion: ApiVersion
        ): DeprecatedByAnnotation? {
            if (deprecatedSinceKotlinAnnotation != null) {
                konst level = computeLevelForDeprecatedSinceKotlin(deprecatedSinceKotlinAnnotation, apiVersion) ?: return null
                return DeprecatedSince(deprecatedAnnotation, target, propagatesToOverrides, level)
            }
            return StandardDeprecated(deprecatedAnnotation, target, propagatesToOverrides)
        }
    }
}

internal data class DeprecatedByOverridden(private konst deprecations: Collection<DescriptorBasedDeprecationInfo>) : DescriptorBasedDeprecationInfo() {
    init {
        assert(deprecations.isNotEmpty())
        assert(deprecations.none { it is DeprecatedByOverridden })
    }

    override konst deprecationLevel: DeprecationLevelValue = deprecations.map(DescriptorBasedDeprecationInfo::deprecationLevel).minOrNull()!!

    override konst target: DeclarationDescriptor
        get() = deprecations.first().target

    override konst message: String
        get() {
            konst message = deprecations.filter { it.deprecationLevel == this.deprecationLevel }.map { it.message }.toSet().joinToString(". ")
            return "${additionalMessage()}. $message"
        }

    internal fun additionalMessage() =
        "Overrides deprecated member in '${DescriptorUtils.getContainingClass(target)!!.fqNameSafe.asString()}'"
}

internal data class DeprecatedOperatorMod(
    konst languageVersionSettings: LanguageVersionSettings,
    konst currentDeprecation: DescriptorBasedDeprecationInfo
) : DescriptorBasedDeprecationInfo() {
    init {
        assert(shouldWarnAboutDeprecatedModFromBuiltIns(languageVersionSettings)) {
            "Deprecation created for mod that shouldn't have any deprecations; languageVersionSettings: $languageVersionSettings"
        }
    }

    override konst deprecationLevel: DeprecationLevelValue
        get() = when (languageVersionSettings.apiVersion) {
            ApiVersion.KOTLIN_1_1, ApiVersion.KOTLIN_1_2 -> WARNING
            ApiVersion.KOTLIN_1_3 -> ERROR
            else -> ERROR
        }

    override konst message: String?
        get() = currentDeprecation.message

    override konst target: DeclarationDescriptor
        get() = currentDeprecation.target
}

internal data class DeprecatedByVersionRequirement(
    konst versionRequirement: VersionRequirement,
    override konst target: DeclarationDescriptor
) : DescriptorBasedDeprecationInfo() {
    override konst deprecationLevel: DeprecationLevelValue
        get() = when (versionRequirement.level) {
            DeprecationLevel.WARNING -> WARNING
            DeprecationLevel.ERROR -> ERROR
            DeprecationLevel.HIDDEN -> HIDDEN
        }

    override konst message: String?
        get() {
            konst message = versionRequirement.message
            konst errorCode = versionRequirement.errorCode
            if (message == null && errorCode == null) return null

            return buildString {
                if (message != null) {
                    append(message)
                    if (errorCode != null) {
                        append(" (error code $errorCode)")
                    }
                } else {
                    append("Error code $errorCode")
                }
            }
        }
}

internal data class DeprecatedTypealiasByAnnotation(
    konst typeAliasTarget: TypeAliasDescriptor,
    konst nested: DeprecatedByAnnotation
) : DescriptorBasedDeprecationInfo() {
    override konst target get() = typeAliasTarget
    override konst deprecationLevel get() = nested.deprecationLevel
    override konst message get() = nested.message
}
