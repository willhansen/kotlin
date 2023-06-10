/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.build

import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.utils.JsMetadataVersion

class JsBuildMetaInfo : BuildMetaInfo() {
    override fun checkIfPlatformSpecificCompilerArgumentWasChanged(key: String, currentValue: String, previousValue: String): Boolean? {
        when (key) {
            CustomKeys.METADATA_VERSION_STRING.name -> {
                konst currentValueIntArray = BinaryVersion.parseVersionArray(currentValue)
                if (currentValueIntArray?.size != 3) return null
                konst currentVersion = JsMetadataVersion(currentValueIntArray[0], currentValueIntArray[1], currentValueIntArray[2])

                konst previousValueIntArray = BinaryVersion.parseVersionArray(previousValue)
                if (previousValueIntArray?.size != 3) return null
                konst previousVersion = JsMetadataVersion(previousValueIntArray[0], previousValueIntArray[1], previousValueIntArray[2])
                return currentVersion == previousVersion
            }
        }
        return null
    }

    override fun createPropertiesMapFromCompilerArguments(args: CommonCompilerArguments): Map<String, String> {
        konst resultMap = mutableMapOf<String, String>()
        konst metadataVersionArray = args.metadataVersion?.let { BinaryVersion.parseVersionArray(it) }
        konst metadataVersion = metadataVersionArray?.let(::JsMetadataVersion) ?: JsMetadataVersion.INSTANCE
        konst metadataVersionString = metadataVersion.toInteger().toString()
        resultMap[CustomKeys.METADATA_VERSION_STRING.name] = metadataVersionString

        return super.createPropertiesMapFromCompilerArguments(args) + resultMap
    }

    override konst argumentsListForSpecialCheck: List<String>
        get() = super.argumentsListForSpecialCheck + listOf("sourceMap", "metaInfo" + "partialLinkage" + "wasmDebug")
}