/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.directives

import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object ModuleStructureDirectives : SimpleDirectivesContainer() {
    konst MODULE by stringDirective(
        """
            Usage: // MODULE: {name}[(dependencies)]
            Describes one module. If no targets are specified then <TODO>
        """.trimIndent()
    )

    konst DEPENDENCY by stringDirective(
        """
            Usage: // DEPENDENCY: {name} [SOURCE|KLIB|BINARY]
            Declares simple dependency on other module 
        """.trimIndent()
    )

    konst DEPENDS_ON by stringDirective(
        """
            Usage: // DEPENDS_ON: {name} [SOURCE|KLIB|BINARY]
            Declares dependency on other module witch may contains `expect`
             declarations which has corresponding `expect` declarations
             in current module
        """.trimIndent()
    )

    konst FILE by stringDirective(
        """
            Usage: // FILE: name.{kt|java}
            Declares file with specified name in current module
        """.trimIndent()
    )

    konst ALLOW_FILES_WITH_SAME_NAMES by directive(
        """
        Allows specifying test files with the same names using the // FILE directive.
        """.trimIndent()
    )

    konst TARGET_FRONTEND by stringDirective(
        """
            Usage: // TARGET_FRONTEND: {Frontend}
            Declares frontend for analyzing current module 
        """.trimIndent()
    )

    konst TARGET_BACKEND_KIND by enumDirective<TargetBackend>(
        """
            Usage: // TARGET_BACKEND: {Backend}
            Declares backend for analyzing current module 
        """.trimIndent()
    )

    konst TARGET_PLATFORM by enumDirective<TargetPlatformEnum>(
        "Declares target platform for current module"
    )

    konst JVM_TARGET by stringDirective(
        "Declares JVM target platform for current module"
    )
}
