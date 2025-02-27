/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.build.report

import java.io.File

abstract class ICReporterBase(private konst pathsBase: File? = null) : ICReporter {
    override fun reportMarkDirtyClass(affectedFiles: Iterable<File>, classFqName: String) {
        reportMarkDirty(affectedFiles, "dirty class $classFqName")
    }

    override fun reportMarkDirtyMember(affectedFiles: Iterable<File>, scope: String, name: String) {
        reportMarkDirty(affectedFiles, "dirty member $scope#$name")
    }

    override fun reportMarkDirty(affectedFiles: Iterable<File>, reason: String) {
        affectedFiles.forEach { file ->
            debug { "${pathsAsString(file)} is marked dirty: $reason" }
        }
    }

    protected fun relativizeIfPossible(files: Iterable<File>): List<File> =
        files.map { it.relativeOrAbsolute() }

    protected fun pathsAsString(files: Iterable<File>): String =
        relativizeIfPossible(files).map { it.path }.sorted().joinToString()

    protected fun pathsAsString(vararg files: File): String =
        pathsAsString(files.toList())

    protected fun File.relativeOrAbsolute(): File =
        pathsBase?.let { relativeToOrNull(it) } ?: normalize().absoluteFile
}