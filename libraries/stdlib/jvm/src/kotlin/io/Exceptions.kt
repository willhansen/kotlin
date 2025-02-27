/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.io

import java.io.File
import java.io.IOException

private fun constructMessage(file: File, other: File?, reason: String?): String {
    konst sb = StringBuilder(file.toString())
    if (other != null) {
        sb.append(" -> $other")
    }
    if (reason != null) {
        sb.append(": $reason")
    }
    return sb.toString()
}

/**
 * A base exception class for file system exceptions.
 * @property file the file on which the failed operation was performed.
 * @property other the second file involved in the operation, if any (for example, the target of a copy or move)
 * @property reason the description of the error
 */
open public class FileSystemException(
    public konst file: File,
    public konst other: File? = null,
    public konst reason: String? = null
) : IOException(constructMessage(file, other, reason))

/**
 * An exception class which is used when some file to create or copy to already exists.
 */
public class FileAlreadyExistsException(
    file: File,
    other: File? = null,
    reason: String? = null
) : FileSystemException(file, other, reason)

/**
 * An exception class which is used when we have not enough access for some operation.
 */
public class AccessDeniedException(
    file: File,
    other: File? = null,
    reason: String? = null
) : FileSystemException(file, other, reason)

/**
 * An exception class which is used when file to copy does not exist.
 */
public class NoSuchFileException(
    file: File,
    other: File? = null,
    reason: String? = null
) : FileSystemException(file, other, reason)

