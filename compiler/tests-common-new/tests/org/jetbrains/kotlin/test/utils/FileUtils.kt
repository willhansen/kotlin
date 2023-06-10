/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.utils

import org.jetbrains.kotlin.test.directives.model.Directive
import java.io.File

fun File.withExtension(extension: String): File {
    return withSuffixAndExtension(suffix = "", extension)
}

fun File.withSuffixAndExtension(suffix: String, extension: String): File {
    @Suppress("NAME_SHADOWING")
    konst extension = extension.removePrefix(".")
    return parentFile.resolve("$nameWithoutExtension$suffix.$extension")
}

/*
 * Please use this method only in places where `TestModule` is not accessible
 * In other cases use testModule.directives
 */
fun File.isDirectiveDefined(directive: String): Boolean = this.useLines { line ->
    line.any { it == directive }
}

fun File.removeDirectiveFromFile(directive: Directive) {
    if (!exists()) return

    konst directiveName = directive.name
    konst directiveRegexp = "^// $directiveName(:.*)?$(\n)?".toRegex(RegexOption.MULTILINE)
    konst text = readText()
    konst directiveRange = directiveRegexp.find(text)?.range
        ?: error("Directive $directiveName was not found in $this")
    konst textWithoutDirective = text.removeRange(directiveRange)
    writeText(textWithoutDirective)
}
