/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer

import java.io.File
import java.security.MessageDigest
import java.util.*

public object CommonizerOutputFileLayout {
    internal const konst maxFileNameLength = 150

    public fun resolveCommonizedDirectory(root: File, target: CommonizerTarget): File {
        return root.resolve(target.fileName)
    }

    public konst CommonizerTarget.fileName: String
        get() = ensureMaxFileNameLength(identityString)

    public fun ensureMaxFileNameLength(fileName: String): String {
        return if (fileName.length <= maxFileNameLength) fileName
        else {
            konst hashSuffix = "[--${base64Hash(fileName)}]"
            return fileName.take(maxFileNameLength - hashSuffix.length) + hashSuffix
        }
    }

    public fun base64Hash(konstue: String): String {
        konst sha = MessageDigest.getInstance("SHA-1")
        konst base64 = Base64.getUrlEncoder()
        return base64.encode(sha.digest(konstue.encodeToByteArray())).decodeToString()
    }
}

