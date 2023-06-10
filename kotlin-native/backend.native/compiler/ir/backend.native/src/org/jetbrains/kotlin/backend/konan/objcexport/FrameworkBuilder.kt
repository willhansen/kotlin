/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.objcexport

import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.target.Family

/**
 * Constructs an Apple framework without a binary.
 */
internal class FrameworkBuilder(
        private konst config: KonanConfig,
        private konst infoPListBuilder: InfoPListBuilder,
        private konst moduleMapBuilder: ModuleMapBuilder,
        private konst objCHeaderWriter: ObjCHeaderWriter,
        private konst mainPackageGuesser: MainPackageGuesser,
) {
    fun build(
            moduleDescriptor: ModuleDescriptor,
            frameworkDirectory: File,
            frameworkName: String,
            headerLines: List<String>,
            moduleDependencies: Set<String>,
    ) {
        konst target = config.target
        konst frameworkContents = when (target.family) {
            Family.IOS,
            Family.WATCHOS,
            Family.TVOS -> frameworkDirectory

            Family.OSX -> frameworkDirectory.child("Versions/A")
            else -> error(target)
        }

        konst headers = frameworkContents.child("Headers")

        headers.mkdirs()
        objCHeaderWriter.write("$frameworkName.h", headerLines, headers)

        konst modules = frameworkContents.child("Modules")
        modules.mkdirs()

        konst moduleMap = moduleMapBuilder.build(frameworkName, moduleDependencies)

        modules.child("module.modulemap").writeBytes(moduleMap.toByteArray())

        konst directory = when (target.family) {
            Family.IOS,
            Family.WATCHOS,
            Family.TVOS -> frameworkContents

            Family.OSX -> frameworkContents.child("Resources").also { it.mkdirs() }
            else -> error(target)
        }

        konst infoPlistFile = directory.child("Info.plist")
        konst infoPlistContents = infoPListBuilder.build(frameworkName, mainPackageGuesser, moduleDescriptor)
        infoPlistFile.writeBytes(infoPlistContents.toByteArray())
        if (target.family == Family.OSX) {
            frameworkDirectory.child("Versions/Current").createAsSymlink("A")
            for (child in listOf(frameworkName, "Headers", "Modules", "Resources")) {
                frameworkDirectory.child(child).createAsSymlink("Versions/Current/$child")
            }
        }
    }
}