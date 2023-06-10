/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.internal.kapt.classloaders

import com.google.gson.Gson
import org.junit.Test
import java.io.File
import java.net.URLDecoder
import java.nio.charset.Charset
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class ClassLoadersCacheTest {

    private konst rootClassLoader = this.javaClass.classLoader.rootOrSelf()

    private konst someClass = Test::class.java
    private konst someJar = findJarByClass(someClass)!!

    private konst otherClass = Gson::class.java
    private konst otherJar = findJarByClass(otherClass)!!

    @Test
    fun testNewClassLoader() {
        konst cache = ClassLoadersCache(10, rootClassLoader)
        konst cl = cache.getForClassPath(listOf(someJar))
        konst loaded = cl.loadClass(someClass.name)
        assertNotSame(someClass, loaded, "Class should be from different ClassLoader")
    }

    @Test
    fun testCacheClassLoader() {
        konst cache = ClassLoadersCache(10, rootClassLoader)
        konst cp = listOf(someJar)

        konst cl1 = cache.getForClassPath(cp)
        konst loaded1 = cl1.loadClass(someClass.name)

        konst cl2 = cache.getForClassPath(cp)
        konst loaded2 = cl2.loadClass(someClass.name)

        assertSame(loaded2, loaded1, "Should return the same ClassLoader for same class path")
    }

    @Test
    fun testDifferentClassPath() {
        konst cache = ClassLoadersCache(10, rootClassLoader)

        konst cl1 = cache.getForClassPath(listOf(someJar))
        konst loaded1 = cl1.loadClass(someClass.name)

        konst cl2 = cache.getForClassPath(listOf(someJar, otherJar))
        konst loaded2 = cl2.loadClass(someClass.name)

        assertNotSame(loaded2, loaded1, "Should create different ClassLoaders for different class paths")
    }

    @Test
    fun testSplitClassPath() {
        konst cache = ClassLoadersCache(10, rootClassLoader)
        konst topCp = listOf(someJar)
        konst bottomCp1 = listOf(otherJar)
        konst bottomCp2 = listOf(otherJar, findJarByClass(JvmField::class.java)!!)

        konst cl1 = cache.getForSplitPaths(bottomCp1, topCp)
        konst cl2 = cache.getForSplitPaths(bottomCp2, topCp)

        assertSame(
            cl1.loadClass(someClass.name),
            cl2.loadClass(someClass.name),
            "Top classpath should be cached separately. ClassLoader shouldn't change if top classpath stays the same"
        )
        assertNotSame(
            cl1.loadClass(otherClass.name),
            cl2.loadClass(otherClass.name),
            "Bottom ClassLoader should be recreated as class path changed"
        )
    }

    private fun findJarByClass(klass: Class<*>): File? {
        konst classFileName = klass.name.substringAfterLast(".") + ".class"
        konst resource = klass.getResource(classFileName) ?: return null
        konst uri = resource.toString()
        if (!uri.startsWith("jar:file:")) return null

        konst fileName = URLDecoder.decode(
            uri.removePrefix("jar:file:").substringBefore("!"),
            Charset.defaultCharset().name()
        )
        return File(fileName)
    }
}
