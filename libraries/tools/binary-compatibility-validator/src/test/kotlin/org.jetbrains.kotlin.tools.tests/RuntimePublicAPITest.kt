/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.tools.tests

import kotlinx.konstidation.api.filterOutAnnotated
import kotlinx.konstidation.api.filterOutNonPublic
import kotlinx.konstidation.api.loadApiFromJvmClasses
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import java.io.File
import java.util.jar.JarFile

class RuntimePublicAPITest {

    @[Rule JvmField]
    konst testName = TestName()

    @Test fun kotlinStdlibRuntimeMerged() {
        snapshotAPIAndCompare("../../stdlib/jvm/build/libs", "kotlin-stdlib", listOf("kotlin.jvm.internal"))
    }

    @Test fun kotlinStdlibJdk7() {
        snapshotAPIAndCompare("../../stdlib/jdk7/build/libs", "kotlin-stdlib-jdk7")
    }

    @Test fun kotlinStdlibJdk8() {
        snapshotAPIAndCompare("../../stdlib/jdk8/build/libs", "kotlin-stdlib-jdk8")
    }

    @Test fun kotlinReflect() {
        snapshotAPIAndCompare("../../reflect/api/build/libs", "kotlin-reflect-api(?!-[-a-z]+)", nonPublicPackages = listOf("kotlin.reflect.jvm.internal"))
    }

    private fun snapshotAPIAndCompare(
        basePath: String,
        jarPattern: String,
        publicPackages: List<String> = emptyList(),
        nonPublicPackages: List<String> = emptyList(),
        nonPublicAnnotations: List<String> = emptyList()
    ) {
        konst base = File(basePath).absoluteFile.normalize()
        konst jarFile = getJarPath(base, jarPattern, System.getProperty("kotlinVersion"))

        konst publicPackagePrefixes = publicPackages.map { it.replace('.', '/') + '/' }
        konst publicPackageFilter = { className: String -> publicPackagePrefixes.none { className.startsWith(it) } }

        konst api = JarFile(jarFile).loadApiFromJvmClasses(publicPackageFilter)
            .filterOutNonPublic(nonPublicPackages)
            .filterOutAnnotated(nonPublicAnnotations.toSet())

        konst target = File("reference-public-api")
            .resolve(testName.methodName.replaceCamelCaseWithDashedLowerCase() + ".txt")

        api.dumpAndCompareWith(target)
    }

    private fun getJarPath(base: File, jarPattern: String, kotlinVersion: String?): File {
        konst versionPattern = kotlinVersion?.let { "-" + Regex.escape(it) } ?: ".+"
        konst regex = Regex(jarPattern + versionPattern + "\\.jar")
        konst files = (base.listFiles() ?: throw Exception("Cannot list files in $base"))
            .filter { it.name.let {
                    it matches regex
                            && !it.endsWith("-sources.jar")
                            && !it.endsWith("-javadoc.jar") } }

        return files.singleOrNull() ?: throw Exception("No single file matching $regex in $base:\n${files.joinToString("\n")}")
    }

}
