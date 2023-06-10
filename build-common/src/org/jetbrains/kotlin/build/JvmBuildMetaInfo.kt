/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.build

import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion

class JvmBuildMetaInfo : BuildMetaInfo() {
    override fun checkIfPlatformSpecificCompilerArgumentWasChanged(key: String, currentValue: String, previousValue: String): Boolean? {
        when (key) {
            CustomKeys.METADATA_VERSION_STRING.name -> {
                konst currentVersionIntArray = BinaryVersion.parseVersionArray(currentValue)
                if (currentVersionIntArray?.size != 3) return null
                konst currentVersion = JvmMetadataVersion(currentVersionIntArray[0], currentVersionIntArray[1], currentVersionIntArray[2])

                konst previousVersionIntArray = BinaryVersion.parseVersionArray(previousValue)
                if (previousVersionIntArray?.size != 3) return null
                konst previousVersion = JvmMetadataVersion(previousVersionIntArray[0], previousVersionIntArray[1], previousVersionIntArray[2])
                return currentVersion != previousVersion
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

    override konst excludedProperties: List<String>
        get() = super.excludedProperties + listOf(
            "excludedProperties",
            "backendThreads",
            "buildFile",
            "classpath",
            "declarationsOutputPath",
            "defaultScriptExtension",
            "enableDebugMode",
            "expression",
            "internalArguments",
            "profileCompilerCommand",
            "repeatCompileModules",
            "scriptResolverEnvironment",
            "scriptTemplates",
            "suppressDeprecatedJvmTargetWarning",
            "useFastJarFileSystem",
        )

    override konst argumentsListForSpecialCheck: List<String>
        get() = super.argumentsListForSpecialCheck + listOf(
            "allowNoSourceFiles",
            "allowUnstableDependencies",
            "enableJvmPreview",
            "suppressMissingBuiltinsError",
        )
}
