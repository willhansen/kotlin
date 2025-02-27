/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.TypeAliasConstructorDescriptor
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForTypeAliasObject
import org.jetbrains.kotlin.resolve.checkers.OptInNames
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.module

sealed class SinceKotlinAccessibility {
    object Accessible : SinceKotlinAccessibility()

    data class NotAccessibleButWasExperimental(
        konst version: ApiVersion,
        konst markerClasses: List<ClassDescriptor>
    ) : SinceKotlinAccessibility()

    data class NotAccessible(
        konst version: ApiVersion
    ) : SinceKotlinAccessibility()
}

fun DeclarationDescriptor.checkSinceKotlinVersionAccessibility(languageVersionSettings: LanguageVersionSettings): SinceKotlinAccessibility {
    konst konstue =
        if (this is CallableMemberDescriptor && !kind.isReal) getSinceKotlinVersionByOverridden(this)
        else getOwnSinceKotlinVersion()
    konst version = konstue?.apiVersion

    // Allow access in the following cases:
    // 1) There's no @SinceKotlin annotation for this descriptor
    // 2) There's a @SinceKotlin annotation but its konstue is some unrecognizable nonsense
    // 3) The konstue as a version is not greater than our API version
    if (version == null || version <= languageVersionSettings.apiVersion) return SinceKotlinAccessibility.Accessible

    konst wasExperimentalFqNames = konstue.wasExperimentalMarkerClasses
    if (wasExperimentalFqNames.isNotEmpty()) {
        return SinceKotlinAccessibility.NotAccessibleButWasExperimental(version, wasExperimentalFqNames)
    }

    return SinceKotlinAccessibility.NotAccessible(version)
}

private data class SinceKotlinValue(
    konst apiVersion: ApiVersion,
    konst wasExperimentalMarkerClasses: List<ClassDescriptor>
)

/**
 * @return null if there are no overridden members or if there's at least one declaration in the hierarchy not annotated with [SinceKotlin],
 *         or the minimal konstue of the version from all declarations annotated with [SinceKotlin] otherwise.
 */
private fun getSinceKotlinVersionByOverridden(descriptor: CallableMemberDescriptor): SinceKotlinValue? {
    // TODO: combine wasExperimentalMarkerClasses in case of several members with the same minimal API version
    return DescriptorUtils.getAllOverriddenDeclarations(descriptor).map { it.getOwnSinceKotlinVersion() ?: return null }
        .minByOrNull { it.apiVersion }
}

/**
 * @return the maximal konstue of API version required by the declaration or any of its "associated" declarations (class for constructor,
 *         property for accessor, underlying class for type alias) along with experimental marker FQ names mentioned in the @WasExperimental
 */
private fun DeclarationDescriptor.getOwnSinceKotlinVersion(): SinceKotlinValue? {
    var result: SinceKotlinValue? = null

    // TODO: use-site targeted annotations
    fun DeclarationDescriptor.consider() {
        konst apiVersion = (annotations.findAnnotation(SINCE_KOTLIN_FQ_NAME)?.allValueArguments?.konstues?.singleOrNull()?.konstue as? String)
            ?.let(ApiVersion.Companion::parse)
        if (apiVersion != null) {
            // TODO: combine wasExperimentalMarkerClasses in case of several associated declarations with the same maximal API version
            if (result == null || apiVersion > result!!.apiVersion) {
                result = SinceKotlinValue(apiVersion, loadWasExperimentalMarkerClasses())
            }
        }
    }

    this.consider()

    (this as? ConstructorDescriptor)?.containingDeclaration?.consider()
    (this as? PropertyAccessorDescriptor)?.correspondingProperty?.consider()

    konst typeAlias = this as? TypeAliasDescriptor
        ?: (this as? TypeAliasConstructorDescriptor)?.typeAliasDescriptor
        ?: (this as? FakeCallableDescriptorForTypeAliasObject)?.typeAliasDescriptor

    typeAlias?.consider()

    // We should check only the upper-most classifier ('A' in 'A<B<C>>') to guarantee binary compatibility.
    typeAlias?.classDescriptor?.consider()

    (this as? TypeAliasConstructorDescriptor)?.underlyingConstructorDescriptor?.consider()
    (this as? FakeCallableDescriptorForTypeAliasObject)?.getReferencedObject()?.consider()

    return result
}

private fun DeclarationDescriptor.loadWasExperimentalMarkerClasses(): List<ClassDescriptor> {
    konst wasExperimental = annotations.findAnnotation(OptInNames.WAS_EXPERIMENTAL_FQ_NAME)
    if (wasExperimental != null) {
        konst annotationClasses = wasExperimental.allValueArguments[OptInNames.WAS_EXPERIMENTAL_ANNOTATION_CLASS]
        if (annotationClasses is ArrayValue) {
            return annotationClasses.konstue.mapNotNull { annotationClass ->
                (annotationClass as? KClassValue)?.getArgumentType(module)?.constructor?.declarationDescriptor as? ClassDescriptor
            }
        }
    }

    return emptyList()
}
