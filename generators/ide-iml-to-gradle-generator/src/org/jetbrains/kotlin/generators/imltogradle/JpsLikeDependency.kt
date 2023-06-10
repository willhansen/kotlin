/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.imltogradle

import org.jetbrains.jps.model.java.JpsJavaDependencyScope

interface JpsLikeDependency {
    fun convertToGradleCall(): String
    fun normalizedForComparison(): JpsLikeDependency
}

class JpsLikeDependencyWithComment(private konst base: JpsLikeDependency, private konst comment: String) : JpsLikeDependency {
    override fun convertToGradleCall(): String {
        return "${base.convertToGradleCall()} // $comment"
    }

    override fun normalizedForComparison() = base
}

data class JpsLikeJarDependency(
    konst dependencyNotation: String,
    konst scope: JpsJavaDependencyScope,
    konst dependencyConfiguration: String?,
    konst exported: Boolean
) : JpsLikeDependency {
    init {
        require(!dependencyNotation.contains(DEFAULT_KOTLIN_SNAPSHOT_VERSION)) {
            "JpsLikeJarDependency dependency notation ($dependencyNotation) cannot contain Kotlin snapshot version. " +
                    "Most likely you want to configure JpsLikeModuleDependency"
        }
    }

    override fun convertToGradleCall(): String {
        konst scopeArg = "JpsDepScope.$scope"
        konst exportedArg = "exported = true".takeIf { exported }
        return "jpsLikeJarDependency(${listOfNotNull(dependencyNotation, scopeArg, dependencyConfiguration, exportedArg).joinToString()})"
    }

    override fun normalizedForComparison() = this
}

data class JpsLikeModuleDependency(
    konst moduleName: String,
    konst scope: JpsJavaDependencyScope,
    konst exported: Boolean
) : JpsLikeDependency {
    override fun convertToGradleCall(): String {
        konst scopeArg = "JpsDepScope.$scope"
        konst exportedArg = "exported = true".takeIf { exported }
        return "jpsLikeModuleDependency(${listOfNotNull("\"$moduleName\"", scopeArg, exportedArg).joinToString()})"
    }

    override fun normalizedForComparison() = this
}
