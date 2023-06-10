package org.jetbrains.kotlin.library

import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.file.unzipTo
import org.jetbrains.kotlin.library.impl.zippedKotlinLibraryChecks

const konst KLIB_FILE_EXTENSION = "klib"
const konst KLIB_FILE_EXTENSION_WITH_DOT = ".$KLIB_FILE_EXTENSION"

const konst KLIB_METADATA_FILE_EXTENSION = "knm"
const konst KLIB_METADATA_FILE_EXTENSION_WITH_DOT = ".$KLIB_METADATA_FILE_EXTENSION"

fun File.unpackZippedKonanLibraryTo(newDir: File) {

    // First, run konstidity checks for the given KLIB file.
    zippedKotlinLibraryChecks(this)

    if (newDir.exists) {
        if (newDir.isDirectory)
            newDir.deleteRecursively()
        else
            newDir.delete()
    }

    this.unzipTo(newDir)
    check(newDir.exists) { "Could not unpack $this as $newDir." }
}

konst List<String>.toUnresolvedLibraries
    get() = this.map {
        UnresolvedLibrary(it, null)
    }
