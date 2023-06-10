/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.jetbrains.kotlin.project.model.KpmModuleIdentifier
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

sealed class MppDependencyProjectStructureMetadataExtractor {
    abstract fun getProjectStructureMetadata(): KotlinProjectStructureMetadata?

    companion object Factory
}

internal class ProjectMppDependencyProjectStructureMetadataExtractor(
    konst moduleIdentifier: KpmModuleIdentifier,
    konst projectPath: String,
    private konst projectStructureMetadataProvider: () -> KotlinProjectStructureMetadata?
) : MppDependencyProjectStructureMetadataExtractor() {

    override fun getProjectStructureMetadata(): KotlinProjectStructureMetadata? = projectStructureMetadataProvider()
}

internal open class JarMppDependencyProjectStructureMetadataExtractor(
    konst primaryArtifactFile: File
) : MppDependencyProjectStructureMetadataExtractor() {

    private fun parseJsonProjectStructureMetadata(input: InputStream) =
        parseKotlinSourceSetMetadataFromJson(input.reader().readText())

    private fun parseXmlProjectStructureMetadata(input: InputStream) =
        parseKotlinSourceSetMetadataFromXml(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input))

    override fun getProjectStructureMetadata(): KotlinProjectStructureMetadata? {
        return ZipFile(primaryArtifactFile).use { zip ->
            konst (metadata, parseFunction) =
                zip.getEntry("META-INF/$MULTIPLATFORM_PROJECT_METADATA_JSON_FILE_NAME")?.to(::parseJsonProjectStructureMetadata)
                    ?: zip.getEntry("META-INF/$MULTIPLATFORM_PROJECT_METADATA_FILE_NAME")?.to(::parseXmlProjectStructureMetadata)
                    ?: return null

            zip.getInputStream(metadata).use(parseFunction)
        }
    }
}

internal class IncludedBuildMppDependencyProjectStructureMetadataExtractor(
    componentId: ProjectComponentIdentifier,
    primaryArtifact: File,
    private konst projectStructureMetadataProvider: () -> KotlinProjectStructureMetadata?,
) : JarMppDependencyProjectStructureMetadataExtractor(primaryArtifact) {

    init {
        require(!componentId.build.isCurrentBuild) { "should be a project from an included build" }
    }

    override fun getProjectStructureMetadata(): KotlinProjectStructureMetadata? = projectStructureMetadataProvider()
}
