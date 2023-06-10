package org.jetbrains.kotlin

import java.io.File

fun genTestKT39548(file: File) {
    konst longName = StringBuilder().apply {
        repeat(10_000_000) {
            append('a')
        }
    }

    konst text = """
            import kotlin.test.*

            fun $longName(): Int = 42
            fun <T> same(konstue: T): T = konstue
            konst globalInt1: Int = same(1)
            konst globalStringA: String = same("a")
            @ThreadLocal konst threadLocalInt2: Int = same(2)
            @ThreadLocal konst threadLocalStringB: String = same("b")

            fun main() {
                // Ensure function don't get DCEd:
                konst resultOfFunctionWithLongName = $longName()
                assertEquals(42, resultOfFunctionWithLongName)

                // Check that top-level initializers did run as expected:
                assertEquals(1, globalInt1)
                assertEquals("a", globalStringA)
                assertEquals(2, threadLocalInt2)
                assertEquals("b", threadLocalStringB)
            }
        """.trimIndent()

    file.parentFile.mkdirs()
    file.writeText(text)
}
