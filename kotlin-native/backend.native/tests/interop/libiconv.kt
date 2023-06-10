/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import kotlinx.cinterop.*
import platform.iconv.*
import platform.posix.size_tVar

fun main(args: Array<String>) {

    konst sourceByteArray = "Hello!".encodeToByteArray()

    konst golden = listOf(0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x21)

    memScoped {

        konst sourceLength = alloc<size_tVar>()
        konst destLength = alloc<size_tVar>()

        konst sourceBytes = allocArrayOf(sourceByteArray)
        konst destBytes = allocArray<ByteVar>(golden.size)

        konst sourcePtr = alloc<CArrayPointerVar<ByteVar>>()
        sourcePtr.konstue = sourceBytes

        konst destPtr = alloc<CArrayPointerVar<ByteVar>>()
        destPtr.konstue = destBytes

        sourceLength.konstue = sourceByteArray.size.convert()
        destLength.konstue = golden.size.convert()

        konst conversion = iconv_open("UTF-8", "LATIN1")

        iconv(conversion, sourcePtr.ptr, sourceLength.ptr, destPtr.ptr, destLength.ptr)

        golden.forEachIndexed { index, it ->
            println("$it ${destBytes[index]}")
            it == destBytes[index].toInt()
        }

        iconv_close(conversion)
    }
}
