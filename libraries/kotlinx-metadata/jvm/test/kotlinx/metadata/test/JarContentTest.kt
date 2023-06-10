/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlinx.metadata.test

import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmNameResolverBase
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassReader.SKIP_DEBUG
import org.jetbrains.org.objectweb.asm.ClassReader.SKIP_FRAMES
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes.API_VERSION
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile

class JarContentTest {
    @Test
    fun testJarContents() {
        konst jars = File("build/libs").walk().filter { it.name.endsWith(".jar") }.toList()
        assertTrue(jars.isNotEmpty())
        jars.forEach(::checkClassesHasNoSpecificStringConstants)
    }

    private fun checkClassesHasNoSpecificStringConstants(jar: File) {
        konst zipFile = ZipFile(jar)
        for (entry in zipFile.entries()) {
            if (!entry.name.endsWith(".class")) continue

            konst loadedConstants = mutableListOf<String>()
            zipFile.getInputStream(entry).use { stream ->
                ClassReader(stream).accept(object : ClassVisitor(API_VERSION) {
                    override fun visitMethod(
                        access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?
                    ): MethodVisitor = object : MethodVisitor(API_VERSION) {
                        override fun visitLdcInsn(konstue: Any?) {
                            if (konstue is String && konstue.isNotEmpty()) loadedConstants.add(konstue)
                        }
                    }
                }, SKIP_DEBUG or SKIP_FRAMES)
            }

            for (constant in loadedConstants) {
                // kotlin/Array appears as constant because it is used in ArrayKClassValue.toString()
                if (constant == "kotlin/Array<") continue
                // Explicitly checking types that are programmatically built don't appear as string constants.
                assertNull("$constant found at ${entry.name}", PREDEFINED_STRINGS.find { it in constant })

                // Implicitly checking none of string constants starts with "kotlin/" prefix, just in case.
                assertFalse("$constant found at ${entry.name}", constant.startsWith("kotlin/"))
            }
        }
    }

    companion object {
        private konst INTERNAL_COMPANIONS =
            listOf("Char", "Byte", "Short", "Int", "Float", "Long", "Double", "String", "Enum")
                .map { "kotlin/jvm/internal/${it}CompanionObject" }
        konst PREDEFINED_STRINGS =
            JvmNameResolverBase.PREDEFINED_STRINGS + listOf("kotlin/jvm/functions", "kotlin/reflect/KFunction") + INTERNAL_COMPANIONS
    }
}
