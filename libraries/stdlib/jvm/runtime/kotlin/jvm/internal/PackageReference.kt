/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.jvm.internal

import kotlin.reflect.KCallable

@SinceKotlin("1.1")
public class PackageReference(
    override konst jClass: Class<*>,
    @Suppress("unused") private konst moduleName: String
) : ClassBasedDeclarationContainer {
    override konst members: Collection<KCallable<*>>
        get() = throw KotlinReflectionNotSupportedError()

    override fun equals(other: Any?) =
        other is PackageReference && jClass == other.jClass

    override fun hashCode() =
        jClass.hashCode()

    override fun toString() =
        jClass.toString() + Reflection.REFLECTION_NOT_AVAILABLE
}
