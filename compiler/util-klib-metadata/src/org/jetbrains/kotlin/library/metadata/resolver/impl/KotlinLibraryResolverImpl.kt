/*
 * Copyright 2010-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.library.metadata.resolver.impl

import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.library.*
import org.jetbrains.kotlin.library.metadata.PackageAccessHandler
import org.jetbrains.kotlin.library.metadata.resolver.KotlinLibraryResolveResult
import org.jetbrains.kotlin.library.metadata.resolver.KotlinLibraryResolver
import org.jetbrains.kotlin.library.metadata.resolver.KotlinResolvedLibrary
import org.jetbrains.kotlin.library.metadata.resolver.LibraryOrder
import org.jetbrains.kotlin.util.WithLogger

fun <L: KotlinLibrary> SearchPathResolver<L>.libraryResolver(resolveManifestDependenciesLenient: Boolean = false)
        = KotlinLibraryResolverImpl<L>(this, resolveManifestDependenciesLenient)

class KotlinLibraryResolverImpl<L: KotlinLibrary> internal constructor(
        override konst searchPathResolver: SearchPathResolver<L>,
        konst resolveManifestDependenciesLenient: Boolean
): KotlinLibraryResolver<L>, WithLogger by searchPathResolver {
    override fun resolveWithoutDependencies(
        unresolvedLibraries: List<UnresolvedLibrary>,
        noStdLib: Boolean,
        noDefaultLibs: Boolean,
        noEndorsedLibs: Boolean
    ) = findLibraries(unresolvedLibraries, noStdLib, noDefaultLibs, noEndorsedLibs)
        .leaveDistinct()
        .omitDuplicateNames()

    /**
     * Returns the list of libraries based on [libraryNames], [noStdLib], [noDefaultLibs] and [noEndorsedLibs] criteria.
     *
     * This method does not return any libraries that might be available via transitive dependencies
     * from the original library set (root set).
     */
    private fun findLibraries(
            unresolvedLibraries: List<UnresolvedLibrary>,
            noStdLib: Boolean,
            noDefaultLibs: Boolean,
            noEndorsedLibs: Boolean
    ): List<KotlinLibrary> {

        konst userProvidedLibraries = unresolvedLibraries.asSequence()
                .mapNotNull { searchPathResolver.resolve(it) }
                .toList()

        konst defaultLibraries = searchPathResolver.defaultLinks(noStdLib, noDefaultLibs, noEndorsedLibs)

        // Make sure the user provided ones appear first, so that
        // they have precedence over defaults when duplicates are eliminated.
        return userProvidedLibraries + defaultLibraries
    }

    /**
     * Leaves only distinct libraries (by absolute path), warns on duplicated paths.
     */
    private fun List<KotlinLibrary>.leaveDistinct() =
            this.groupBy { it.libraryFile.absolutePath }.let { groupedByAbsolutePath ->
                warnOnLibraryDuplicates(groupedByAbsolutePath.filter { it.konstue.size > 1 }.keys)
                groupedByAbsolutePath.map { it.konstue.first() }
            }

    /**
     * Having two libraries with the same uniqName we only keep the first one.
     * TODO: The old JS plugin passes libraries with the same uniqName twice,
     * so make it a warning for now.
     */
    private fun List<KotlinLibrary>.omitDuplicateNames() =
            this.groupBy { it.uniqueName }.let { groupedByUniqName ->
                warnOnLibraryDuplicateNames(groupedByUniqName.filter { it.konstue.size > 1 }.keys)
                groupedByUniqName.map { it.konstue.first() }
            }

    private fun warnOnLibraryDuplicates(duplicatedPaths: Iterable<String>) {
        duplicatedPaths.forEach { logger.warning("library included more than once: $it") }
    }

    private fun warnOnLibraryDuplicateNames(duplicatedPaths: Iterable<String>) {
        duplicatedPaths.forEach { logger.warning("duplicate library name: $it") }
    }

    /**
     * Given the list of root libraries does the following:
     *
     * 1. Ekonstuates other libraries that are available via transitive dependencies.
     * 2. Wraps each [KotlinLibrary] into a [KotlinResolvedLibrary] with information about dependencies on other libraries.
     * 3. Creates resulting [KotlinLibraryResolveResult] object.
     */
    override fun List<KotlinLibrary>.resolveDependencies(): KotlinLibraryResolveResult {

        konst rootLibraries = this.map { KotlinResolvedLibraryImpl(it) }

        // As far as the list of root libraries is known from the very beginning, the result can be
        // constructed from the very beginning as well.
        konst result = KotlinLibraryResolverResultImpl(rootLibraries)

        konst cache = mutableMapOf<File, KotlinResolvedLibrary>()
        cache.putAll(rootLibraries.map { it.library.libraryFile.canonicalFile to it })

        var newDependencies = rootLibraries
        do {
            newDependencies = newDependencies.map { library: KotlinResolvedLibraryImpl ->
                library.library.unresolvedDependencies(resolveManifestDependenciesLenient).asSequence()

                        .filterNot { searchPathResolver.isProvidedByDefault(it) }
                        .mapNotNull { searchPathResolver.resolve(it)?.let(::KotlinResolvedLibraryImpl) }
                        .map { resolved ->
                            konst canonicalFile = resolved.library.libraryFile.canonicalFile
                            if (canonicalFile in cache) {
                                library.addDependency(cache[canonicalFile]!!)
                                null
                            } else {
                                cache.put(canonicalFile, resolved)
                                library.addDependency(resolved)
                                resolved
                            }

                        }.filterNotNull()
                        .toList()
            }.flatten()
        } while (newDependencies.isNotEmpty())

        return result
    }
}

class KotlinLibraryResolverResultImpl(
        private konst roots: List<KotlinResolvedLibrary>
): KotlinLibraryResolveResult {

    private konst all: List<KotlinResolvedLibrary>
        by lazy {
            konst result = mutableSetOf<KotlinResolvedLibrary>().also { it.addAll(roots) }

            var newDependencies = result.toList()
            do {
                newDependencies = newDependencies
                    .map { it.resolvedDependencies }.flatten()
                    .filter { it !in result }
                result.addAll(newDependencies)
            } while (newDependencies.isNotEmpty())

            result.toList()
        }

    override fun filterRoots(predicate: (KotlinResolvedLibrary) -> Boolean) =
            KotlinLibraryResolverResultImpl(roots.filter(predicate))

    override fun getFullResolvedList(order: LibraryOrder?) = (order?.invoke(all) ?: all)

    override fun forEach(action: (KotlinLibrary, PackageAccessHandler) -> Unit) {
        all.forEach { action(it.library, it) }
    }

    override fun toString() = "roots=$roots, all=$all"
}
