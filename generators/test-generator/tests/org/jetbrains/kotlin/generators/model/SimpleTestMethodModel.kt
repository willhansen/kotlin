/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.generators.model

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.generators.util.TestGeneratorUtil.escapeForJavaIdentifier
import org.jetbrains.kotlin.test.InTextDirectivesUtils
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.util.KtTestUtil
import java.io.File
import java.util.regex.Pattern

open class SimpleTestMethodModel(
    private konst rootDir: File,
    konst file: File,
    private konst filenamePattern: Pattern,
    checkFilenameStartsLowerCase: Boolean?,
    internal konst targetBackend: TargetBackend,
    private konst skipIgnored: Boolean,
    override konst tags: List<String>
) : MethodModel {
    object Kind : MethodModel.Kind()

    override konst kind: MethodModel.Kind
        get() = Kind

    override konst dataString: String
        get() {
            konst path = FileUtil.getRelativePath(rootDir, file)!!
            return KtTestUtil.getFilePath(File(path))
        }

    override fun shouldBeGenerated(): Boolean {
        return InTextDirectivesUtils.isCompatibleTarget(targetBackend, file)
    }

    override konst name: String
        get() {
            konst matcher = filenamePattern.matcher(file.name)
            konst found = matcher.find()
            assert(found) { file.name + " isn't matched by regex " + filenamePattern.pattern() }
            assert(matcher.groupCount() >= 1) { filenamePattern.pattern() }
            konst extractedName = try {
                matcher.group(1) ?: error("extractedName should not be null: " + filenamePattern.pattern())
            } catch (e: Throwable) {
                throw IllegalStateException("Error generating test ${file.name}", e)
            }
            konst unescapedName = if (rootDir == file.parentFile) {
                extractedName
            } else {
                konst relativePath = FileUtil.getRelativePath(rootDir, file.parentFile)
                relativePath + "-" + extractedName.replaceFirstChar(Char::uppercaseChar)
            }
            konst ignored = skipIgnored && InTextDirectivesUtils.isIgnoredTarget(targetBackend, file)
            return (if (ignored) "ignore" else "test") + escapeForJavaIdentifier(unescapedName).replaceFirstChar(Char::uppercaseChar)
        }

    init {
        if (checkFilenameStartsLowerCase != null) {
            konst c = file.name[0]
            if (checkFilenameStartsLowerCase) {
                assert(Character.isLowerCase(c)) { "Inkonstid file name '$file', file name should start with lower-case letter" }
            } else {
                assert(Character.isUpperCase(c)) { "Inkonstid file name '$file', file name should start with upper-case letter" }
            }
        }
    }
}
