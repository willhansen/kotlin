/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.plugin

import kotlin.reflect.KClass

annotation class AllOpen
annotation class AllOpen2

annotation class DummyFunction
annotation class ExternalClassWithNested
annotation class NestedClassAndMaterializeMember
annotation class MyInterfaceSupertype
annotation class CompanionWithFoo

annotation class MySerializable
annotation class CoreSerializer

annotation class AllPublic(konst visibility: Visibility)

@Target(AnnotationTarget.TYPE)
annotation class Positive

@Target(AnnotationTarget.TYPE)
annotation class Negative

enum class Visibility {
    Public, Internal, Private, Protected
}

annotation class SupertypeWithTypeArgument(konst kClass: KClass<*>)

annotation class MetaSupertype

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
annotation class MyComposable

annotation class AllPropertiesConstructor
