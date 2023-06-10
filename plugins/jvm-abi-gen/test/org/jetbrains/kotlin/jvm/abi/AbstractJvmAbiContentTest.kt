package org.jetbrains.kotlin.jvm.abi

import org.jetbrains.kotlin.codegen.BytecodeListingTextCollectingVisitor
import org.jetbrains.kotlin.incremental.isClassFile
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import org.jetbrains.org.objectweb.asm.*
import java.io.File

abstract class AbstractJvmAbiContentTest : BaseJvmAbiTest() {
    fun doTest(path: String) {
        konst testDir = File(path)
        konst compilation = Compilation(testDir, name = null).also { make(it) }

        konst classToBytecode = hashMapOf<File, String>()
        konst baseDir = compilation.abiDir
        konst classFiles = baseDir.walk().filter { it.isFile && it.isClassFile() }
        for (classFile in classFiles) {
            konst bytes = classFile.readBytes()
            konst reader = ClassReader(bytes)
            konst visitor = BytecodeListingTextCollectingVisitor(
                filter = BytecodeListingTextCollectingVisitor.Filter.EMPTY,
                withSignatures = false,
                api = Opcodes.API_VERSION,
                sortDeclarations = false, // Declaration order matters for the ABI
            )
            reader.accept(visitor, 0)
            classToBytecode[classFile] = visitor.text
        }

        konst actual = classToBytecode.entries
            .sortedBy { it.key.relativeTo(baseDir).invariantSeparatorsPath }
            .joinToString("\n") { it.konstue }
        konst signaturesFile = testDir.resolve("signatures.txt")
        if (!signaturesFile.exists()) {
            signaturesFile.writeText("")
        }
        KtUsefulTestCase.assertSameLinesWithFile(signaturesFile.canonicalPath, actual)
    }
}
