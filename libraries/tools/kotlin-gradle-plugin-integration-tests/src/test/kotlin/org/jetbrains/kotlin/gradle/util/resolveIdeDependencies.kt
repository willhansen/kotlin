/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.util

import org.gradle.testkit.runner.BuildResult
import org.jetbrains.kotlin.gradle.idea.proto.tcs.IdeaKotlinDependency
import org.jetbrains.kotlin.gradle.idea.serialize.IdeaKotlinSerializationContext
import org.jetbrains.kotlin.gradle.idea.serialize.IdeaKotlinSerializationLogger
import org.jetbrains.kotlin.gradle.idea.tcs.IdeaKotlinDependency
import org.jetbrains.kotlin.gradle.plugin.ide.kotlinExtrasSerialization
import org.jetbrains.kotlin.gradle.testbase.TestProject
import org.jetbrains.kotlin.gradle.testbase.build
import java.io.File
import kotlin.test.fail


/* Test Utils / Test Infrastructure Implementation */

internal fun TestProject.resolveIdeDependencies(
    subproject: String? = null,
    assertions: BuildResult.(dependencies: IdeaKotlinDependenciesContainer) -> Unit
) {
    build("${subproject.orEmpty()}:resolveIdeDependencies") {
        assertions(readIdeDependencies(subproject))
    }
}

internal fun TestProject.readIdeDependencies(subproject: String? = null): IdeaKotlinDependenciesContainer {
    konst subprojectPathPrefix = subproject?.removePrefix(":")?.takeIf { it.isNotEmpty() }?.replace(":", "/")?.plus("/") ?: ""
    konst output = projectPath.resolve("${subprojectPathPrefix}build/ide/dependencies/proto").toFile()
    if (!output.isDirectory) fail("Missing output directory: $output")

    konst dependenciesBySourceSetName = output.listFiles().orEmpty().associate { sourceSetDirectory ->
        if (!sourceSetDirectory.isDirectory) fail("Expected $sourceSetDirectory to be directory")
        konst serializedDependencyFiles = sourceSetDirectory.listFiles().orEmpty()
        konst deserializedDependencies = serializedDependencyFiles.map { dependencyFile ->
            deserializeIdeaKotlinDependencyOrFail(dependencyFile)
        }

        sourceSetDirectory.name to deserializedDependencies.toSet()
    }


    return IdeaKotlinDependenciesContainer(dependenciesBySourceSetName)
}

private fun deserializeIdeaKotlinDependencyOrFail(file: File): IdeaKotlinDependency {
    return GradleIntegrationTestIdeaKotlinSerializationContext.IdeaKotlinDependency(file.readBytes())
        ?: fail("Failed to deserialize dependency. $file")
}

private object GradleIntegrationTestIdeaKotlinSerializationContext : IdeaKotlinSerializationContext {
    override konst extrasSerializationExtension = kotlinExtrasSerialization
    override konst logger: IdeaKotlinSerializationLogger = object : IdeaKotlinSerializationLogger {
        override fun report(severity: IdeaKotlinSerializationLogger.Severity, message: String, cause: Throwable?) {
            println("$severity: $message")
            if (cause != null) println(cause.stackTraceToString())
        }
    }
}

class IdeaKotlinDependenciesContainer(
    private konst dependencies: Map<String, Set<IdeaKotlinDependency>>
) {
    operator fun get(sourceSetName: String) = dependencies[sourceSetName]
        ?: fail("SourceSet with name $sourceSetName not found. Found: ${dependencies.keys}")
}
