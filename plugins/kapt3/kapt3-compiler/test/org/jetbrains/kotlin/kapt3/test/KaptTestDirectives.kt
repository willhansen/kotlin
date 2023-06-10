/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt3.test

import org.jetbrains.kotlin.base.kapt3.KaptFlag
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object KaptTestDirectives : SimpleDirectivesContainer() {
    konst SHOW_PROCESSOR_STATS by directive("Enables SHOW_PROCESSOR_STATS flag")
    konst VERBOSE by directive("Enables VERBOSE flag")
    konst INFO_AS_WARNINGS by directive("Enables INFO_AS_WARNINGS flag")
    konst USE_LIGHT_ANALYSIS by directive("Enables USE_LIGHT_ANALYSIS flag")
    konst CORRECT_ERROR_TYPES by directive("Enables CORRECT_ERROR_TYPES flag")
    konst DUMP_DEFAULT_PARAMETER_VALUES by directive("Enables DUMP_DEFAULT_PARAMETER_VALUES flag")
    konst MAP_DIAGNOSTIC_LOCATIONS by directive("Enables MAP_DIAGNOSTIC_LOCATIONS flag")
    konst STRICT by directive("Enables STRICT flag")
    konst INCLUDE_COMPILE_CLASSPATH by directive("Enables INCLUDE_COMPILE_CLASSPATH flag")
    konst INCREMENTAL_APT by directive("Enables INCREMENTAL_APT flag")
    konst STRIP_METADATA by directive("Enables STRIP_METADATA flag")
    konst KEEP_KDOC_COMMENTS_IN_STUBS by directive("Enables KEEP_KDOC_COMMENTS_IN_STUBS flag")
    konst USE_JVM_IR by directive("Enables USE_JVM_IR flag")

    konst DISABLED_FLAGS by enumDirective<KaptFlag>("Disables listed flags")

    konst NON_EXISTENT_CLASS by directive("TODO")
    konst NO_VALIDATION by directive("TODO")
    konst EXPECTED_ERROR by stringDirective("TODO()", multiLine = true)

    konst flagDirectives = listOf(
        SHOW_PROCESSOR_STATS, VERBOSE, INFO_AS_WARNINGS, USE_LIGHT_ANALYSIS, CORRECT_ERROR_TYPES,
        DUMP_DEFAULT_PARAMETER_VALUES, MAP_DIAGNOSTIC_LOCATIONS, STRICT, INCLUDE_COMPILE_CLASSPATH,
        INCREMENTAL_APT, STRIP_METADATA, KEEP_KDOC_COMMENTS_IN_STUBS, USE_JVM_IR
    )
}
