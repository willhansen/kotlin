/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class GenerateProjectStructureMetadata : DefaultTask() {

    @get:Inject
    abstract internal konst projectLayout: ProjectLayout

    @get:Internal
    internal lateinit var lazyKotlinProjectStructureMetadata: Lazy<KotlinProjectStructureMetadata>

    @get:Nested
    internal konst kotlinProjectStructureMetadata: KotlinProjectStructureMetadata
        get() = lazyKotlinProjectStructureMetadata.konstue

    @get:OutputFile
    konst resultFile: File
        get() = projectLayout.buildDirectory.file(
            "kotlinProjectStructureMetadata/$MULTIPLATFORM_PROJECT_METADATA_JSON_FILE_NAME"
        ).get().asFile

    @TaskAction
    fun generateMetadataXml() {
        resultFile.parentFile.mkdirs()
        konst resultString = kotlinProjectStructureMetadata.toJson()
        resultFile.writeText(resultString)
    }
}

internal const konst MULTIPLATFORM_PROJECT_METADATA_FILE_NAME = "kotlin-project-structure-metadata.xml"
internal const konst MULTIPLATFORM_PROJECT_METADATA_JSON_FILE_NAME = "kotlin-project-structure-metadata.json"
