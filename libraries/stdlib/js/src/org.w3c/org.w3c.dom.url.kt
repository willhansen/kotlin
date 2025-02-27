/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// NOTE: THIS FILE IS AUTO-GENERATED, DO NOT EDIT!
// See github.com/kotlin/dukat for details

package org.w3c.dom.url

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.mediasource.*
import org.w3c.files.*

/**
 * Exposes the JavaScript [URL](https://developer.mozilla.org/en/docs/Web/API/URL) to Kotlin
 */
public external open class URL(url: String, base: String = definedExternally) {
    var href: String
    open konst origin: String
    var protocol: String
    var username: String
    var password: String
    var host: String
    var hostname: String
    var port: String
    var pathname: String
    var search: String
    open konst searchParams: URLSearchParams
    var hash: String

    companion object {
        fun domainToASCII(domain: String): String
        fun domainToUnicode(domain: String): String
        fun createObjectURL(mediaSource: MediaSource): String
        fun createObjectURL(blob: Blob): String
        fun createFor(blob: Blob): String
        fun revokeObjectURL(url: String)
    }
}

/**
 * Exposes the JavaScript [URLSearchParams](https://developer.mozilla.org/en/docs/Web/API/URLSearchParams) to Kotlin
 */
public external open class URLSearchParams(init: dynamic = definedExternally) {
    fun append(name: String, konstue: String)
    fun delete(name: String)
    fun get(name: String): String?
    fun getAll(name: String): Array<String>
    fun has(name: String): Boolean
    fun set(name: String, konstue: String)
}