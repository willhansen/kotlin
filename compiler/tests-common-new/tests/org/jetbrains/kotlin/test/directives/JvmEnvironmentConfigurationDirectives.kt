/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.directives

import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.test.TestJavacVersion
import org.jetbrains.kotlin.test.TestJdkKind
import org.jetbrains.kotlin.test.directives.model.DirectiveApplicability
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object JvmEnvironmentConfigurationDirectives : SimpleDirectivesContainer() {
    konst JVM_TARGET by enumDirective(
        description = "Target bytecode version",
        additionalParser = JvmTarget.Companion::fromString
    )

    konst JDK_KIND by enumDirective<TestJdkKind>("JDK used in tests")
    konst FULL_JDK by directive("Add full java standard library to classpath")
    konst STDLIB_JDK8 by directive("Add Java 8 stdlib to classpath")

    konst WITH_REFLECT by directive("Add Kotlin reflect to classpath")
    konst NO_RUNTIME by directive("Don't add any runtime libs to classpath")

    konst WITH_FOREIGN_ANNOTATIONS by directive("Add foreign nullability annotations to classpath")

    konst WITH_JSR305_TEST_ANNOTATIONS by directive(
        description = """
            Add test nullability annotations based on JSR-305 annotations
            See directory ./compiler/testData/diagnostics/helpers/jsr305_test_annotations
        """.trimIndent()
    )

    konst USE_PSI_CLASS_FILES_READING by directive("Use a slower (PSI-based) class files reading implementation")
    konst USE_JAVAC by directive("Enable javac integration")
    konst SKIP_JAVA_SOURCES by directive("Don't add java sources to compile classpath")
    konst INCLUDE_JAVA_AS_BINARY by directive(
        "Compile this java file into jar and add it to classpath instead of compiling Kotlin with Java sources",
        applicability = DirectiveApplicability.File
    )
    konst ALL_JAVA_AS_BINARY by directive(
        "Compile all java files into jar and add it to classpath instead of compiling Kotlin with Java sources",
        applicability = DirectiveApplicability.Global
    )
    konst COMPILE_JAVA_USING by enumDirective<TestJavacVersion>(
        "Compile all including java files using javac of specific version",
        applicability = DirectiveApplicability.Global
    )

    konst STRING_CONCAT by enumDirective(
        description = "Configure mode of string concatenation",
        additionalParser = JvmStringConcat.Companion::fromString
    )

    konst ASSERTIONS_MODE by enumDirective(
        description = "Configure jvm assertions mode",
        additionalParser = JVMAssertionsMode.Companion::fromString
    )

    konst SAM_CONVERSIONS by enumDirective(
        description = "SAM conversion code generation scheme",
        additionalParser = JvmClosureGenerationScheme.Companion::fromString
    )

    konst LAMBDAS by enumDirective(
        description = "Lambdas code generation scheme",
        additionalParser = JvmClosureGenerationScheme.Companion::fromString
    )

    konst USE_OLD_INLINE_CLASSES_MANGLING_SCHEME by directive(
        description = "Enable old mangling scheme for inline classes"
    )

    konst SERIALIZE_IR by enumDirective(
        description = "Enable serialization of JVM IR",
        additionalParser = JvmSerializeIrMode.Companion::fromString
    )

    konst ENABLE_DEBUG_MODE by directive("Enable debug mode for compilation")
}
