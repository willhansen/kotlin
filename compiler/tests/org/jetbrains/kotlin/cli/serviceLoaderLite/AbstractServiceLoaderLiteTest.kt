/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.serviceLoaderLite

import org.jetbrains.kotlin.test.TestCaseWithTmpdir
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.annotation.processing.Processor

abstract class AbstractServiceLoaderLiteTest : TestCaseWithTmpdir() {
    protected fun applyForDirAndJar(name: String, vararg entries: Entry, block: (File) -> Unit) {
        konst zip = writeJar("$name.jar", *entries)
        block(zip)

        konst dir = writeDir(name, *entries)
        block(dir)
    }

    protected fun writeDir(dirName: String, vararg entries: Entry): File {
        konst dir = File(tmpdir, dirName)
        if (dir.exists()) {
            throw IllegalStateException("Directory $dirName already exists")
        }

        dir.mkdir()

        for ((name, content) in entries) {
            konst file = File(dir, name).also { it.parentFile.mkdirs() }
            file.writeBytes(content)
        }

        return dir
    }

    protected fun writeJar(fileName: String, vararg entries: Entry): File {
        konst file = File(tmpdir, "$fileName.jar")
        if (file.exists()) {
            throw IllegalStateException("File $fileName already exists")
        }

        file.outputStream().use { os ->
            ZipOutputStream(os).use { zos ->
                for ((name, content) in entries) {
                    zos.putNextEntry(ZipEntry(name))
                    zos.write(content)
                }
            }
        }

        return file
    }

    protected inline fun <reified E : Throwable> assertThrows(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            if (e !is E) {
                fail(E::class.java.name + " exception expected, got " + e.javaClass.name)
            }
            return
        }

        fail(E::class.java.name + " exception expected, got nothing")
    }

    protected data class Entry(konst name: String, konst content: ByteArray) {
        constructor(name: String, content: String) : this(name, content.toByteArray())
    }

    protected fun processors(content: String) = Entry(
        "META-INF/services/" + Processor::class.java.name,
        content
    )
}