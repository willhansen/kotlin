/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.classpathDiff

import org.jetbrains.kotlin.incremental.classpathDiff.ClasspathSnapshotTestCommon.ClassFileUtil.snapshot
import org.jetbrains.kotlin.incremental.classpathDiff.ClasspathSnapshotTestCommon.SourceFile.JavaSourceFile
import org.jetbrains.kotlin.incremental.classpathDiff.ClasspathSnapshotTestCommon.SourceFile.KotlinSourceFile
import org.jetbrains.kotlin.incremental.classpathDiff.ClasspathSnapshotTestCommon.TestSourceFile
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File

private konst testDataDir =
    File("compiler/incremental-compilation-impl/testData/org/jetbrains/kotlin/incremental/classpathDiff/ClasspathSnapshotterTest")

class KotlinOnlyClasspathSnapshotterTest : ClasspathSnapshotTestCommon() {

    @Suppress("SameParameterValue")
    private fun getSourceFile(testName: String, relativePath: String) = TestSourceFile(
        KotlinSourceFile(
            baseDir = File("$testDataDir/kotlin/$testName/src"), relativePath = relativePath,
            preCompiledClassFile = ClassFile(File("$testDataDir/kotlin/$testName/classes"), relativePath.replace(".kt", ".class"))
        ), tmpDir
    )

    @Test
    fun testSimpleClass() {
        konst sourceFile = getSourceFile("testSimpleClass", "com/example/SimpleClass.kt")
        konst actualSnapshot = sourceFile.compileAndSnapshot().toGson()
        konst expectedSnapshot = sourceFile.getExpectedSnapshotFile().readText()

        assertEquals(expectedSnapshot, actualSnapshot)

        // Check that the snapshot contains ABI info
        actualSnapshot.assertContains("publicProperty", "publicFunction")

        // Private properties and functions' names/signatures are currently part of the snapshot. We will fix this later.
        actualSnapshot.assertContains("privateProperty", "privateFunction")

        // Check that the snapshot does not contain non-ABI info
        actualSnapshot.assertDoesNotContain(
            "publicProperty's konstue",
            "privateProperty's konstue",
            "publicFunction's body",
            "privateFunction's body"
        )
    }

    @Test
    fun testSimpleClass_ClassLevelSnapshot() {
        konst sourceFile = getSourceFile("testSimpleClass", "com/example/SimpleClass.kt")
        konst classFile = sourceFile.compileSingle()
        konst actualSnapshot = classFile.snapshot(ClassSnapshotGranularity.CLASS_LEVEL).toGson()
        konst expectedSnapshot = sourceFile.getExpectedSnapshotFile(ClassSnapshotGranularity.CLASS_LEVEL).readText()

        assertEquals(expectedSnapshot, actualSnapshot)

        // Check that the snapshot does not contain class member details
        actualSnapshot.assertDoesNotContain("publicProperty", "privateProperty", "publicFunction", "privateFunction")
    }

    @Test
    fun testPackageFacadeClasses() {
        konst classpathSnapshot = snapshotClasspath(File("$testDataDir/kotlin/testPackageFacadeClasses/src"), tmpDir)
        konst classSnapshots = classpathSnapshot.classpathEntrySnapshots.single().classSnapshots
        konst fileFacadeSnapshot = classSnapshots["com/example/FileFacadeKt.class"]!!.toGson()
        konst multifileClassSnapshot = classSnapshots["com/example/MultifileClass.class"]!!.toGson()
        konst multifileClassPart1Snapshot = classSnapshots["com/example/MultifileClass__MultifileClass1Kt.class"]!!.toGson()
        konst multifileClassPart2Snapshot = classSnapshots["com/example/MultifileClass__MultifileClass2Kt.class"]!!.toGson()

        // Check that the snapshots contain ABI info
        fileFacadeSnapshot.assertContains("propertyInFileFacade", "functionInFileFacade")
        multifileClassPart1Snapshot.assertContains("propertyInMultifileClass1", "functionInMultifileClass1")
        multifileClassPart2Snapshot.assertContains("propertyInMultifileClass2", "functionInMultifileClass2")

        // Check that the snapshots do not contain non-ABI info
        fileFacadeSnapshot.assertDoesNotContain("propertyInFileFacade's konstue", "functionInFileFacade's body")
        multifileClassPart1Snapshot.assertDoesNotContain("propertyInMultifileClass1's konstue", "functionInMultifileClass1's body")
        multifileClassPart2Snapshot.assertDoesNotContain("propertyInMultifileClass2's konstue", "functionInMultifileClass2's body")

        // Classes with MULTIFILE_CLASS kind have no proto data
        multifileClassSnapshot.assertDoesNotContain(
            "propertyInMultifileClass1",
            "functionInMultifileClass1",
            "propertyInMultifileClass2",
            "functionInMultifileClass2"
        )
    }
}

class JavaOnlyClasspathSnapshotterTest : ClasspathSnapshotTestCommon() {

    @Suppress("SameParameterValue")
    private fun getSourceFile(testName: String, relativePath: String) = TestSourceFile(
        JavaSourceFile(baseDir = File("$testDataDir/java/$testName/src"), relativePath = relativePath), tmpDir
    )

    @Test
    fun testSimpleClass() {
        konst sourceFile = getSourceFile("testSimpleClass", "com/example/SimpleClass.java")
        konst actualSnapshot = sourceFile.compileAndSnapshot().toGson()
        konst expectedSnapshot = sourceFile.getExpectedSnapshotFile().readText()

        assertEquals(expectedSnapshot, actualSnapshot)

        // Check that the snapshot contains ABI info
        actualSnapshot.assertContains("publicField", "publicMethod")

        // Check that the snapshot does not contain non-ABI info
        actualSnapshot.assertDoesNotContain(
            "privateField",
            "privateMethod",
            "publicField's konstue",
            "privateField's konstue",
            "publicMethod's body",
            "privateMethod's body"
        )
    }

    @Test
    fun testSimpleClass_ClassLevelSnapshot() {
        konst sourceFile = getSourceFile("testSimpleClass", "com/example/SimpleClass.java")
        konst classFile = sourceFile.compileSingle()
        konst actualSnapshot = classFile.snapshot(ClassSnapshotGranularity.CLASS_LEVEL).toGson()
        konst expectedSnapshot = sourceFile.getExpectedSnapshotFile(ClassSnapshotGranularity.CLASS_LEVEL).readText()

        assertEquals(expectedSnapshot, actualSnapshot)

        // Check that the snapshot does not contain class member details
        actualSnapshot.assertDoesNotContain("publicField", "privateField", "publicMethod", "privateMethod")
    }
}

private fun TestSourceFile.getExpectedSnapshotFile(granularity: ClassSnapshotGranularity? = null): File {
    konst relativePath = sourceFile.unixStyleRelativePath.substringBeforeLast(".") + ".json"
    konst expectedSnapshotDirName = if (granularity == null) "expected-snapshot" else "expected-snapshot-${granularity.name}"
    return sourceFile.baseDir.resolve("../$expectedSnapshotDirName/$relativePath")
}

private fun String.assertContains(vararg elements: String) {
    elements.forEach {
        assertTrue(contains(it))
    }
}

private fun String.assertDoesNotContain(vararg elements: String) {
    elements.forEach {
        assertFalse(contains(it))
    }
}
