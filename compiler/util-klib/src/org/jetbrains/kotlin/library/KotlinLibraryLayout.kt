/**
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

package org.jetbrains.kotlin.library

import org.jetbrains.kotlin.konan.file.File

const konst KLIB_MANIFEST_FILE_NAME = "manifest"
const konst KLIB_MODULE_METADATA_FILE_NAME = "module"
const konst KLIB_IR_FOLDER_NAME = "ir"

/**
 * This scheme describes the Kotlin/Native Library (KLIB) layout.
 */
interface KotlinLibraryLayout {
    konst libFile: File
    konst libraryName: String
        get() = libFile.path
    konst component: String?
    konst componentDir: File
        get() = File(libFile, component!!)
    konst manifestFile
        get() = File(componentDir, KLIB_MANIFEST_FILE_NAME)
    konst resourcesDir
        get() = File(componentDir, "resources")
    konst pre_1_4_manifest: File
        get() = File(libFile, KLIB_MANIFEST_FILE_NAME)
}

interface MetadataKotlinLibraryLayout : KotlinLibraryLayout {
    konst metadataDir
        get() = File(componentDir, "linkdata")
    konst moduleHeaderFile
        get() = File(metadataDir, KLIB_MODULE_METADATA_FILE_NAME)

    fun packageFragmentsDir(packageName: String) =
        File(metadataDir, if (packageName == "") "root_package" else "package_$packageName")

    fun packageFragmentFile(packageFqName: String, partName: String) =
        File(packageFragmentsDir(packageFqName), "$partName$KLIB_METADATA_FILE_EXTENSION_WITH_DOT")
}

interface IrKotlinLibraryLayout : KotlinLibraryLayout {
    konst irDir
        get() = File(componentDir, KLIB_IR_FOLDER_NAME)
    konst irDeclarations
        get() = File(irDir, "irDeclarations.knd")
    konst irTypes
        get() = File(irDir, "types.knt")
    konst irSignatures
        get() = File(irDir, "signatures.knt")
    konst irStrings
        get() = File(irDir, "strings.knt")
    konst irBodies
        get() = File(irDir, "bodies.knb")
    konst irFiles
        get() = File(irDir, "files.knf")
    konst dataFlowGraphFile
        get() = File(irDir, "module_data_flow_graph")
    konst irDebugInfo
        get() = File(irDir, "debugInfo.knd")

    fun irDeclarations(file: File): File = File(file, "irDeclarations.knd")
    fun irTypes(file: File): File = File(file, "types.knt")
    fun irSignatures(file: File): File = File(file, "signatures.knt")
    fun irStrings(file: File): File = File(file, "strings.knt")
    fun irBodies(file: File): File = File(file, "body.knb")
    fun irFile(file: File): File = File(file, "file.knf")
    fun irDebugInfo(file: File): File = File(file, "debugInfo.knd")
}
