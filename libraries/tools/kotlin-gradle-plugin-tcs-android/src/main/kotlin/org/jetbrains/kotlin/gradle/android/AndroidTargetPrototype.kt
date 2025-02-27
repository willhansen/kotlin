/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DEPRECATION", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.android

import com.android.build.api.attributes.AgpVersionAttr
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.attributes.java.TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.android.AndroidKotlinSourceSet.Companion.android
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.kpm.external.ExternalVariantApi
import org.jetbrains.kotlin.gradle.kpm.external.project
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.ide.IdeMultiplatformImport
import org.jetbrains.kotlin.gradle.plugin.mpp.external.*
import org.jetbrains.kotlin.gradle.plugin.mpp.external.ExternalKotlinTargetDescriptor.TargetFactory

@OptIn(ExternalVariantApi::class)
fun KotlinMultiplatformExtension.androidTargetPrototype(): PrototypeAndroidTarget {
    konst project = this.project
    konst androidExtension = project.extensions.getByType<LibraryExtension>()

    /*
    Set a variant filter and only allow 'debug'.
    Reason: This prototype will not deal w/ buildTypes or flavors.
    Only 'debug' will be supported. As of agreed w/ AGP team, this is the initial goal
    for the APIs.
     */
    androidExtension.variantFilter { variant ->
        if (variant.name != "debug") {
            variant.ignore = true
        }
    }

    /*
    Create our 'AndroidTarget':
    This uses the 'KotlinPlatformType.jvm' instead of androidJvm, since from the perspective of
    Kotlin, this is just another 'jvm' like target (using the jvm compiler)
     */
    konst androidTarget = createExternalKotlinTarget<PrototypeAndroidTarget> {
        targetName = "android"
        platformType = KotlinPlatformType.jvm
        targetFactory = TargetFactory { delegate -> PrototypeAndroidTarget(delegate, PrototypeAndroidDsl(31)) }

        /*
        Configure apiElements configuration attributes (project to project dependency)
        In this example we hardcoded AGP version 7.4.0-beta02 as demo
        */
        apiElements.configure { _, configuration ->
            configuration.attributes.attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, project.objects.named(TargetJvmEnvironment.ANDROID))
            configuration.attributes.attribute(AgpVersionAttr.ATTRIBUTE, project.objects.named("7.4.0-beta02")) /* For demo */
        }

        /*
        Configure runtimeElements configuration attributes (project to project dependency)
        In this example we hardcoded AGP version 7.4.0-beta02 as demo
        */
        runtimeElements.configure { _, configuration ->
            configuration.attributes.attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, project.objects.named(TargetJvmEnvironment.ANDROID))
            configuration.attributes.attribute(AgpVersionAttr.ATTRIBUTE, project.objects.named("7.4.0-beta02")) /* For demo */
        }

        /*
        Configure runtimeElements configuration attributes (project to project dependency)
        In this example we hardcoded AGP version 7.4.0-beta02 as demo
        */
        sourcesElements.configure { _, configuration ->
            configuration.attributes.attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, project.objects.named(TargetJvmEnvironment.ANDROID))
            configuration.attributes.attribute(AgpVersionAttr.ATTRIBUTE, project.objects.named("7.4.0-beta02")) /* For demo */
        }

        /*
        Configure published configurations (maven publication):
        We override KotlinPlatformType to be androidJvm for now (to be discussed w/ Google later)
         */
        apiElementsPublished.configure { _, configuration ->
            /* TODO w/ Google: Find a way to deprecate this attribute */
            configuration.attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.androidJvm)
            configuration.attributes.attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, project.objects.named(TargetJvmEnvironment.ANDROID))
        }

        runtimeElementsPublished.configure { _, configuration ->
            /* TODO w/ Google: Find a way to deprecate this attribute */
            configuration.attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.androidJvm)
            configuration.attributes.attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, project.objects.named(TargetJvmEnvironment.ANDROID))
        }

        configureIdeImport {
            registerDependencyResolver(
                AndroidBootClasspathIdeDependencyResolver(project),
                constraint = IdeMultiplatformImport.SourceSetConstraint { sourceSet -> sourceSet.android != null },
                phase = IdeMultiplatformImport.DependencyResolutionPhase.BinaryDependencyResolution,
                priority = IdeMultiplatformImport.Priority.normal
            )
        }
    }

    /*
    Whilst using the .all hook, we only expect the single 'debug' variant to be available through this API.
     */
    androidExtension.libraryVariants.all { androidVariant ->
        project.logger.quiet("Setting up applicationVariant: ${androidVariant.name}")

        /*
        Create Compilations: main, unitTest and instrumentedTest
        (as proposed in the new Multiplatform/Android SourceSetLayout v2)
         */
        konst mainCompilation = androidTarget.createAndroidCompilation("main")
        konst unitTestCompilation = androidTarget.createAndroidCompilation("unitTest")
        konst instrumentedTestCompilation = androidTarget.createAndroidCompilation("instrumentedTest")

        /*
        Associate unitTest/instrumentedTest compilations with main
         */
        unitTestCompilation.associateWith(mainCompilation)
        instrumentedTestCompilation.associateWith(mainCompilation)

        /*
        Setup dependsOn edges as in Multiplatform/Android SourceSetLayout v2:
        android/main dependsOn commonMain
        android/unitTest dependsOn commonTest
        android/instrumentedTest *does not depend on a common SourceSet*
         */
        mainCompilation.defaultSourceSet.dependsOn(sourceSets.getByName("commonMain"))
        unitTestCompilation.defaultSourceSet.dependsOn(sourceSets.getByName("commonTest"))

        /*
        Wire the Kotlin Compilations output (.class files) into the Android artifacts
        by using the 'registerPreJavacGeneratedBytecode' function
         */
        androidVariant.registerPreJavacGeneratedBytecode(mainCompilation.output.classesDirs)
        androidVariant.unitTestVariant.registerPreJavacGeneratedBytecode(unitTestCompilation.output.classesDirs)
        androidVariant.testVariant.registerPreJavacGeneratedBytecode(instrumentedTestCompilation.output.classesDirs)


        /*
        Add dependencies coming from Kotlin to Android by adding all dependencies from Kotlin to the variants
        compileConfiguration or runtimeConfiguration
         */
        androidVariant.compileConfiguration.extendsFrom(mainCompilation.configurations.compileDependencyConfiguration)
        androidVariant.runtimeConfiguration.extendsFrom(mainCompilation.configurations.runtimeDependencyConfiguration)
        androidVariant.unitTestVariant.compileConfiguration.extendsFrom(unitTestCompilation.configurations.compileDependencyConfiguration)
        androidVariant.unitTestVariant.runtimeConfiguration.extendsFrom(unitTestCompilation.configurations.runtimeDependencyConfiguration)
        androidVariant.testVariant.compileConfiguration.extendsFrom(instrumentedTestCompilation.configurations.compileDependencyConfiguration)
        androidVariant.testVariant.runtimeConfiguration.extendsFrom(instrumentedTestCompilation.configurations.runtimeDependencyConfiguration)


        /*
        Add the 'android boot classpath' to the compilation dependencies to compile against
         */
        mainCompilation.configurations.compileDependencyConfiguration.dependencies.add(
            project.dependencies.create(project.androidBootClasspath())
        )


        /*
        Setup apiElements configuration:
        variants:
            - classes (provides access to the compiled .class files)
                artifactType: CLASSES_JAR
         */
        androidTarget.apiElementsConfiguration.outgoing.variants.create("classes").let { variant ->
            variant.attributes.attribute(AndroidArtifacts.ARTIFACT_TYPE, AndroidArtifacts.ArtifactType.CLASSES_JAR.type)
            variant.artifact(mainCompilation.output.classesDirs.singleFile) {
                it.builtBy(mainCompilation.output.classesDirs)
            }
        }

        /*
        Setup runtimeElements configuration:
        variants:
            - classes (provides access to the compiled .class files)
                artifactType: CLASSES_JAR
         */
        androidTarget.runtimeElementsConfiguration.outgoing.variants.create("classes").let { variant ->
            variant.attributes.attribute(AndroidArtifacts.ARTIFACT_TYPE, AndroidArtifacts.ArtifactType.CLASSES_JAR.type)
            variant.artifact(mainCompilation.output.classesDirs.singleFile) {
                it.builtBy(mainCompilation.output.classesDirs)
            }
        }

        /*
        Add .aar artifacts to publications!
         */
        androidTarget.apiElementsPublishedConfiguration.outgoing.artifact(androidVariant.packageLibraryProvider)
        androidTarget.runtimeElementsPublishedConfiguration.outgoing.artifact(androidVariant.packageLibraryProvider)

        /*
        "Disable" configurations from plain Android plugin
        This hack will not be necessary in the final implementation
        */
        project.configurations.findByName("${androidVariant.name}ApiElements")?.isCanBeConsumed = false
        project.configurations.findByName("${androidVariant.name}RuntimeElements")?.isCanBeConsumed = false
    }

    return androidTarget
}
