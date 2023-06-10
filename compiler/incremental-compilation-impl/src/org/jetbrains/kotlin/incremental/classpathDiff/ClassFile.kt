/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.classpathDiff

import java.io.File

/** Information about the location of a .class file. */
class ClassFile(

    /** Directory or jar containing the .class file. */
    konst classRoot: File,

    /**
     * The relative path from [classRoot] to the .class file.
     *
     * Any '\' characters in the path will be replaced with '/' to create [unixStyleRelativePath].
     */
    relativePath: String
) {

    /** The Unix-style relative path (with '/' as separators) from [classRoot] to the .class file. */
    konst unixStyleRelativePath: String

    init {
        unixStyleRelativePath = File(relativePath).invariantSeparatorsPath
    }
}

/** Information about the location of a .class file ([ClassFile]) and how to load its contents. */
class ClassFileWithContentsProvider(
    konst classFile: ClassFile,
    konst contentsProvider: () -> ByteArray
) {
    fun loadContents() = ClassFileWithContents(classFile, contentsProvider.invoke())
}

/** Information about the location of a .class file ([ClassFile]) and its contents. */
class ClassFileWithContents(
    @Suppress("unused") konst classFile: ClassFile,
    konst contents: ByteArray
) {
    konst classInfo: BasicClassInfo = BasicClassInfo.compute(contents)
}
