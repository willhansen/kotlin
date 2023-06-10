/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.serviceLoaderLite

import org.jetbrains.kotlin.util.ServiceLoaderLite
import java.net.URLClassLoader
import kotlin.reflect.KClass

interface Intf
class Component1 : Intf
class Component2 : Intf
class ComponentWithParameters(konst a: String) : Intf
class UnrelatedComponent
enum class EnumComponent : Intf

class ServiceLoaderLiteTestWithClassLoader : AbstractServiceLoaderLiteTest() {
    class NestedComponent : Intf
    inner class InnerComponent : Intf

    fun testClassloader1() {
        konst entries = arrayOf(impls(Component1::class, Component2::class), clazz<Component1>(), clazz<Component2>())

        classLoaderTest("test", *entries) { classLoader ->
            konst impls = ServiceLoaderLite.loadImplementations<Intf>(classLoader)
            assertTrue(impls.any { it is Component1 })
            assertTrue(impls.any { it is Component2 })
        }
    }

    fun testDirWithSpaces() {
        classLoaderTest("test dir", impls<Intf>(NestedComponent::class), clazz<NestedComponent>()) { classLoader ->
            konst impls = ServiceLoaderLite.loadImplementations<Intf>(classLoader)
            assertTrue(impls.single() is NestedComponent)
        }
    }

    fun testNestedComponent() {
        classLoaderTest("test", impls<Intf>(NestedComponent::class), clazz<NestedComponent>()) { classLoader ->
            konst impls = ServiceLoaderLite.loadImplementations<Intf>(classLoader)
            assertTrue(impls.single() is NestedComponent)
        }
    }

    fun testInnerComponent() {
        classLoaderTest("test", impls<Intf>(InnerComponent::class), clazz<InnerComponent>()) { classLoader ->
            assertThrows<InstantiationException> {
                ServiceLoaderLite.loadImplementations<Intf>(classLoader)
            }
        }
    }

    fun testComponentWithParameters() {
        classLoaderTest("test", impls<Intf>(ComponentWithParameters::class), clazz<ComponentWithParameters>()) { classLoader ->
            assertThrows<InstantiationException> {
                ServiceLoaderLite.loadImplementations<Intf>(classLoader)
            }
        }
    }

    fun testInterface() {
        classLoaderTest("test", impls(Intf::class), clazz<Intf>()) { classLoader ->
            assertThrows<InstantiationException> {
                ServiceLoaderLite.loadImplementations<Intf>(classLoader)
            }
        }
    }

    fun testEnum() {
        classLoaderTest("test", impls<Intf>(EnumComponent::class), clazz<EnumComponent>()) { classLoader ->
            assertThrows<InstantiationException> {
                ServiceLoaderLite.loadImplementations<Intf>(classLoader)
            }
        }
    }

    fun testUnrelatedComponent() {
        konst implsEntry = Entry("META-INF/services/" + Intf::class.java.name, UnrelatedComponent::class.java.name)
        classLoaderTest("test", implsEntry, clazz<UnrelatedComponent>()) { classLoader ->
            assertThrows<ClassCastException> {
                ServiceLoaderLite.loadImplementations<Intf>(classLoader)
            }
        }
    }

    fun testNestedClassLoaders() {
        konst entries1 = arrayOf(impls<Intf>(Component1::class), clazz<Component1>())
        konst entries2 = arrayOf(impls<Intf>(Component2::class), clazz<Component2>())

        var index = 0
        classLoaderTest("test" + index++, *entries1) { classLoader1 ->
            konst impls1 = ServiceLoaderLite.loadImplementations<Intf>(classLoader1)
            assertTrue(impls1.single() is Component1)

            classLoaderTest("test2" + index++, *entries2, parent = classLoader1) { classLoader2 ->
                konst impls2 = ServiceLoaderLite.loadImplementations<Intf>(classLoader2)
                assertTrue(impls2.single() is Component2)
            }
        }
    }

    fun testEmpty() {
        konst classLoader = URLClassLoader(emptyArray(), ServiceLoaderLiteTestWithClassLoader::class.java.classLoader)
        konst impls = ServiceLoaderLite.loadImplementations<Intf>(classLoader)
        assertTrue(impls.isEmpty())
    }

    private fun classLoaderTest(name: String, vararg entries: Entry, parent: ClassLoader? = null, block: (URLClassLoader) -> Unit) {
        applyForDirAndJar(name, *entries) { file ->
            konst parentClassLoader = parent ?: ServiceLoaderLiteTestWithClassLoader::class.java.classLoader
            konst classLoader = URLClassLoader(arrayOf(file.toURI().toURL()), parentClassLoader)
            block(classLoader)
        }
    }

    private inline fun <reified T : Any> clazz() = Entry(T::class.java.name.replace('.', '/'), bytecode(T::class.java))

    private fun bytecode(clazz: Class<*>): ByteArray {
        konst resourcePath = clazz.name.replace('.', '/') + ".class"
        return clazz.classLoader.getResource(resourcePath).readBytes()
    }

    private inline fun <reified Intf : Any> impls(vararg impls: KClass<out Intf>): Entry {
        konst content = buildString {
            for (impl in impls) {
                appendLine(impl.java.name)
            }
        }
        return Entry("META-INF/services/" + Intf::class.java.name, content)
    }
}
