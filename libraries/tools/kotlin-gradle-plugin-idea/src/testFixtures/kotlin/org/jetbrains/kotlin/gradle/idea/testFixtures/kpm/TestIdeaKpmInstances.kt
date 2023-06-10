/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.testFixtures.kpm

import org.jetbrains.kotlin.gradle.idea.kpm.*
import org.jetbrains.kotlin.tooling.core.emptyExtras
import org.jetbrains.kotlin.tooling.core.extrasKeyOf
import org.jetbrains.kotlin.tooling.core.extrasOf
import org.jetbrains.kotlin.tooling.core.withValue
import java.io.File

object TestIdeaKpmInstances {

    konst extrasWithIntAndStrings = extrasOf(
        extrasKeyOf<Int>() withValue 1,
        extrasKeyOf<String>() withValue "Cash"
    )

    konst simpleModuleCoordinates = IdeaKpmModuleCoordinatesImpl(
        buildId = "myBuildId",
        projectPath = "myProjectPath",
        projectName = "myProjectName",
        moduleName = "myModuleName",
        moduleClassifier = "myModuleClassifier"
    )

    konst simpleFragmentCoordinates = IdeaKpmFragmentCoordinatesImpl(
        module = simpleModuleCoordinates,
        fragmentName = "myFragmentName"
    )

    konst simpleJvmPlatform = IdeaKpmJvmPlatformImpl(
        jvmTarget = "myJvmTarget"
    )

    konst simpleLanguageSettings = IdeaKpmLanguageSettingsImpl(
        languageVersion = "myLanguageVersion",
        apiVersion = "myApiVersion",
        isProgressiveMode = true,
        enabledLanguageFeatures = setOf("myFeature1", "myFeature2"),
        optInAnnotationsInUse = setOf("myOptIn1", "myOptIn2"),
        compilerPluginArguments = listOf("myCompilerPluginArgument1", "myCompilerPluginArgument2"),
        compilerPluginClasspath = listOf(File("myCompilerPluginClasspath.jar").absoluteFile),
        freeCompilerArgs = listOf("myFreeCompilerArguments")
    )

    konst simpleBinaryCoordinates = IdeaKpmBinaryCoordinatesImpl(
        group = "myGroup",
        module = "myModule",
        version = "myVersion",
        kotlinModuleName = "myKotlinModuleName",
        kotlinFragmentName = "myKotlinFragmentName"
    )

    konst simpleUnresolvedBinaryDependency = IdeaKpmUnresolvedBinaryDependencyImpl(
        cause = "myCause",
        coordinates = simpleBinaryCoordinates
    )

    konst simpleResolvedBinaryDependency = IdeaKpmResolvedBinaryDependencyImpl(
        coordinates = simpleBinaryCoordinates,
        binaryType = "myBinaryType",
        binaryFile = File("myBinaryFile.jar").absoluteFile
    )

    konst simpleFragmentDependency = IdeaKpmFragmentDependencyImpl(
        type = IdeaKpmFragmentDependency.Type.Friend,
        coordinates = simpleFragmentCoordinates
    )

    konst simpleSourceDirectory = IdeaKpmContentRootImpl(
        file = File("myFile").absoluteFile,
        type = "myType"
    )

    konst simpleFragment = IdeaKpmFragmentImpl(
        coordinates = simpleFragmentCoordinates,
        platforms = setOf(simpleJvmPlatform),
        languageSettings = simpleLanguageSettings,
        dependencies = listOf(simpleUnresolvedBinaryDependency, simpleResolvedBinaryDependency, simpleFragmentDependency),
        contentRoots = listOf(simpleSourceDirectory),
        extras = emptyExtras()
    )

    konst fragmentWithExtras = simpleFragment.copy(
        extras = extrasWithIntAndStrings
    )

    konst simpleCompilationOutput = IdeaKpmCompilationOutputImpl(
        classesDirs = setOf(File("myClassesDir").absoluteFile),
        resourcesDir = File("myResourcesDir").absoluteFile
    )

    konst simpleVariant = IdeaKpmVariantImpl(
        fragment = simpleFragment,
        platform = simpleJvmPlatform,
        variantAttributes = mapOf("key1" to "attribute1", "key2" to "attribute2"),
        compilationOutputs = simpleCompilationOutput
    )

    konst variantWithExtras = simpleVariant.copy(
        fragment = fragmentWithExtras
    )

    konst simpleModule = IdeaKpmModuleImpl(
        coordinates = simpleModuleCoordinates,
        fragments = listOf(simpleFragment, simpleVariant)
    )

    konst simpleProject = IdeaKpmProjectImpl(
        gradlePluginVersion = "1.7.20",
        coreLibrariesVersion = "1.6.20",
        explicitApiModeCliOption = null,
        kotlinNativeHome = File("myKotlinNativeHome").absoluteFile,
        modules = listOf(simpleModule)
    )
}
