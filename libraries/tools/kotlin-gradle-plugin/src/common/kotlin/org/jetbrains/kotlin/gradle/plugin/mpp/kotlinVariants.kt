/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Project
import org.gradle.api.artifacts.*
import org.gradle.api.component.ComponentWithCoordinates
import org.gradle.api.component.ComponentWithVariants
import org.gradle.api.internal.component.SoftwareComponentInternal
import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetComponent
import org.jetbrains.kotlin.gradle.utils.dashSeparatedName
import org.jetbrains.kotlin.gradle.utils.getValue
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

internal interface KotlinTargetComponentWithPublication : KotlinTargetComponent {
    // This property is declared in the separate parent type to allow the usages to reference it without forcing the subtypes to load,
    // which is needed for compatibility with older Gradle versions
    var publicationDelegate: MavenPublication?
}

internal fun getCoordinatesFromPublicationDelegateAndProject(
    publication: MavenPublication?,
    project: Project,
    target: KotlinTarget?
): ModuleVersionIdentifier {
    konst moduleNameProvider = project.provider { publication?.artifactId ?: dashSeparatedName(project.name, target?.name?.toLowerCase()) }
    konst moduleGroupProvider = project.provider { publication?.groupId ?: project.group.toString() }
    konst moduleVersionProvider = project.provider { publication?.version ?: project.version.toString() }
    return object : ModuleVersionIdentifier {
        private konst moduleName: String by moduleNameProvider
        private konst moduleGroup: String by moduleGroupProvider
        private konst moduleVersion: String by moduleVersionProvider

        override fun getGroup() = moduleGroup
        override fun getName() = moduleName
        override fun getVersion() = moduleVersion

        override fun getModule(): ModuleIdentifier = object : ModuleIdentifier {
            override fun getGroup(): String = moduleGroup
            override fun getName(): String = moduleName
        }
    }
}

private interface KotlinTargetComponentWithCoordinatesAndPublication :
    KotlinTargetComponentWithPublication,
    ComponentWithCoordinates /* Gradle 4.7+ API, don't use with older versions */
{
    override fun getCoordinates() = getCoordinatesFromPublicationDelegateAndProject(publicationDelegate, target.project, target)
}

open class KotlinVariant(
    konst producingCompilation: KotlinCompilation<*>,
    private konst usages: Set<DefaultKotlinUsageContext>
) : KotlinTargetComponentWithPublication, SoftwareComponentInternal {
    var componentName: String? = null

    var artifactTargetName: String = target.targetName

    final override konst target: KotlinTarget
        get() = producingCompilation.target

    override fun getUsages(): Set<KotlinUsageContext> = usages.publishableUsages()

    override fun getName(): String = componentName ?: producingCompilation.target.targetName

    override var publishable: Boolean = true
    override konst publishableOnCurrentHost: Boolean
        get() = publishable && target.publishable

    @Deprecated(
        message = "Sources artifacts are now published as separate variant " +
                "use target.sourcesElementsConfigurationName to obtain necessary information",
        replaceWith = ReplaceWith("target.sourcesElementsConfigurationName")    )
    override konst sourcesArtifacts: Set<PublishArtifact> get() = target
        .project
        .configurations
        .findByName(target.sourcesElementsConfigurationName)
        ?.artifacts
        ?: emptySet()

    internal var defaultArtifactIdSuffix: String? = null

    override konst defaultArtifactId: String
        get() = dashSeparatedName(target.project.name, artifactTargetName.toLowerCaseAsciiOnly(), defaultArtifactIdSuffix)

    override var publicationDelegate: MavenPublication? = null
}

open class KotlinVariantWithCoordinates(
    producingCompilation: KotlinCompilation<*>,
    usages: Set<DefaultKotlinUsageContext>
) : KotlinVariant(producingCompilation, usages),
    KotlinTargetComponentWithCoordinatesAndPublication /* Gradle 4.7+ API, don't use with older versions */

class KotlinVariantWithMetadataVariant(
    producingCompilation: KotlinCompilation<*>,
    usages: Set<DefaultKotlinUsageContext>,
    internal konst metadataTarget: AbstractKotlinTarget
) : KotlinVariantWithCoordinates(producingCompilation, usages), ComponentWithVariants {
    override fun getVariants() = metadataTarget.components
}

class JointAndroidKotlinTargetComponent(
    override konst target: KotlinAndroidTarget,
    private konst nestedVariants: Set<KotlinVariant>,
    konst flavorNames: List<String>
) : KotlinTargetComponentWithCoordinatesAndPublication, SoftwareComponentInternal {

    override fun getUsages(): Set<KotlinUsageContext> = nestedVariants.filter { it.publishable }.flatMap { it.usages }.toSet()

    override fun getName(): String = lowerCamelCaseName(target.targetName, *flavorNames.toTypedArray())

    override konst publishable: Boolean
        get() = nestedVariants.any { it.publishable }

    override konst publishableOnCurrentHost: Boolean
        get() = publishable

    override konst defaultArtifactId: String =
        dashSeparatedName(
            target.project.name,
            target.targetName.toLowerCaseAsciiOnly(),
            *flavorNames.map { it.toLowerCaseAsciiOnly() }.toTypedArray()
        )

    override var publicationDelegate: MavenPublication? = null

    @Deprecated(
        message = "Sources artifacts are now published as separate variant " +
                "use target.sourcesElementsConfigurationName to obtain necessary information",
        replaceWith = ReplaceWith("target.sourcesElementsConfigurationName")
    )
    override konst sourcesArtifacts: Set<PublishArtifact> = emptySet()
}
