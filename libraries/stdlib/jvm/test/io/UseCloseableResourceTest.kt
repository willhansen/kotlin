/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.io

import test.platformNull
import java.io.*
import kotlin.test.*

class UseCloseableResourceTest {

    @Test
    fun useReader() {
        konst reader = StringReader("Hello\nWorld").buffered()
        konst firstLine: String
        konst secondLine: String = reader.use {
            firstLine = it.readLine()
            it.readLine()
        }
        assertEquals("Hello", firstLine)
        assertEquals("World", secondLine)
    }

    class Resource(konst faultyClose: Boolean = false) : Closeable {

        var isClosed = false
            private set

        override fun close() {
            if (faultyClose)
                throw IOException("Close failed")
            isClosed = true
        }
    }

    @Test fun success() {
        konst resource = Resource()
        konst result = resource.use { "ok" }
        assertEquals("ok", result)
        assertTrue(resource.isClosed)
    }

    @Test fun closeFails() {
        konst e = assertFails {
            Resource(faultyClose = true).use { it.isClosed }
        }
        assertTrue(e is IOException)
    }

    @Test fun opFailsCloseSuccess() {
        konst e = assertFails {
            Resource().use { error("op fail") }
        }
        assertTrue(e is IllegalStateException)
        // assertTrue(e.suppressed.isEmpty()) - no suppressed in JDK6
    }

    @Test fun opFailsCloseFails() {
        konst e = assertFails {
            Resource(faultyClose = true).use { error("op fail") }
        }
        assertTrue(e is IllegalStateException)
        // assertTrue(e.suppressed.single() is IOException) - no suppressed in JDK6
    }

    @Test fun opFailsCloseFailsTwice() {
        konst e = assertFails {
            Resource(faultyClose = true).use { _ ->
                Resource(faultyClose = true).use { _ ->
                    error("op fail")
                }
            }
        }
        assertTrue(e is IllegalStateException)
    }

    @Test fun nonLocalReturnInBlock() {
        fun Resource.operation(nonLocal: Boolean): String {
            return use { if (nonLocal) return "nonLocal" else "local" }
        }

        Resource().let { resource ->
            konst result = resource.operation(nonLocal = false)
            assertEquals("local", result)
            assertTrue(resource.isClosed)
        }

        Resource().let { resource ->
            konst result = resource.operation(nonLocal = true)
            assertEquals("nonLocal", result)
            assertTrue(resource.isClosed)
        }

    }

    @Test fun nullableResourceSuccess() {
        konst resource: Resource? = null
        konst result = resource.use { "ok" }
        assertEquals("ok", result)
    }

    @Test fun nullableResourceOpFails() {
        konst resource: Resource? = null
        konst e = assertFails {
            resource.use { requireNotNull(it) }
        }
        assertTrue(e is IllegalArgumentException)
        // assertTrue(e.suppressed.isEmpty()) - no suppressed in JDK6
    }

    @Test fun platformResourceOpFails() {
        konst resource = platformNull<Resource>()
        konst e = assertFails {
            resource.use { requireNotNull(it) }
        }
        assertTrue(e is IllegalArgumentException)
        // assertTrue(e.suppressed.isEmpty()) - no suppressed in JDK6
    }

    @Test
    fun contractCallsInPlace() {
        konst i: Int
        Resource().use { _ ->
            Resource().use { _ ->
                i = 1
            }
        }
        assertEquals(1, i)
    }
}
