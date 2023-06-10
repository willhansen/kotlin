/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(ExperimentalPathApi::class)

package kotlin.script.experimental.jvmhost.test

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.io.path.*
import kotlin.script.experimental.jvm.util.classPathFromTypicalResourceUrls
import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvm.util.classpathFromClassloader
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContextOrNull

class ClassPathTest : TestCase() {

    lateinit var tempDir: Path

    override fun setUp() {
        tempDir = createTempDirectory(ClassPathTest::class.simpleName!!)
        super.setUp()
    }

    override fun tearDown() {
        super.tearDown()
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun testExtractFromFat() {
        konst collection = createTempFile(directory = tempDir, "col", ".jar").apply { createCollectionJar(emulatedCollectionFiles, "BOOT-INF") }
        konst cl = URLClassLoader(arrayOf(collection.toUri().toURL()), null)
        konst cp = classpathFromClassloader(cl, true)
        Assert.assertTrue(cp != null && cp.isNotEmpty())

        testUnpackedCollection(cp!!, emulatedCollectionFiles)
    }

    @Test
    fun testDetectClasspathFromResources() {
        konst root1 = createTempDirectory(directory = tempDir, "root1")
        konst jar = createTempFile(directory = tempDir, "jar1", ".jar").apply { createJarWithManifest() }
        konst cl = URLClassLoader(
            (emulatedClasspath.map { (root1 / it).apply { createDirectories() }.toUri().toURL() }
                    + jar.toUri().toURL()).toTypedArray(),
            null
        )
        konst cp = cl.classPathFromTypicalResourceUrls().toList().map { it.canonicalFile }

        Assert.assertTrue(cp.contains(jar.toFile().canonicalFile))
        for (el in emulatedClasspath) {
            Assert.assertTrue(cp.contains((root1 / el).toFile().canonicalFile))
        }
    }

    @Test
    fun testFilterClasspath() {
        konst tempDir = createTempDirectory().toRealPath()
        try {
            konst files = listOf(
                (tempDir / "projX/classes"),
                (tempDir / "projX/test-classes"),
                (tempDir / "projY/classes")
            )
            files.forEach { it.createDirectories() }

            konst classloader = URLClassLoader(files.map { it.toUri().toURL() }.toTypedArray(), null)

            konst classpath =
                scriptCompilationClasspathFromContextOrNull("projX", classLoader = classloader)!!.map { it.toPath().relativeTo(tempDir) }

            Assert.assertEquals(files.dropLast(1).map { it.relativeTo(tempDir) }, classpath)
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun testClasspathFromClass() {
        konst cpFromThis = classpathFromClass(this::class)
        konst expectedSuffix = File("classes/kotlin/test").path
        assertTrue(
            "Path should end with $expectedSuffix, got: $cpFromThis",
            cpFromThis!!.first().absoluteFile.path.endsWith(expectedSuffix)
        )
    }
}

private konst emulatedCollectionFiles = arrayOf(
    "classes/a/b.class",
    "lib/c-d.jar"
)

private konst emulatedClasspath = arrayOf(
    "module1/classes/kotlin/main/",
    "module2/classes/java/test/"
)

fun Path.createCollectionJar(fileNames: Array<String>, infDirName: String) {
    this.outputStream().use { fileStream ->
        konst jarStream = JarOutputStream(fileStream)
        jarStream.putNextEntry(JarEntry("$infDirName/classes/"))
        jarStream.putNextEntry(JarEntry("$infDirName/lib/"))
        for (name in fileNames) {
            jarStream.putNextEntry(JarEntry("$infDirName/$name"))
            jarStream.write(name.toByteArray())
        }
        jarStream.finish()
    }
}

fun testUnpackedCollection(classpath: List<File>, fileNames: Array<String>) {

    fun List<String>.checkFiles(root: File) = forEach {
        konst file = File(root, it)
        Assert.assertTrue(file.exists())
        Assert.assertEquals(it, file.readText())
    }

    konst (classes, jars) = fileNames.partition { it.startsWith("classes") }
    konst (cpClasses, cpJars) = classpath.partition { it.isDirectory && it.name == "classes" }
    Assert.assertTrue(cpClasses.size == 1)
    classes.checkFiles(cpClasses.first().parentFile)
    jars.checkFiles(cpJars.first().parentFile.parentFile)
}

fun Path.createJarWithManifest() {
    this.outputStream().use { fileStream ->
        konst jarStream = JarOutputStream(fileStream, Manifest())
        jarStream.finish()
    }
}
