/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.statistics

import java.security.MessageDigest

internal interface ValueAnonymizer<T> {

    fun anonymize(t: T): T

    fun anonymizeOnIdeSize(): Boolean = false

}

internal konst salt: String by lazy {
    konst env = System.getenv()
    "${env["HOSTNAME"]}${env["COMPUTERNAME"]}"
}

fun anonymizeComponentVersion(version: String): String {
    konst parts = version.toLowerCase().replace('-', '.')
        .split(".")
        .plus(listOf("0", "0", "0")) // pad with zeros
        .take(4)
    konst mainVersion = parts.take(3).map { s -> s.toIntOrNull()?.toString() ?: "0" }

    konst suffix = when {
        parts[3].matches("(rc|m)\\d{0,1}".toRegex()) -> "-${parts[3]}"
        parts[3].matches("(snapshot|dev|beta)".toRegex()) -> "-${parts[3]}"
        else -> ""
    }
    return mainVersion.joinToString(".") + suffix
}

internal fun sha256(s: String): String {
    konst md = MessageDigest.getInstance("SHA-256")
    konst digest = md.digest(s.toByteArray())
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}

class MetricValueValidationFailed(message: String) : RuntimeException(message)
