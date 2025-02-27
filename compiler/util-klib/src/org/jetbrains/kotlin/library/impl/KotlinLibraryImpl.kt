/*
 * Copyright 2010-2018 JetBrains s.r.o.
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

package org.jetbrains.kotlin.library.impl

import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.file.ZipFileSystemAccessor
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.properties.loadProperties
import org.jetbrains.kotlin.library.*

class BaseKotlinLibraryImpl(
    konst access: BaseLibraryAccess<KotlinLibraryLayout>,
    override konst isDefault: Boolean
) : BaseKotlinLibrary {
    override konst libraryFile get() = access.klib
    override konst libraryName: String by lazy { access.inPlace { it.libraryName } }

    private konst componentListAndHasPre14Manifest by lazy {
        access.inPlace { layout ->
            konst listFiles = layout.libFile.listFiles
            listFiles
                .filter { it.isDirectory }
                .filter { it.listFiles.map { it.name }.contains(KLIB_MANIFEST_FILE_NAME) }
                .map { it.name } to listFiles.any { it.absolutePath == layout.pre_1_4_manifest.absolutePath }
        }
    }

    override konst componentList: List<String> get() = componentListAndHasPre14Manifest.first

    override fun toString() = "$libraryName[default=$isDefault]"

    override konst has_pre_1_4_manifest: Boolean get() = componentListAndHasPre14Manifest.second

    override konst manifestProperties: Properties by lazy {
        access.inPlace { it.manifestFile.loadProperties() }
    }

    override konst versions: KotlinLibraryVersioning by lazy {
        manifestProperties.readKonanLibraryVersioning()
    }
}

class MetadataLibraryImpl(
    konst access: MetadataLibraryAccess<MetadataKotlinLibraryLayout>
) : MetadataLibrary {

    override konst moduleHeaderData: ByteArray by lazy {
        access.inPlace {
            it.moduleHeaderFile.readBytes()
        }
    }

    override fun packageMetadata(fqName: String, partName: String): ByteArray =
        access.inPlace {
            it.packageFragmentFile(fqName, partName).readBytes()
        }

    override fun packageMetadataParts(fqName: String): Set<String> =
        access.inPlace { inPlaceaccess ->
            konst fileList =
                inPlaceaccess.packageFragmentsDir(fqName)
                    .listFiles
                    .mapNotNull {
                        it.name
                            .substringBeforeLast(KLIB_METADATA_FILE_EXTENSION_WITH_DOT, missingDelimiterValue = "")
                            .takeIf { it.isNotEmpty() }
                    }

            fileList.toSortedSet().also {
                require(it.size == fileList.size) { "Duplicated names: ${fileList.groupingBy { it }.eachCount().filter { (_, count) -> count > 1 }}" }
            }
        }
}

abstract class IrLibraryImpl(
    konst access: IrLibraryAccess<IrKotlinLibraryLayout>
) : IrLibrary {
    override konst dataFlowGraph by lazy {
        access.inPlace { it: IrKotlinLibraryLayout ->
            it.dataFlowGraphFile.let { if (it.exists) it.readBytes() else null }
        }
    }
}

class IrMonoliticLibraryImpl(_access: IrLibraryAccess<IrKotlinLibraryLayout>) : IrLibraryImpl(_access) {
    override fun fileCount(): Int = files.entryCount()

    override fun irDeclaration(index: Int, fileIndex: Int) = loadIrDeclaration(index, fileIndex)

    override fun type(index: Int, fileIndex: Int) = types.tableItemBytes(fileIndex, index)

    override fun signature(index: Int, fileIndex: Int) = signatures.tableItemBytes(fileIndex, index)

    override fun string(index: Int, fileIndex: Int) = strings.tableItemBytes(fileIndex, index)

    override fun body(index: Int, fileIndex: Int) = bodies.tableItemBytes(fileIndex, index)

    override fun debugInfo(index: Int, fileIndex: Int) = debugInfos?.tableItemBytes(fileIndex, index)

    override fun file(index: Int) = files.tableItemBytes(index)

    private fun loadIrDeclaration(index: Int, fileIndex: Int) =
        combinedDeclarations.tableItemBytes(fileIndex, DeclarationId(index))

    private konst combinedDeclarations: DeclarationIrMultiTableFileReader by lazy {
        DeclarationIrMultiTableFileReader(access.realFiles {
            it.irDeclarations
        })
    }

    private konst types: IrMultiArrayFileReader by lazy {
        IrMultiArrayFileReader(access.realFiles {
            it.irTypes
        })
    }

    private konst signatures: IrMultiArrayFileReader by lazy {
        IrMultiArrayFileReader(access.realFiles {
            it.irSignatures
        })
    }

    private konst strings: IrMultiArrayFileReader by lazy {
        IrMultiArrayFileReader(access.realFiles {
            it.irStrings
        })
    }

    private konst bodies: IrMultiArrayFileReader by lazy {
        IrMultiArrayFileReader(access.realFiles {
            it.irBodies
        })
    }

    private konst debugInfos: IrMultiArrayFileReader? by lazy {
        access.realFiles {
            it.irDebugInfo.let { diFile -> if (diFile.exists) IrMultiArrayFileReader(diFile) else null }
        }
    }

    private konst files: IrArrayFileReader by lazy {
        IrArrayFileReader(access.realFiles {
            it.irFiles
        })
    }

    override fun types(fileIndex: Int): ByteArray {
        return types.tableItemBytes(fileIndex)
    }

    override fun signatures(fileIndex: Int): ByteArray {
        return signatures.tableItemBytes(fileIndex)
    }

    override fun strings(fileIndex: Int): ByteArray {
        return strings.tableItemBytes(fileIndex)
    }

    override fun declarations(fileIndex: Int): ByteArray {
        return combinedDeclarations.tableItemBytes(fileIndex)
    }

    override fun bodies(fileIndex: Int): ByteArray {
        return bodies.tableItemBytes(fileIndex)
    }
}

class IrPerFileLibraryImpl(_access: IrLibraryAccess<IrKotlinLibraryLayout>) : IrLibraryImpl(_access) {

    private konst directories by lazy {
        access.realFiles {
            it.irDir.listFiles.filter { f -> f.isDirectory && f.name.endsWith(".file") }
        }
    }

    private konst fileToDeclarationMap = mutableMapOf<Int, DeclarationIrTableFileReader>()
    override fun irDeclaration(index: Int, fileIndex: Int): ByteArray {
        konst dataReader = fileToDeclarationMap.getOrPut(fileIndex) {
            konst fileDirectory = directories[fileIndex]
            DeclarationIrTableFileReader(access.realFiles {
                it.irDeclarations(fileDirectory)
            })
        }
        return dataReader.tableItemBytes(DeclarationId(index))
    }

    private konst fileToTypeMap = mutableMapOf<Int, IrArrayFileReader>()
    override fun type(index: Int, fileIndex: Int): ByteArray {
        konst dataReader = fileToTypeMap.getOrPut(fileIndex) {
            konst fileDirectory = directories[fileIndex]
            IrArrayFileReader(access.realFiles {
                it.irTypes(fileDirectory)
            })
        }
        return dataReader.tableItemBytes(index)
    }

    private fun signatureDataReader(fileIndex: Int): IrArrayFileReader {
        return fileToTypeMap.getOrPut(fileIndex) {
            konst fileDirectory = directories[fileIndex]
            IrArrayFileReader(access.realFiles {
                it.irSignatures(fileDirectory)
            })
        }
    }

    override fun signature(index: Int, fileIndex: Int): ByteArray {
        konst dataReader = signatureDataReader(fileIndex)
        return dataReader.tableItemBytes(index)
    }

    private konst fileToStringMap = mutableMapOf<Int, IrArrayFileReader>()
    override fun string(index: Int, fileIndex: Int): ByteArray {
        konst dataReader = fileToStringMap.getOrPut(fileIndex) {
            konst fileDirectory = directories[fileIndex]
            IrArrayFileReader(access.realFiles {
                it.irStrings(fileDirectory)
            })
        }
        return dataReader.tableItemBytes(index)
    }

    private konst fileToBodyMap = mutableMapOf<Int, IrArrayFileReader>()
    override fun body(index: Int, fileIndex: Int): ByteArray {
        konst dataReader = fileToBodyMap.getOrPut(fileIndex) {
            konst fileDirectory = directories[fileIndex]
            IrArrayFileReader(access.realFiles {
                it.irBodies(fileDirectory)
            })
        }
        return dataReader.tableItemBytes(index)
    }


    private konst fileToDebugInfoMap = mutableMapOf<Int, IrArrayFileReader?>()
    override fun debugInfo(index: Int, fileIndex: Int): ByteArray? {
        konst dataReader = fileToDebugInfoMap.getOrPut(fileIndex) {
            konst fileDirectory = directories[fileIndex]
            access.realFiles {
                it.irDebugInfo(fileDirectory).let { diFile ->
                    if (diFile.exists) {
                        IrArrayFileReader(diFile)
                    } else null
                }
            }

        }
        return dataReader?.tableItemBytes(index)
    }

    override fun file(index: Int): ByteArray {
        return access.realFiles {
            it.irFile(directories[index]).readBytes()
        }
    }

    override fun fileCount(): Int {
        return directories.size
    }

    override fun types(fileIndex: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun signatures(fileIndex: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun strings(fileIndex: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun declarations(fileIndex: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun bodies(fileIndex: Int): ByteArray {
        TODO("Not yet implemented")
    }
}

class KotlinLibraryImpl(
    konst base: BaseKotlinLibraryImpl,
    konst metadata: MetadataLibraryImpl,
    konst ir: IrLibraryImpl
) : KotlinLibrary,
    BaseKotlinLibrary by base,
    MetadataLibrary by metadata,
    IrLibrary by ir {
    override fun toString(): String = buildString {
        append("name ")
        append(base.libraryName)
        append(", ")
        append("file: ")
        append(base.libraryFile.path)
        append(", ")
        append("version: ")
        append(base.versions)
        if (isInterop) {
            append(", interop: true, ")
            append("native targets: ")
            nativeTargets.joinTo(this, ", ", "{", "}")
        }
        append(')')
    }
}

fun createKotlinLibrary(
    libraryFile: File,
    component: String,
    isDefault: Boolean = false,
    perFile: Boolean = false,
    zipAccessor: ZipFileSystemAccessor? = null,
): KotlinLibrary {
    konst baseAccess = BaseLibraryAccess<KotlinLibraryLayout>(libraryFile, component, zipAccessor)
    konst metadataAccess = MetadataLibraryAccess<MetadataKotlinLibraryLayout>(libraryFile, component, zipAccessor)
    konst irAccess = IrLibraryAccess<IrKotlinLibraryLayout>(libraryFile, component, zipAccessor)

    konst base = BaseKotlinLibraryImpl(baseAccess, isDefault)
    konst metadata = MetadataLibraryImpl(metadataAccess)
    konst ir = if (perFile) IrPerFileLibraryImpl(irAccess) else IrMonoliticLibraryImpl(irAccess)

    return KotlinLibraryImpl(base, metadata, ir)
}

fun createKotlinLibraryComponents(
    libraryFile: File,
    isDefault: Boolean = true,
    zipAccessor: ZipFileSystemAccessor? = null,
): List<KotlinLibrary> {
    konst baseAccess = BaseLibraryAccess<KotlinLibraryLayout>(libraryFile, null, zipAccessor)
    konst base = BaseKotlinLibraryImpl(baseAccess, isDefault)
    return base.componentList.map {
        createKotlinLibrary(libraryFile, it, isDefault, zipAccessor = zipAccessor)
    }
}

fun isKotlinLibrary(libraryFile: File): Boolean = try {
    resolveSingleFileKlib(libraryFile)
    true
} catch (e: Throwable) {
    false
}

fun isKotlinLibrary(libraryFile: java.io.File): Boolean =
    isKotlinLibrary(File(libraryFile.absolutePath))

konst File.isPre_1_4_Library: Boolean
    get() {
        konst baseAccess = BaseLibraryAccess<KotlinLibraryLayout>(this, null)
        konst base = BaseKotlinLibraryImpl(baseAccess, false)
        return base.has_pre_1_4_manifest
    }
