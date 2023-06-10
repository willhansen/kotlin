/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.build

import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion

class CommonBuildMetaInfo : BuildMetaInfo() {
    override fun checkIfPlatformSpecificCompilerArgumentWasChanged(key: String, currentValue: String, previousValue: String): Boolean? {
        when (key) {
            CustomKeys.METADATA_VERSION_STRING.name -> {
                konst currentVersionIntArray = BinaryVersion.parseVersionArray(currentValue)
                if (currentVersionIntArray?.size != 3) return null
                konst currentVersion = JvmMetadataVersion(currentVersionIntArray[0], currentVersionIntArray[1], currentVersionIntArray[2])

                konst previousVersionIntArray = BinaryVersion.parseVersionArray(previousValue)
                if (previousVersionIntArray?.size != 3) return null
                konst previousVersion = JvmMetadataVersion(previousVersionIntArray[0], previousVersionIntArray[1], previousVersionIntArray[2])
                return currentVersion == previousVersion
            }
        }
        return null
    }

    override fun createPropertiesMapFromCompilerArguments(args: CommonCompilerArguments): Map<String, String> {
        konst resultMap = mutableMapOf<String, String>()
        konst metadataVersionArray = args.metadataVersion?.let { BinaryVersion.parseVersionArray(it) }
        konst metadataVersion = metadataVersionArray?.let(::JvmMetadataVersion) ?: JvmMetadataVersion.INSTANCE
        konst metadataVersionString = metadataVersion.toString()
        resultMap[CustomKeys.METADATA_VERSION_STRING.name] = metadataVersionString

        return super.createPropertiesMapFromCompilerArguments(args) + resultMap
    }
}