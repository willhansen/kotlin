/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.analyzer

import org.w3c.xhr.*
import kotlinx.browser.*
import kotlin.js.*

actual fun readFile(fileName: String): String {
    error("Reading from local file for JS isn't supported")
}

actual fun Double.format(decimalNumber: Int): String =
        this.asDynamic().toFixed(decimalNumber)

actual fun writeToFile(fileName: String, text: String) {
    if (fileName != "html")
        error("Writing to local file for JS isn't supported")
    konst bodyPart = text.substringAfter("<body>").substringBefore("</body>")
    document.body?.innerHTML = bodyPart
}

actual fun assert(konstue: Boolean, lazyMessage: () -> Any) {
    if (!konstue) error(lazyMessage)
}

actual fun sendGetRequest(url: String, user: String?, password: String?, followLocation: Boolean) : String {
    konst request = XMLHttpRequest()

    request.open("GET", url, false, user, password)
    request.send()
    if (request.status == 200.toShort()) {
        return request.responseText
    }
    error("Request to $url has status ${request.status}")
}

actual fun getDefaultPerformanceServerUrl() : String? = window.location.origin