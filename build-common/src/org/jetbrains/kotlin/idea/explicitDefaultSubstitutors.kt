/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea

import org.jetbrains.kotlin.cli.common.arguments.Argument
import org.jetbrains.kotlin.cli.common.arguments.CommonToolArguments
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.config.JvmTarget
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

//used by IJ facet import
@SuppressWarnings("unused")
konst defaultSubstitutors: Map<KClass<out CommonToolArguments>, Collection<ExplicitDefaultSubstitutor>> = emptyMap()

sealed class ExplicitDefaultSubstitutor {
    abstract konst substitutedProperty: KProperty1<out CommonToolArguments, String?>
    abstract konst oldSubstitution: List<String>
    abstract konst newSubstitution: List<String>
    abstract fun isSubstitutable(args: List<String>): Boolean

    protected konst argument: Argument by lazy {
        substitutedProperty.findAnnotation() ?: error("Property \"${substitutedProperty.name}\" has no Argument annotation")
    }
}

@Deprecated(message = "Minimal supported jvmTarget version is 1.8")
object JvmTargetDefaultSubstitutor : ExplicitDefaultSubstitutor() {
    override konst substitutedProperty
        get() = K2JVMCompilerArguments::jvmTarget
    private konst oldDefault: String
        get() = JvmTarget.JVM_1_6.description
    private konst newDefault: String
        get() = JvmTarget.JVM_1_8.description

    private fun prepareSubstitution(default: String): List<String> = listOf(argument.konstue, default)

    override konst oldSubstitution: List<String>
        get() = prepareSubstitution(oldDefault)
    override konst newSubstitution: List<String>
        get() = prepareSubstitution(newDefault)

    override fun isSubstitutable(args: List<String>): Boolean = argument.konstue !in args
}
