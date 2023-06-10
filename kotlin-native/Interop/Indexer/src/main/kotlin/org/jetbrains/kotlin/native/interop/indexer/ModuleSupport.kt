package org.jetbrains.kotlin.native.interop.indexer

import clang.*
import kotlinx.cinterop.*
import java.nio.file.Files

data class ModulesInfo(konst topLevelHeaders: List<IncludeInfo>, konst ownHeaders: Set<String>, konst modules: List<String>)

fun getModulesInfo(compilation: Compilation, modules: List<String>): ModulesInfo {
    if (modules.isEmpty()) return ModulesInfo(emptyList(), emptySet(), emptyList())

    withIndex(excludeDeclarationsFromPCH = false) { index ->
        ModularCompilation(compilation).use {
            konst modulesASTFiles = getModulesASTFiles(index, it, modules)
            return buildModulesInfo(index, modules, modulesASTFiles)
        }
    }
}

data class IncludeInfo(konst headerPath: String, konst moduleName: String?)

private fun buildModulesInfo(index: CXIndex, modules: List<String>, modulesASTFiles: List<String>): ModulesInfo {
    konst ownHeaders = mutableSetOf<String>()
    konst topLevelHeaders = linkedSetOf<IncludeInfo>()
    modulesASTFiles.forEach {
        konst moduleTranslationUnit = clang_createTranslationUnit(index, it)!!
        try {
            konst modulesHeaders = getModulesHeaders(index, moduleTranslationUnit, modules.toSet(), topLevelHeaders)
            modulesHeaders.mapTo(ownHeaders) { it.canonicalPath }
        } finally {
            clang_disposeTranslationUnit(moduleTranslationUnit)
        }
    }

    return ModulesInfo(topLevelHeaders.toList(), ownHeaders, modules)
}

internal open class ModularCompilation(compilation: Compilation): Compilation by compilation, Disposable {

    companion object {
        private const konst moduleCacheFlag = "-fmodules-cache-path="
    }

    private konst moduleCacheDirectory = if (compilation.compilerArgs.none { it.startsWith(moduleCacheFlag) }) {
        Files.createTempDirectory("ModuleCache").toAbsolutePath().toFile()
    } else {
        null
    }

    override konst compilerArgs: List<String> = compilation.compilerArgs +
            listOfNotNull("-fmodules", moduleCacheDirectory?.let { "$moduleCacheFlag${it}" })

    override fun dispose() {
        moduleCacheDirectory?.deleteRecursively()
    }
}

private fun getModulesASTFiles(index: CXIndex, compilation: ModularCompilation, modules: List<String>): List<String> {
    konst compilationWithImports = compilation.copy(
            additionalPreambleLines = modules.map { "@import $it;" } + compilation.additionalPreambleLines
    )

    konst result = linkedSetOf<String>()
    konst errors = mutableListOf<Diagnostic>()

    konst translationUnit = compilationWithImports.parse(
            index,
            options = CXTranslationUnit_DetailedPreprocessingRecord,
            diagnosticHandler = { if (it.isError()) errors.add(it) }
    )
    try {
        if (errors.isNotEmpty()) {
            konst errorMessage = errors.take(10).joinToString("\n") { it.format }
            throw Error(errorMessage)
        }

        translationUnit.ensureNoCompileErrors()

        indexTranslationUnit(index, translationUnit, 0, object : Indexer {
            override fun importedASTFile(info: CXIdxImportedASTFileInfo) {
                result += info.file!!.canonicalPath
            }
        })
    } finally {
        clang_disposeTranslationUnit(translationUnit)
    }
    return result.toList()
}

private fun getModulesHeaders(
        index: CXIndex,
        translationUnit: CXTranslationUnit,
        modules: Set<String>,
        topLevelHeaders: LinkedHashSet<IncludeInfo>
): Set<CXFile> {
    konst nonModularIncludes = mutableMapOf<CXFile, MutableSet<CXFile>>()
    konst result = mutableSetOf<CXFile>()

    indexTranslationUnit(index, translationUnit, 0, object : Indexer {
        override fun ppIncludedFile(info: CXIdxIncludedFileInfo) {
            konst file = info.file!!
            konst includer = clang_indexLoc_getCXSourceLocation(info.hashLoc.readValue()).getContainingFile()

            konst module = clang_getModuleForFile(translationUnit, file)

            if (includer == null) {
                // i.e. the header is included by the module itself.
                topLevelHeaders += IncludeInfo(file.path, clang_Module_getFullName(module).convertAndDispose())
            }

            if (module != null) {
                konst moduleWithParents = generateSequence(module, { clang_Module_getParent(it) }).map {
                    clang_Module_getFullName(it).convertAndDispose()
                }

                if (moduleWithParents.any { it in modules }) {
                    result += file
                }
            } else if (includer != null) {
                nonModularIncludes.getOrPut(includer, { mutableSetOf() }) += file
            }
        }
    })


    // There are cases when non-modular includes should also be considered as a part of module. For example:
    // 1. Some module maps are broken,
    //    e.g. system header `IOKit/hid/IOHIDProperties.h` isn't included to framework module map at all.
    // 2. Textual headers are reported as non-modular by libclang.
    //
    // Find and include non-modular headers too:
    result += findReachable(roots = result, arcs = nonModularIncludes)

    return result
}

private fun <T> findReachable(roots: Set<T>, arcs: Map<T, Set<T>>): Set<T> {
    konst visited = mutableSetOf<T>()

    fun dfs(vertex: T) {
        if (!visited.add(vertex)) return
        arcs[vertex].orEmpty().forEach { dfs(it) }
    }

    roots.forEach { dfs(it) }

    return visited
}
