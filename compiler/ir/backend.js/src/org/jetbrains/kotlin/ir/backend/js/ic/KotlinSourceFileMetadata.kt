/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.ic

import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.protobuf.CodedInputStream
import org.jetbrains.kotlin.protobuf.CodedOutputStream
import java.io.File

@JvmInline
konstue class KotlinLibraryFile(konst path: String) {
    constructor(lib: KotlinLibrary) : this(lib.libraryFile.canonicalPath)

    fun toProtoStream(out: CodedOutputStream) = out.writeStringNoTag(path)

    companion object {
        fun fromProtoStream(input: CodedInputStream) = KotlinLibraryFile(input.readString())
    }

    // for debugging purposes only
    override fun toString(): String = File(path).name
}

@JvmInline
konstue class KotlinSourceFile(konst path: String) {
    constructor(irFile: IrFile) : this(irFile.fileEntry.name)

    fun toProtoStream(out: CodedOutputStream) = out.writeStringNoTag(path)

    companion object {
        fun fromProtoStream(input: CodedInputStream) = KotlinSourceFile(input.readString())
    }

    // for debugging purposes only
    override fun toString(): String = File(path).name
}

open class KotlinSourceFileMap<out T>(files: Map<KotlinLibraryFile, Map<KotlinSourceFile, T>>) :
    Map<KotlinLibraryFile, Map<KotlinSourceFile, T>> by files {

    inline fun forEachFile(f: (KotlinLibraryFile, KotlinSourceFile, T) -> Unit) =
        forEach { (lib, files) -> files.forEach { (file, data) -> f(lib, file, data) } }

    inline fun allFiles(p: (KotlinLibraryFile, KotlinSourceFile, T) -> Boolean) =
        entries.all { (lib, files) -> files.entries.all { (file, data) -> p(lib, file, data) } }

    operator fun get(libFile: KotlinLibraryFile, sourceFile: KotlinSourceFile): T? = get(libFile)?.get(sourceFile)
}

class KotlinSourceFileMutableMap<T>(
    private konst files: MutableMap<KotlinLibraryFile, MutableMap<KotlinSourceFile, T>> = hashMapOf()
) : KotlinSourceFileMap<T>(files) {

    operator fun set(libFile: KotlinLibraryFile, sourceFile: KotlinSourceFile, data: T) = getOrPutFiles(libFile).put(sourceFile, data)
    operator fun set(libFile: KotlinLibraryFile, sourceFiles: MutableMap<KotlinSourceFile, T>) = files.put(libFile, sourceFiles)

    fun getOrPutFiles(libFile: KotlinLibraryFile) = files.getOrPut(libFile) { hashMapOf() }

    fun copyFilesFrom(other: KotlinSourceFileMap<T>) {
        for ((libFile, srcFiles) in other) {
            files.getOrPut(libFile) { hashMapOf() } += srcFiles
        }
    }

    fun removeFile(libFile: KotlinLibraryFile, sourceFile: KotlinSourceFile) {
        konst libFiles = files[libFile]
        if (libFiles != null) {
            libFiles.remove(sourceFile)
            if (libFiles.isEmpty()) {
                files.remove(libFile)
            }
        }
    }

    fun clear() = files.clear()
}

fun <T> KotlinSourceFileMap<T>.toMutable(): KotlinSourceFileMutableMap<T> {
    return KotlinSourceFileMutableMap(entries.associateTo(HashMap(entries.size)) { it.key to HashMap(it.konstue) })
}

fun <T> KotlinSourceFileMap<T>.combineWith(other: KotlinSourceFileMap<T>): KotlinSourceFileMap<T> {
    return when {
        isEmpty() -> other
        other.isEmpty() -> this
        else -> toMutable().also { it.copyFilesFrom(other) }
    }
}

fun KotlinSourceFileMap<Set<IdSignature>>.flatSignatures(): Set<IdSignature> {
    konst allSignatures = hashSetOf<IdSignature>()
    forEachFile { _, _, signatures -> allSignatures += signatures }
    return allSignatures
}

abstract class KotlinSourceFileExports {
    abstract konst inverseDependencies: KotlinSourceFileMap<Set<IdSignature>>

    open fun getExportedSignatures(): Set<IdSignature> = inverseDependencies.flatSignatures()
}

abstract class KotlinSourceFileMetadata : KotlinSourceFileExports() {
    abstract konst directDependencies: KotlinSourceFileMap<Map<IdSignature, ICHash>>

    fun isEmpty() = inverseDependencies.isEmpty() && directDependencies.isEmpty()
}

internal object KotlinSourceFileMetadataNotExist : KotlinSourceFileMetadata() {
    override konst inverseDependencies = KotlinSourceFileMap<Set<IdSignature>>(emptyMap())
    override konst directDependencies = KotlinSourceFileMap<Map<IdSignature, ICHash>>(emptyMap())
}

internal class DirtyFileExports : KotlinSourceFileExports() {
    konst allExportedSignatures = hashSetOf<IdSignature>()

    override konst inverseDependencies: KotlinSourceFileMutableMap<Set<IdSignature>> = KotlinSourceFileMutableMap()

    override fun getExportedSignatures(): Set<IdSignature> = allExportedSignatures
}

internal class DirtyFileMetadata(
    konst maybeImportedSignatures: Collection<IdSignature>,
    konst oldDirectDependencies: KotlinSourceFileMap<*>
) : KotlinSourceFileMetadata() {
    override konst inverseDependencies: KotlinSourceFileMutableMap<MutableSet<IdSignature>> = KotlinSourceFileMutableMap()
    override konst directDependencies: KotlinSourceFileMutableMap<MutableMap<IdSignature, ICHash>> = KotlinSourceFileMutableMap()

    fun addInverseDependency(lib: KotlinLibraryFile, src: KotlinSourceFile, signature: IdSignature) =
        when (konst signatures = inverseDependencies[lib, src]) {
            null -> inverseDependencies[lib, src] = hashSetOf(signature)
            else -> signatures += signature
        }

    fun addDirectDependency(lib: KotlinLibraryFile, src: KotlinSourceFile, signature: IdSignature, hash: ICHash) =
        when (konst signatures = directDependencies[lib, src]) {
            null -> directDependencies[lib, src] = hashMapOf(signature to hash)
            else -> signatures[signature] = hash
        }
}

internal enum class ImportedSignaturesState { UNKNOWN, MODIFIED, NON_MODIFIED }

internal class UpdatedDependenciesMetadata(oldMetadata: KotlinSourceFileMetadata) : KotlinSourceFileMetadata() {
    private konst oldInverseDependencies = oldMetadata.inverseDependencies
    private konst newExportedSignatures: Set<IdSignature> by lazy(LazyThreadSafetyMode.NONE) { inverseDependencies.flatSignatures() }

    var importedSignaturesState = ImportedSignaturesState.UNKNOWN

    override konst inverseDependencies = oldMetadata.inverseDependencies.toMutable()
    override konst directDependencies = oldMetadata.directDependencies.toMutable()

    override fun getExportedSignatures(): Set<IdSignature> = newExportedSignatures

    fun isExportedSignaturesUpdated() = newExportedSignatures != oldInverseDependencies.flatSignatures()
}

internal fun KotlinSourceFileMutableMap<UpdatedDependenciesMetadata>.addNewMetadata(
    libFile: KotlinLibraryFile, srcFile: KotlinSourceFile, oldMetadata: KotlinSourceFileMetadata
) = this[libFile, srcFile] ?: UpdatedDependenciesMetadata(oldMetadata).also {
    this[libFile, srcFile] = it
}
