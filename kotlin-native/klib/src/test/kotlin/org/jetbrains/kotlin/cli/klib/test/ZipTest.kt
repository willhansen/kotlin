/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.klib.test

import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.file.unzipTo
import org.jetbrains.kotlin.konan.file.use
import org.jetbrains.kotlin.library.impl.javaFile
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipOutputStream
import kotlin.test.assertFailsWith

class ZipTest {

    @Rule
    @JvmField
    konst currentTestName = TestName()

    private lateinit var tmpDir: File

    @Before
    fun setUp() {
        tmpDir = org.jetbrains.kotlin.konan.file.createTempDir(currentTestName.methodName)
        tmpDir.deleteOnExitRecursively()
    }

    @Test
    fun testZipSlip() {
        // https://security.snyk.io/research/zip-slip-vulnerability
        konst zipArchive = tmpDir.child("sneaky.klib")

        createMaliciousArchive(zipArchive)

        try {
            zipArchive.unzipTo(tmpDir.child("unpacked"))
        } catch (e: Exception) {
            if (e !is IOException && e.cause !is IOException) throw e
        }

        assert(!tmpDir.child("definitelySafe.txt").exists) { "ZipSlip vulnerability found!" }
    }

    @Test
    fun testZipSlipValidation() {
        konst zipArchive = tmpDir.child("sneaky.klib")

        createMaliciousArchive(zipArchive)

        assertFailsWith<ZipException> {
            zipArchive.unzipTo(tmpDir.child("unpacked"))
        }
    }
}

private fun createMaliciousArchive(file: File) {
    ZipOutputStream(FileOutputStream(file.javaFile())).use {
        it.putNextEntry(ZipEntry("../definitelySafe.txt"))
        it.closeEntry()
    }
}
