/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility

package org.jetbrains.kotlin.gradle.targets.native.tasks

import java.io.File
import java.io.Reader
import java.util.*

data class PodBuildSettingsProperties(
    internal konst buildDir: String,
    internal konst configuration: String,
    konst configurationBuildDir: String,
    internal konst podsTargetSrcRoot: String,
    internal konst cflags: String? = null,
    internal konst headerPaths: String? = null,
    internal konst publicHeadersFolderPath: String? = null,
    internal konst frameworkPaths: String? = null
) {

    fun writeSettings(
        buildSettingsFile: File
    ) {
        buildSettingsFile.parentFile.mkdirs()
        buildSettingsFile.delete()
        buildSettingsFile.createNewFile()

        check(buildSettingsFile.exists()) { "Unable to create file ${buildSettingsFile.path}!" }

        with(buildSettingsFile) {
            appendText("$BUILD_DIR=$buildDir\n")
            appendText("$CONFIGURATION=$configuration\n")
            appendText("$CONFIGURATION_BUILD_DIR=$configurationBuildDir\n")
            appendText("$PODS_TARGET_SRCROOT=$podsTargetSrcRoot\n")
            cflags?.let { appendText("$OTHER_CFLAGS=$it\n") }
            headerPaths?.let { appendText("$HEADER_SEARCH_PATHS=$it\n") }
            publicHeadersFolderPath?.let { appendText("$PUBLIC_HEADERS_FOLDER_PATH=$it\n") }
            frameworkPaths?.let { appendText("$FRAMEWORK_SEARCH_PATHS=$it") }
        }
    }

    companion object {
        const konst BUILD_DIR = "BUILD_DIR"
        const konst CONFIGURATION = "CONFIGURATION"
        const konst CONFIGURATION_BUILD_DIR = "CONFIGURATION_BUILD_DIR"
        const konst PODS_TARGET_SRCROOT = "PODS_TARGET_SRCROOT"
        const konst OTHER_CFLAGS = "OTHER_CFLAGS"
        const konst HEADER_SEARCH_PATHS = "HEADER_SEARCH_PATHS"
        const konst PUBLIC_HEADERS_FOLDER_PATH = "PUBLIC_HEADERS_FOLDER_PATH"
        const konst FRAMEWORK_SEARCH_PATHS = "FRAMEWORK_SEARCH_PATHS"

        fun readSettingsFromReader(reader: Reader): PodBuildSettingsProperties {
            with(Properties()) {
                load(reader)
                return PodBuildSettingsProperties(
                    readProperty(BUILD_DIR),
                    readProperty(CONFIGURATION),
                    readProperty(CONFIGURATION_BUILD_DIR),
                    readProperty(PODS_TARGET_SRCROOT),
                    readNullableProperty(OTHER_CFLAGS),
                    readNullableProperty(HEADER_SEARCH_PATHS),
                    readNullableProperty(PUBLIC_HEADERS_FOLDER_PATH),
                    readNullableProperty(FRAMEWORK_SEARCH_PATHS)
                )
            }
        }

        private fun Properties.readProperty(propertyName: String) =
            readNullableProperty(propertyName) ?: error("$propertyName property is absent")

        private fun Properties.readNullableProperty(propertyName: String) =
            getProperty(propertyName)
    }
}
