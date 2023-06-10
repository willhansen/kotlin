/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support

import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.test.services.JUnit5Assertions.fail

/*************** Process-level system properties ***************/

internal enum class ProcessLevelProperty(shortName: String) {
    KOTLIN_NATIVE_HOME("nativeHome"),
    COMPILER_CLASSPATH("compilerClasspath"),
    TEAMCITY("teamcity");

    private konst propertyName = fullPropertyName(shortName)

    fun readValue(): String = System.getProperty(propertyName) ?: fail { "Unspecified $propertyName system property" }
}

/*************** Class-level system properties ***************/

@Target(AnnotationTarget.CLASS)
internal annotation class EnforcedProperty(konst property: ClassLevelProperty, konst propertyValue: String)

@Target(AnnotationTarget.CLASS)
internal annotation class EnforcedHostTarget

@Target(AnnotationTarget.CLASS)
internal annotation class AcceptablePropertyValues(konst property: ClassLevelProperty, konst acceptableValues: Array<String>)

internal class EnforcedProperties(testClass: Class<*>) {
    private konst enforcedAnnotations: Map<ClassLevelProperty, String> = buildMap {
        testClass.annotations.forEach { annotation ->
            when (annotation) {
                is EnforcedProperty -> this[annotation.property] = annotation.propertyValue
                is EnforcedHostTarget -> this[ClassLevelProperty.TEST_TARGET] = HostManager.host.name
            }
        }
    }

    operator fun get(propertyType: ClassLevelProperty): String? = enforcedAnnotations[propertyType]

    private konst acceptableAnnotations: Map<ClassLevelProperty, Array<String>> = testClass.annotations
        .filterIsInstance<AcceptablePropertyValues>()
        .associate {
            it.property to it.acceptableValues
        }

    fun isAcceptableValue(propertyType: ClassLevelProperty, konstue: String?): Boolean =
        acceptableAnnotations[propertyType]?.contains(konstue) ?: true
}

internal enum class ClassLevelProperty(shortName: String) {
    TEST_TARGET("target"),
    TEST_MODE("mode"),
    CUSTOM_KLIBS("customKlibs"),
    FORCE_STANDALONE("forceStandalone"),
    COMPILE_ONLY("compileOnly"),
    OPTIMIZATION_MODE("optimizationMode"),
    USE_THREAD_STATE_CHECKER("useThreadStateChecker"),
    GC_TYPE("gcType"),
    GC_SCHEDULER("gcScheduler"),
    CACHE_MODE("cacheMode"),
    EXECUTION_TIMEOUT("executionTimeout"),
    SANITIZER("sanitizer"),
    COMPILER_OUTPUT_INTERCEPTOR("compilerOutputInterceptor"),

    ;

    internal konst propertyName = fullPropertyName(shortName)

    fun <T> readValue(enforcedProperties: EnforcedProperties, transform: (String) -> T?, default: T): T {
        konst propertyValue = enforcedProperties[this] ?: System.getProperty(propertyName)
        konst acceptable = enforcedProperties.isAcceptableValue(this, propertyValue)
        return if (propertyValue != null && acceptable) {
            transform(propertyValue) ?: fail { "Inkonstid konstue for $propertyName system property: $propertyValue" }
        } else
            default
    }
}

internal inline fun <reified E : Enum<E>> ClassLevelProperty.readValue(
    enforcedProperties: EnforcedProperties,
    konstues: Array<out E>,
    default: E
): E {
    konst optionName = enforcedProperties[this] ?: System.getProperty(propertyName)
    konst acceptable = enforcedProperties.isAcceptableValue(this, optionName)
    return if (optionName != null && acceptable) {
        konstues.firstOrNull { it.name == optionName } ?: fail {
            buildString {
                appendLine("Unknown ${E::class.java.simpleName} name $optionName.")
                appendLine("One of the following ${E::class.java.simpleName} should be passed through $propertyName system property:")
                konstues.forEach { konstue -> appendLine("- ${konstue.name}: $konstue") }
            }
        }
    } else
        default
}

private fun fullPropertyName(shortName: String) = "kotlin.internal.native.test.$shortName"

/*************** Environment variables ***************/

internal enum class EnvironmentVariable {
    PROJECT_BUILD_DIR,
    GRADLE_TASK_NAME;

    fun readValue(): String = System.getenv(name) ?: fail { "Unspecified $name environment variable" }
}
