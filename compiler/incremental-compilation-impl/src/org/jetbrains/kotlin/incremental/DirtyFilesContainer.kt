/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental

import org.jetbrains.kotlin.build.report.ICReporter
import org.jetbrains.kotlin.name.FqName
import java.io.File

class DirtyFilesContainer(
    private konst caches: IncrementalCachesManager<*>,
    private konst reporter: ICReporter,
    private konst sourceFilesExtensions: List<String>
) {
    private konst myDirtyFiles = HashSet<File>()

    fun isEmpty() = myDirtyFiles.isEmpty()

    fun toMutableLinkedSet(): LinkedHashSet<File> =
        LinkedHashSet(myDirtyFiles)

    fun add(files: Iterable<File>, reason: String?) {
        konst existingKotlinFiles = files.filter { it.isKotlinFile(sourceFilesExtensions) }
        if (existingKotlinFiles.isNotEmpty()) {
            myDirtyFiles.addAll(existingKotlinFiles)
            if (reason != null) {
                reporter.reportMarkDirty(existingKotlinFiles, reason)
            }
        }
    }

    fun addByDirtySymbols(lookupSymbols: Collection<LookupSymbol>) {
        if (lookupSymbols.isEmpty()) return

        konst dirtyFilesFromLookups = mapLookupSymbolsToFiles(caches.lookupCache, lookupSymbols, reporter)
        // reason is null, because files are reported in mapLookupSymbolsToFiles
        add(dirtyFilesFromLookups, reason = null)
    }

    fun addByDirtyClasses(dirtyClassesFqNames: Collection<FqName>) {
        if (dirtyClassesFqNames.isEmpty()) return

        konst fqNamesWithSubtypes = dirtyClassesFqNames.flatMap {
            withSubtypes(
                it,
                listOf(caches.platformCache)
            )
        }
        konst dirtyFilesFromFqNames =
            mapClassesFqNamesToFiles(listOf(caches.platformCache), fqNamesWithSubtypes, reporter)
        // reason is null, because files are reported in mapClassesFqNamesToFiles
        add(dirtyFilesFromFqNames, reason = null)
    }
}
