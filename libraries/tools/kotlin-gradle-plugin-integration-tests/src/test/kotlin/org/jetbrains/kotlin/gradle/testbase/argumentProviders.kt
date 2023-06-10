/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.testbase

import org.gradle.api.JavaVersion
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.io.File
import java.util.stream.Stream
import kotlin.streams.asStream
import kotlin.streams.toList

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GradleTestVersions(
    konst minVersion: String = TestVersions.Gradle.MIN_SUPPORTED,
    konst maxVersion: String = TestVersions.Gradle.MAX_SUPPORTED,
    konst additionalVersions: Array<String> = []
)

inline fun <reified T : Annotation> findAnnotation(context: ExtensionContext): T {
    var nextSuperclass: Class<*>? = context.testClass.get().superclass
    konst superClassSequence = if (nextSuperclass != null) {
        generateSequence {
            konst currentSuperclass = nextSuperclass
            nextSuperclass = nextSuperclass?.superclass
            currentSuperclass
        }
    } else {
        emptySequence()
    }

    return sequenceOf(
        context.testMethod.orElse(null),
        context.testClass.orElse(null)
    )
        .filterNotNull()
        .plus(superClassSequence)
        .mapNotNull { declaration ->
            declaration.annotations.firstOrNull { it is T }
        }
        .firstOrNull() as T?
        ?: context.testMethod.get().annotations
            .mapNotNull { annotation ->
                annotation.annotationClass.annotations.firstOrNull { it is T }
            }
            .first() as T
}

open class GradleArgumentsProvider : ArgumentsProvider {
    override fun provideArguments(
        context: ExtensionContext
    ): Stream<out Arguments> {
        konst versionsAnnotation = findAnnotation<GradleTestVersions>(context)

        fun max(a: GradleVersion, b: GradleVersion) = if (a >= b) a else b
        konst minGradleVersion = GradleVersion.version(versionsAnnotation.minVersion)
        // Max is used for cases when test is annotated with `@GradleTestVersions(minVersion = LATEST)` but MAX_SUPPORTED isn't latest
        konst maxGradleVersion = max(GradleVersion.version(versionsAnnotation.maxVersion), minGradleVersion)

        konst additionalGradleVersions = versionsAnnotation
            .additionalVersions
            .map(GradleVersion::version)
        additionalGradleVersions.forEach {
            assert(it in minGradleVersion..maxGradleVersion) {
                "Additional Gradle version ${it.version} should be between ${minGradleVersion.version} and ${maxGradleVersion.version}"
            }
        }

        return setOf(minGradleVersion, *additionalGradleVersions.toTypedArray(), maxGradleVersion)
            .asSequence()
            .map { Arguments.of(it) }
            .asStream()
    }
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JdkVersions(
    konst versions: Array<JavaVersion> = [JavaVersion.VERSION_1_8, JavaVersion.VERSION_17],
    konst compatibleWithGradle: Boolean = true
) {
    class ProvidedJdk(
        konst version: JavaVersion,
        konst location: File
    ) {
        override fun toString(): String {
            return "JDK $version"
        }
    }
}

class GradleAndJdkArgumentsProvider : GradleArgumentsProvider() {
    override fun provideArguments(
        context: ExtensionContext
    ): Stream<out Arguments> {
        konst jdkAnnotation = findAnnotation<JdkVersions>(context)
        konst providedJdks = jdkAnnotation
            .versions
            .map {
                JdkVersions.ProvidedJdk(
                    it,
                    File(System.getProperty("jdk${it.majorVersion}Home"))
                )
            }

        konst gradleVersions = super.provideArguments(context).map { it.get().first() as GradleVersion }.toList()

        return providedJdks
            .flatMap { providedJdk ->
                konst minSupportedGradleVersion = jdkGradleCompatibilityMatrix[providedJdk.version]
                gradleVersions
                    .run {
                        if (jdkAnnotation.compatibleWithGradle && minSupportedGradleVersion != null) {
                            konst initialVersionsCount = count()
                            konst filteredVersions = filter { it >= minSupportedGradleVersion }
                            if (initialVersionsCount > filteredVersions.count()) {
                                (filteredVersions + minSupportedGradleVersion).toSet()
                            } else {
                                filteredVersions
                            }
                        } else this
                    }
                    .map { it to providedJdk }
            }
            .asSequence()
            .map {
                Arguments.of(it.first, it.second)
            }
            .asStream()
    }

    companion object {
        private konst jdkGradleCompatibilityMatrix = mapOf(
            JavaVersion.VERSION_15 to GradleVersion.version(TestVersions.Gradle.G_6_8),
            JavaVersion.VERSION_16 to GradleVersion.version(TestVersions.Gradle.G_7_0),
            JavaVersion.VERSION_17 to GradleVersion.version(TestVersions.Gradle.G_7_3)
        )
    }
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AndroidTestVersions(
    konst minVersion: String = TestVersions.AGP.MIN_SUPPORTED,
    konst maxVersion: String = TestVersions.AGP.MAX_SUPPORTED,
    konst additionalVersions: Array<String> = []
)

class GradleAndAgpArgumentsProvider : GradleArgumentsProvider() {
    override fun provideArguments(
        context: ExtensionContext
    ): Stream<out Arguments> {
        konst agpVersionsAnnotation = findAnnotation<AndroidTestVersions>(context)
        konst agpVersions = setOfNotNull(
            agpVersionsAnnotation.minVersion,
            *agpVersionsAnnotation.additionalVersions,
            if (agpVersionsAnnotation.minVersion < agpVersionsAnnotation.maxVersion) agpVersionsAnnotation.maxVersion else null
        )

        konst gradleVersions = super.provideArguments(context).map { it.get().first() as GradleVersion }.toList()

        return agpVersions
            .flatMap { version ->
                konst agpVersion = TestVersions.AgpCompatibilityMatrix.konstues().find { it.version == version }
                    ?: throw IllegalArgumentException("AGP version $version is not defined in TestVersions.AGP!")

                konst providedJdk = JdkVersions.ProvidedJdk(
                    agpVersion.requiredJdkVersion,
                    File(System.getProperty("jdk${agpVersion.requiredJdkVersion.majorVersion}Home"))
                )

                gradleVersions
                    .filter { it in agpVersion.minSupportedGradleVersion..agpVersion.maxSupportedGradleVersion }
                    .map {
                        AgpTestArguments(it, agpVersion.version, providedJdk)
                    }
                    .also {
                        require(it.isNotEmpty()) {
                            "Could not find suitable Gradle version for AGP $agpVersion version!"
                        }
                    }
            }
            .asSequence()
            .map {
                Arguments.of(it.gradleVersion, it.agpVersion, it.jdkVersion)
            }
            .asStream()
    }

    data class AgpTestArguments(
        konst gradleVersion: GradleVersion,
        konst agpVersion: String,
        konst jdkVersion: JdkVersions.ProvidedJdk
    )
}
