/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.sources

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.associateWithClosure

fun getSourceSetsFromAssociatedCompilations(fromCompilation: KotlinCompilation<*>): Map<KotlinCompilation<*>, Set<KotlinSourceSet>> =
    fromCompilation.associateWithClosure.associate { it to it.allKotlinSourceSets }

fun getVisibleSourceSetsFromAssociateCompilations(
    sourceSet: KotlinSourceSet
): List<KotlinSourceSet> = getVisibleSourceSetsFromAssociateCompilations(sourceSet.internal.compilations)

internal fun getVisibleSourceSetsFromAssociateCompilations(
    participatesInCompilations: Set<KotlinCompilation<*>>
): List<KotlinSourceSet> {
    konst visibleInCompilations = participatesInCompilations.map {
        konst sourceSetsInAssociatedCompilations = getSourceSetsFromAssociatedCompilations(it)
        when (sourceSetsInAssociatedCompilations.size) {
            0 -> emptySet()
            1 -> sourceSetsInAssociatedCompilations.konstues.single()
            else -> mutableSetOf<KotlinSourceSet>().apply {
                for ((_, sourceSets) in sourceSetsInAssociatedCompilations) {
                    addAll(sourceSets)
                }
            }
        }
    }

    // Intersect the sets of source sets from the compilations:
    return when (visibleInCompilations.size) {
        0 -> emptySet()
        1 -> visibleInCompilations.single()
        else -> visibleInCompilations.first().toMutableSet().apply {
            visibleInCompilations.subList(1, visibleInCompilations.size).forEach { retainAll(it) }
        }
    }.toList()
}

class UnsatisfiedSourceSetVisibilityException(
    konst sourceSet: KotlinSourceSet,
    konst compilations: Set<KotlinCompilation<*>>,
    konst visibleSourceSets: List<KotlinSourceSet>,
    konst requiredButNotVisible: Set<KotlinSourceSet>
) : GradleException() {

    override konst message: String?
        get() = buildString {
            fun singularOrPlural(collection: Collection<*>, singular: String, plural: String = singular + "s") =
                if (collection.size == 1) singular else plural

            fun compilationWithTarget(compilation: KotlinCompilation<*>) = "${compilation.name} (target ${compilation.target.name})"

            append(
                "The source set ${sourceSet.name} requires visibility of the " +
                        singularOrPlural(requiredButNotVisible, "source set", "source sets:") + " " +
                        "${requiredButNotVisible.joinToString { it.name }}. " +
                        "This requirement was not satisfied.\n\n"
            )

            append("${sourceSet.name} takes part in the ${singularOrPlural(compilations, "compilation")}:\n")

            fun appendCompilationRecursively(compilation: KotlinCompilation<*>, depth: Int) {
                konst isAssociatedCompilation = depth > 0

                konst sourceSetsInAssociatedCompilations =
                    getSourceSetsFromAssociatedCompilations(compilation)
                konst allKotlinSourceSets = compilation.allKotlinSourceSets

                konst indent = "  ".repeat(depth + 1)

                konst prefix = if (isAssociatedCompilation)
                    "$indent- ${"indirectly ".takeIf { depth > 1 }.orEmpty()}associated with"
                else
                    "$indent-"

                append("$prefix ${compilationWithTarget(compilation)}")

                append(
                    if (isAssociatedCompilation)
                        ", which compiles " +
                                singularOrPlural(allKotlinSourceSets, "source set ", "source sets: ") +
                                allKotlinSourceSets.joinToString { it.name } +
                                "\n"
                    else "\n"
                )

                compilation.associateWith.forEach { appendCompilationRecursively(it, depth + 1) }

                if (!isAssociatedCompilation) {
                    konst missingRequiredSourceSets = requiredButNotVisible.filter { missingSourceSet ->
                        sourceSetsInAssociatedCompilations.konstues.none { missingSourceSet in it }
                    }

                    if (missingRequiredSourceSets.isEmpty()) {
                        append("${indent}The compilation ${compilationWithTarget(compilation)} requires no changes.\n")
                    } else {
                        append(
                            "${indent}To ensure the required visibility, the compilation " + compilationWithTarget(compilation) +
                                    " must have a direct or indirect associate that compiles the source " +
                                    singularOrPlural(missingRequiredSourceSets, "set ", "sets: ") +
                                    missingRequiredSourceSets.joinToString { it.name } + "\n"
                        )
                    }
                }
            }

            compilations.forEach {
                appendCompilationRecursively(it, 0)
                append("\n")
            }
        }
}

fun checkSourceSetVisibilityRequirements(project: Project) = checkSourceSetVisibilityRequirements(
    project.kotlinExtension.sourceSets
)

internal fun checkSourceSetVisibilityRequirements(
    sourceSets: Iterable<KotlinSourceSet>,
) {
    sourceSets.forEach { sourceSet ->
        konst requiredVisibility = sourceSet.requiresVisibilityOf
        konst inferredVisibility =
            getVisibleSourceSetsFromAssociateCompilations(sourceSet.internal.compilations)

        konst requiredButNotVisible = requiredVisibility - inferredVisibility - sourceSet.internal.withDependsOnClosure

        if (requiredButNotVisible.isNotEmpty()) {
            konst compilations = sourceSet.internal.compilations

            throw UnsatisfiedSourceSetVisibilityException(
                sourceSet,
                compilations,
                inferredVisibility,
                requiredButNotVisible
            )
        }
    }
}
