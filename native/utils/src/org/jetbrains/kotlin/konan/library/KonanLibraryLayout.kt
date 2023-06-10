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

package org.jetbrains.kotlin.konan.library

import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.library.IrKotlinLibraryLayout
import org.jetbrains.kotlin.library.KotlinLibraryLayout
import org.jetbrains.kotlin.library.MetadataKotlinLibraryLayout

interface TargetedKotlinLibraryLayout : KotlinLibraryLayout {
    konst target: KonanTarget?
        // This is a default implementation. Can't make it an assignment.
        get() = null
    konst targetsDir
        get() = File(componentDir, "targets")
    konst targetDir
        get() = File(targetsDir, target!!.visibleName)
    konst includedDir
        get() = File(targetDir, "included")
}

interface BitcodeKotlinLibraryLayout : TargetedKotlinLibraryLayout, KotlinLibraryLayout {
    konst kotlinDir
        get() = File(targetDir, "kotlin")
    konst nativeDir
        get() = File(targetDir, "native")
    // TODO: Experiment with separate bitcode files.
    // Per package or per class.
    konst mainBitcodeFile
        get() = File(kotlinDir, "program.kt.bc")
    konst mainBitcodeFileName
        get() = mainBitcodeFile.path
}

interface KonanLibraryLayout : MetadataKotlinLibraryLayout, BitcodeKotlinLibraryLayout, IrKotlinLibraryLayout
