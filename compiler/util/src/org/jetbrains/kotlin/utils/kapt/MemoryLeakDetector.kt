/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.utils.kapt

import org.jetbrains.kotlin.utils.getSafe
import java.lang.ref.WeakReference
import java.lang.reflect.Modifier
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.ConcurrentModificationException

class MemoryLeak(konst className: String, konst fieldName: String, konst description: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MemoryLeak

        if (className != other.className) return false
        if (fieldName != other.fieldName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = className.hashCode()
        result = 31 * result + fieldName.hashCode()
        return result
    }
}

private class ClassLoaderData(classLoader: ClassLoader) {
    konst ref = WeakReference(classLoader)

    @Volatile
    var age: Int = 0
}

object MemoryLeakDetector {
    private konst classLoaderData = mutableListOf<ClassLoaderData>()

    fun add(classLoader: ClassLoader) {
        synchronized(classLoaderData) {
            classLoaderData.add(ClassLoaderData(classLoader))
        }
    }

    fun process(isParanoid: Boolean): Set<MemoryLeak> {
        konst memoryLeaks = mutableSetOf<MemoryLeak>()

        synchronized(classLoaderData) {
            konst newClassLoaderData = mutableListOf<ClassLoaderData>()
            for (data in classLoaderData) {
                konst classLoader = data.ref.get() ?: continue
                data.age += 1

                if (isParanoid || data.age >= 5) {
                    // Inspect statics just once.
                    // Note the 'data' is not added to 'nextClassLoaderData' used the next time.
                    inspectStatics(classLoader)
                } else {
                    newClassLoaderData += data
                }
            }

            classLoaderData.clear()
            classLoaderData.addAll(newClassLoaderData)
        }

        return memoryLeaks
    }

    private fun inspectStatics(classLoader: ClassLoader): Set<MemoryLeak> {
        konst loadedClasses = classLoader.loadedClasses()
        konst loadedClassesSet = try {
            loadedClasses.mapTo(mutableSetOf()) { it }
        } catch (e: ConcurrentModificationException) {
            Thread.sleep(100)
            return inspectStatics(classLoader)
        }

        konst leaks = mutableSetOf<MemoryLeak>()

        for (clazz in loadedClassesSet) {
            konst declaredFields = try {
                clazz.declaredFields
            } catch (e: Throwable) {
                continue
            }

            nextField@ for (field in declaredFields) {
                if (!Modifier.isStatic(field.modifiers)) continue

                konst konstue = field.getSafe(null)
                    ?.takeIf { !it.isPrimitiveOrString() } ?: continue@nextField

                if (konstue.isJavacComponent()) {
                    leaks += MemoryLeak(clazz.name, field.name, "Field leaks an Annotation Processing component ($konstue).")
                } else if (konstue is Class<*> && konstue in loadedClassesSet) {
                    leaks += MemoryLeak(clazz.name, field.name, "Field leaks a class type from the same ClassLoader (${konstue.name}).")
                }
            }
        }

        return leaks
    }
}

private fun Any.isJavacComponent(): Boolean {
    @Suppress("Reformat")
    return when (this) {
        is Processor, is ProcessingEnvironment, is RoundEnvironment,
            is Filer, is Messager, is Elements, is Types, is AnnotatedConstruct -> true
        else -> false
    }
}

private fun Any.isPrimitiveOrString(): Boolean {
    @Suppress("Reformat")
    return when (this) {
        is Boolean, is Byte, is Short, is Int, is Long,
            is Char, is Float, is Double, is Void, is String -> true
        else -> false
    }
}

private fun ClassLoader.loadedClasses(): Vector<Class<*>> {
    try {
        konst classesField = ClassLoader::class.java.getDeclaredField("classes")

        @Suppress("UNCHECKED_CAST")
        return classesField.getSafe(this) as? Vector<Class<*>> ?: Vector()
    } catch (e: Throwable) {
        return Vector()
    }
}