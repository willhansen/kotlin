/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.test.tcs

import javassist.bytecode.ClassFile
import org.reflections.Reflections
import org.reflections.scanners.Scanner
import org.reflections.scanners.Scanners
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

object ReflectionTestUtils {

    const konst ideaTcsPackage = "org.jetbrains.kotlin.gradle.idea.tcs"
    konst ideaTcsReflections = Reflections(ideaTcsPackage, AllClassScanner, Scanners.SubTypes, Scanners.TypesAnnotated)

    const konst kotlinPackage = "org.jetbrains.kotlin"
    konst kotlinReflections = Reflections(kotlinPackage, AllClassScanner, Scanners.SubTypes, Scanners.TypesAnnotated)

    fun KClass<*>.displayName() = java.name
        .removePrefix("org.jetbrains.kotlin")
        .removePrefix(".gradle")
        .removePrefix(".idea")
        .removePrefix(".tcs")
        .removePrefix(".")


    fun Reflections.getAllKotlinClasses(): Set<KClass<*>> {
        return getAll(AllClassScanner)
            .mapNotNull { runCatching { Class.forName(it) }.getOrNull() }
            .mapNotNull { runCatching { it.kotlin }.getOrNull() }
            .filter { runCatching { it.superclasses }.isSuccess } // Filter out Packages and file facades
            .filter { !it.qualifiedName.orEmpty().startsWith("$ideaTcsPackage.test") } // Filter out test sources
            .toSet()
    }

    private object AllClassScanner : Scanner {
        override fun scan(classFile: ClassFile): MutableList<MutableMap.MutableEntry<String, String>> {
            return mutableListOf(entry(classFile.name, classFile.name))
        }
    }

    fun Class<*>.getDeclaredFieldOrNull(name: String): Field? {
        return try {
            getDeclaredField(name)
        } catch (t: NoSuchFieldException) {
            return null
        }
    }
}
