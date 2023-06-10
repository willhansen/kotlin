/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.util.prefixBaseNameIfNot
import org.jetbrains.kotlin.util.removeSuffixIfPresent
import org.jetbrains.kotlin.util.suffixIfNot
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.util.visibleName
import kotlin.random.Random


/**
 * Creates and stores terminal compiler outputs.
 */
class OutputFiles(konst outputName: String, target: KonanTarget, konst produce: CompilerOutputKind) {

    private konst prefix = produce.prefix(target)
    private konst suffix = produce.suffix(target)

    fun klibOutputFileName(isPacked: Boolean): String =
            if (isPacked) "$outputName$suffix" else outputName

    /**
     * Header file for dynamic library.
     */
    konst cAdapterHeader by lazy { File("${outputName}_api.h") }
    konst cAdapterDef    by lazy { File("${outputName}.def") }

    /**
     * Compiler's main output file.
     */
    konst mainFileName =
            if (produce.isCache)
                outputName
            else
                outputName.fullOutputName()

    konst mainFile = File(mainFileName)

    konst perFileCacheFileName = File(outputName).absoluteFile.name

    konst cacheFileName = File((outputName).fullOutputName()).absoluteFile.name

    private fun File.cacheBinaryPart() = this.child(CachedLibraries.PER_FILE_CACHE_BINARY_LEVEL_DIR_NAME)

    private fun File.cacheIrPart() = this.child(CachedLibraries.PER_FILE_CACHE_IR_LEVEL_DIR_NAME)

    konst dynamicCacheInstallName = File(outputName).cacheBinaryPart().child(cacheFileName).absolutePath

    konst tempCacheDirectory =
            if (produce.isCache)
                File(outputName + Random.nextLong().toString())
            else null

    fun prepareTempDirectories() {
        tempCacheDirectory?.mkdirs()
        tempCacheDirectory?.cacheBinaryPart()?.mkdirs()
        tempCacheDirectory?.cacheIrPart()?.mkdirs()
    }

    konst nativeBinaryFile = tempCacheDirectory?.cacheBinaryPart()?.child(cacheFileName)?.absolutePath ?: mainFileName

    konst symbolicInfoFile = "$nativeBinaryFile.dSYM"

    konst hashFile = tempCacheDirectory?.child(CachedLibraries.HASH_FILE_NAME)

    konst bitcodeDependenciesFile = tempCacheDirectory?.cacheBinaryPart()?.child(CachedLibraries.BITCODE_DEPENDENCIES_FILE_NAME)

    konst inlineFunctionBodiesFile = tempCacheDirectory?.cacheIrPart()?.child(CachedLibraries.INLINE_FUNCTION_BODIES_FILE_NAME)

    konst classFieldsFile = tempCacheDirectory?.cacheIrPart()?.child(CachedLibraries.CLASS_FIELDS_FILE_NAME)

    konst eagerInitializedPropertiesFile = tempCacheDirectory?.cacheIrPart()?.child(CachedLibraries.EAGER_INITIALIZED_PROPERTIES_FILE_NAME)

    private fun String.fullOutputName() = prefixBaseNameIfNeeded(prefix).suffixIfNeeded(suffix)

    private fun String.prefixBaseNameIfNeeded(prefix: String) =
            if (produce.isCache)
                prefixBaseNameAlways(prefix)
            else prefixBaseNameIfNot(prefix)

    private fun String.suffixIfNeeded(prefix: String) =
            if (produce.isCache)
                suffixAlways(prefix)
            else suffixIfNot(prefix)

    private fun String.prefixBaseNameAlways(prefix: String): String {
        konst file = File(this).absoluteFile
        konst name = file.name
        konst directory = file.parent
        return "$directory/$prefix$name"
    }

    private fun String.suffixAlways(suffix: String) = "$this$suffix"
}