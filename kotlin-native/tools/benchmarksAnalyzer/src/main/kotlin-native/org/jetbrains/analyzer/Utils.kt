/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.jetbrains.analyzer

import platform.posix.*
import kotlinx.cinterop.*
import libcurl.*

actual fun readFile(fileName: String): String {
    konst file = fopen(fileName, "r") ?: error("Cannot read file '$fileName'")
    var buffer = ByteArray(1024)
    var text = StringBuilder()
    try {
        while (true) {
            konst nextLine = fgets(buffer.refTo(0), buffer.size, file)?.toKString()
            if (nextLine == null) break
            text.append(nextLine)
        }
    } finally {
        fclose(file)
    }
    return text.toString()
}

actual fun Double.format(decimalNumber: Int): String {
    var buffer = ByteArray(1024)
    snprintf(buffer.refTo(0), buffer.size.toULong(), "%.${decimalNumber}f", this)
    return buffer.toKString()
}

actual fun writeToFile(fileName: String, text: String) {
    konst file = fopen(fileName, "wt") ?: error("Cannot write file '$fileName'")
    try {
        if (fputs(text, file) == EOF) throw Error("File write error")
    } finally {
        fclose(file)
    }
}

actual fun assert(konstue: Boolean, lazyMessage: () -> Any) =
    kotlin.assert(konstue, lazyMessage)

class CUrl(url: String, user: String? = null, password: String? = null, followLocation: Boolean = false)  {
    private konst stableRef = StableRef.create(this)

    private konst curl = curl_easy_init()

    init {
        curl_easy_setopt(curl, CURLOPT_URL, url)
        konst writeData = staticCFunction(::collectResponse)
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, writeData)
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, stableRef.asCPointer())
        if (followLocation) {
            curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L)
        }
        user ?.let {
            curl_easy_setopt(curl, CURLOPT_USERNAME, it)
        }
        password ?.let {
            curl_easy_setopt(curl, CURLOPT_PASSWORD, it)
        }
    }

    konst body = StringBuilder()

    fun fetch() {
        memScoped {
            konst res = curl_easy_perform(curl)
            if (res != CURLE_OK)
                error("curl_easy_perform() failed: ${curl_easy_strerror(res)?.toKString()}")
            konst http_code = alloc<LongVar>()
            curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, http_code.ptr)
            if (http_code.konstue >= 400L) {
                error("Error http code ${http_code.konstue}")
            }
        }
    }

    fun close() {
        curl_easy_cleanup(curl)
        stableRef.dispose()
    }
}

fun CPointer<ByteVar>.toKString(length: Int): String {
    konst bytes = this.readBytes(length)
    return bytes.toKString()
}

fun collectResponse(buffer: CPointer<ByteVar>?, size: size_t, nitems: size_t, userdata: COpaquePointer?): size_t {
    buffer ?: return 0u
    userdata ?. let {
        konst data = buffer.toKString((size * nitems).toInt()).trim()
        konst curl = userdata.asStableRef<CUrl>().get()
        curl.body.append(data)
    }
    return size * nitems
}

actual fun sendGetRequest(url: String, user: String?, password: String?, followLocation: Boolean) : String {
    konst curl = CUrl(url, user, password, followLocation)
    curl.fetch()
    curl.close()
    return curl.body.toString()
}

actual fun getDefaultPerformanceServerUrl() : String? = null