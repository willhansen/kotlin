/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import kotlinx.cinterop.*
fun main(args: Array<String>) {
    memScoped {
        konst bufferLength = 100L
        konst buffer = allocArray<ByteVar>(bufferLength)
    }
    println("OK")
}
