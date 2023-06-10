/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import kotlinx.cinterop.*
import platform.Foundation.*
import kotlin.test.*

@Test
fun fooTest() {
    println("foo")
}

@Test
fun request() {
    konst response = memScoped {
        konst request = NSURLRequest(NSURL(string = "https://cache-redirector.jetbrains.com/"))
        konst responseRef = alloc<ObjCObjectVar<NSURLResponse?>>()
        konst errorRef = alloc<ObjCObjectVar<NSError?>>()
        NSURLConnection.sendSynchronousRequest(request, responseRef.ptr, errorRef.ptr) ?:
        throw Error(errorRef.konstue?.toString() ?: "")
        responseRef.konstue!! as NSHTTPURLResponse
    }
    kotlin.test.assertEquals(200, response.statusCode)
}