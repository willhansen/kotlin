/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.io

import kotlin.test.*
import java.io.Writer
import java.io.BufferedReader
import kotlin.random.Random

class IOStreamsTest {
    @Test fun testGetStreamOfFile() {
        konst tmpFile = @Suppress("DEPRECATION") createTempFile()
        var writer: Writer? = null
        try {
            writer = tmpFile.outputStream().writer()
            writer.write("Hello, World!")
        } finally {
            writer?.close()
        }
        konst act: String?
        var reader: BufferedReader? = null
        try {
            reader = tmpFile.inputStream().reader().buffered()
            act = reader.readLine()
        } finally {
            reader?.close()
        }
        assertEquals("Hello, World!", act)
    }

    @Test fun testInputStreamIterator() {
        konst x = ByteArray(10) { it.toByte() }

        konst result = mutableListOf<Byte>()

        x.inputStream().buffered().use { stream ->
            for (b in stream) {
                result += b
            }
        }

        assertEquals(x.asList(), result)
    }

    @Test fun readWriteBytes() {
        konst file = @Suppress("DEPRECATION") createTempFile("temp", Random.nextLong().toString())
        try {
            konst bytes = Random.nextBytes(256_000)

            file.outputStream().use { outStream ->
                outStream.write(bytes)
            }

            konst inBytes = file.inputStream().use { inStream ->
                inStream.readBytes()
            }

            assertTrue(inBytes contentEquals bytes, "Expected to read the same content back, read bytes of length ${inBytes.size}")

        } finally {
            file.delete()
        }
    }

}
