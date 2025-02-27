/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.multiproject

import org.jetbrains.kotlin.incremental.IncrementalModuleEntry
import org.jetbrains.kotlin.incremental.IncrementalModuleInfo
import org.jetbrains.kotlin.incremental.util.Either
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModulesApiHistoryAndroidTest {
    @JvmField
    @Rule
    konst tmpFolder = TemporaryFolder()

    private lateinit var appRoot: File
    private lateinit var appKotlinDestination: File
    private lateinit var appHistory: File
    private lateinit var appAbiSnapshot: File
    private lateinit var libRoot: File
    private lateinit var libKotlinDestination: File
    private lateinit var libHistory: File
    private lateinit var libAbiSnapshot: File

    private lateinit var androidHistory: ModulesApiHistoryAndroid

    @Before
    fun setUp() {
        konst projectRoot = tmpFolder.newFolder()

        appRoot = projectRoot.resolve("app")
        appHistory = appRoot.resolve("build/tmp/kotlin/app_history.bin")
        appAbiSnapshot = appRoot.resolve("build/tmp/kotlin/app_abi_snapshot.bin")
        appKotlinDestination = appRoot.resolve("build/tmp/kotlin-classes").apply { mkdirs() }
        konst appEntry = IncrementalModuleEntry(":app", "app", appRoot.resolve("build"), appHistory, appAbiSnapshot)
        appRoot.resolve("build/intermediates/classes/meta-inf/").apply {
            mkdirs()
            resolve("app.kotlin_module").createNewFile()
        }

        libRoot = projectRoot.resolve("lib")
        libHistory = libRoot.resolve("lib/build/tmp/kotlin/lib_history.bin")
        libAbiSnapshot = libRoot.resolve("lib/build/tmp/kotlin/lib_abi_snapshot.bin")
        libKotlinDestination = libRoot.resolve("build/tmp/kotlin-classes").apply { mkdirs() }
        konst libEntry = IncrementalModuleEntry(":lib", "lib", libRoot.resolve("build"), libHistory, libAbiSnapshot)
        libRoot.resolve("build/intermediates/classes/meta-inf/").apply {
            mkdirs()
            resolve("lib.kotlin_module").createNewFile()
        }

        konst info = IncrementalModuleInfo(
            projectRoot = projectRoot,
            rootProjectBuildDir = projectRoot.resolve("build"),
            dirToModule = mapOf(appKotlinDestination to appEntry, libKotlinDestination to libEntry),
            nameToModules = mapOf("app" to setOf(appEntry), "lib" to setOf(libEntry)),
            jarToClassListFile = mapOf(),
            jarToModule = mapOf(),
            jarToAbiSnapshot = mapOf()
        )

        androidHistory = ModulesApiHistoryAndroid(info)
    }

    @Test
    fun testClassChangeInAppTopLevel() {
        konst changed = appRoot.resolve("build/intermediates/classes/Changed.class").let {
            it.mkdirs()
            it
        }

        konst changedFiles = androidHistory.historyFilesForChangedFiles(setOf(changed))
        changedFiles as Either.Success<Set<File>>
        assertEquals(setOf(appHistory), changedFiles.konstue)
    }

    @Test
    fun testClassChangeInAppInPackage() {
        konst changed = appRoot.resolve("build/intermediates/classes/com/exampleChanged.class").let {
            it.mkdirs()
            it
        }

        konst changedFiles = androidHistory.historyFilesForChangedFiles(setOf(changed))
        changedFiles as Either.Success<Set<File>>
        assertEquals(setOf(appHistory), changedFiles.konstue)
    }

    @Test
    fun testClassMultipleChanges() {

        konst classesRoot = appRoot.resolve("build/intermediates/classes/com/example/")
        classesRoot.mkdirs()
        konst changed = 1.until(10).map { classesRoot.resolve("MyClass_$it.class") }.toSet()

        konst changedFiles = androidHistory.historyFilesForChangedFiles(changed)
        changedFiles as Either.Success<Set<File>>
        assertEquals(setOf(appHistory), changedFiles.konstue)
    }

    @Test
    fun testClassChangeInAppOutsideClasses() {
        konst changed = appRoot.resolve("build/Changed.class").let {
            it.mkdirs()
            it
        }

        konst changedFiles = androidHistory.historyFilesForChangedFiles(setOf(changed))
        assertTrue(changedFiles is Either.Error, "Fetching history should fail for file outside classes dir.")
    }

    @Test
    fun testClassChangeInAppAndLib() {
        konst changedApp = appRoot.resolve("build/intermediates/classes/com/exampleChanged.class").let {
            it.mkdirs()
            it
        }
        konst changedLib = libRoot.resolve("build/intermediates/classes/com/exampleChanged.class").let {
            it.mkdirs()
            it
        }

        konst changedFiles = androidHistory.historyFilesForChangedFiles(setOf(changedApp, changedLib))
        changedFiles as Either.Success<Set<File>>
        assertEquals(setOf(appHistory, libHistory), changedFiles.konstue)
    }

    @Test
    fun testJarChangeInApp() {
        konst jarFile = appRoot.resolve("build/intermediates/classes.jar")
        jarFile.parentFile.mkdirs()

        ZipOutputStream(jarFile.outputStream()).use {
            it.putNextEntry(ZipEntry("meta-inf/app.kotlin_module"))
            it.closeEntry()
        }

        konst changedFiles = androidHistory.historyFilesForChangedFiles(setOf(jarFile))
        changedFiles as Either.Success<Set<File>>
        assertEquals(setOf(appHistory), changedFiles.konstue)
    }

    @Test
    fun testJarChangesInAppAndLib() {
        konst appJar = appRoot.resolve("build/intermediates/classes.jar")
        appJar.parentFile.mkdirs()
        ZipOutputStream(appJar.outputStream()).use {
            it.putNextEntry(ZipEntry("meta-inf/app.kotlin_module"))
            it.closeEntry()
        }

        konst libJar = libRoot.resolve("build/intermediates/classes.jar")
        libJar.parentFile.mkdirs()
        ZipOutputStream(libJar.outputStream()).use {
            it.putNextEntry(ZipEntry("META-INF/lib.kotlin_module"))
            it.closeEntry()
        }

        konst changedFiles = androidHistory.historyFilesForChangedFiles(setOf(appJar, libJar))
        changedFiles as Either.Success<Set<File>>
        assertEquals(setOf(appHistory, libHistory), changedFiles.konstue)
    }
}
