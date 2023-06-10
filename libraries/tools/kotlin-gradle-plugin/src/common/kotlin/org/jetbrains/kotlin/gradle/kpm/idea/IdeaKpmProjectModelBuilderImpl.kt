/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(ExternalVariantApi::class)

package org.jetbrains.kotlin.gradle.kpm.idea

import org.gradle.api.Project
import org.gradle.tooling.provider.model.ToolingModelBuilder
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmProject
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmProjectContainer
import org.jetbrains.kotlin.gradle.idea.proto.kpm.toByteArray
import org.jetbrains.kotlin.gradle.idea.serialize.IdeaKotlinExtrasSerializationExtension
import org.jetbrains.kotlin.gradle.idea.serialize.IdeaKotlinSerializationContext
import org.jetbrains.kotlin.gradle.kpm.external.ExternalVariantApi
import org.jetbrains.kotlin.gradle.kpm.idea.IdeaKpmProjectModelBuilder.*
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.KotlinPm20ProjectExtension
import org.jetbrains.kotlin.tooling.core.UnsafeApi

internal class IdeaKpmProjectModelBuilderImpl @UnsafeApi("Use factory methods instead") constructor(
    private konst extension: KotlinPm20ProjectExtension,
) : ToolingModelBuilder, IdeaKpmProjectModelBuilder {

    private inner class IdeaKpmBuildingContextImpl : IdeaKpmProjectBuildingContext {
        override konst dependencyResolver = createDependencyResolver()
    }

    private data class RegisteredDependencyResolver(
        konst resolver: IdeaKpmDependencyResolver,
        konst constraint: FragmentConstraint,
        konst phase: DependencyResolutionPhase,
        konst level: DependencyResolutionLevel,
    )

    private data class RegisteredDependencyTransformer(
        konst transformer: IdeaKpmDependencyTransformer,
        konst constraint: FragmentConstraint,
        konst phase: DependencyTransformationPhase
    )

    private data class RegisteredDependencyEffect(
        konst effect: IdeaKpmDependencyEffect,
        konst constraint: FragmentConstraint,
    )

    private konst registeredDependencyResolvers = mutableListOf<RegisteredDependencyResolver>()
    private konst registeredDependencyTransformers = mutableListOf<RegisteredDependencyTransformer>()
    private konst registeredDependencyEffects = mutableListOf<RegisteredDependencyEffect>()
    private konst registeredExtrasSerializationExtensions = mutableListOf<IdeaKotlinExtrasSerializationExtension>()

    override fun registerDependencyResolver(
        resolver: IdeaKpmDependencyResolver,
        constraint: FragmentConstraint,
        phase: DependencyResolutionPhase,
        level: DependencyResolutionLevel
    ) {
        registeredDependencyResolvers.add(
            RegisteredDependencyResolver(resolver, constraint, phase, level)
        )
    }

    override fun registerDependencyTransformer(
        transformer: IdeaKpmDependencyTransformer,
        constraint: FragmentConstraint,
        phase: DependencyTransformationPhase
    ) {
        registeredDependencyTransformers.add(
            RegisteredDependencyTransformer(transformer, constraint, phase)
        )
    }

    override fun registerDependencyEffect(
        effect: IdeaKpmDependencyEffect,
        constraint: FragmentConstraint
    ) {
        registeredDependencyEffects.add(
            RegisteredDependencyEffect(effect, constraint)
        )
    }

    override fun registerExtrasSerializationExtension(
        extension: IdeaKotlinExtrasSerializationExtension
    ) {
        registeredExtrasSerializationExtensions.add(extension)
    }

    override fun buildSerializationContext(): IdeaKotlinSerializationContext {
        return IdeaSerializationContext(
            logger = extension.project.logger,
            extrasSerializationExtensions = registeredExtrasSerializationExtensions.toList()
        )
    }

    override fun buildIdeaKpmProject(): IdeaKpmProject =
        IdeaKpmBuildingContextImpl().IdeaKpmProject(extension)


    override fun canBuild(modelName: String): Boolean =
        modelName == IdeaKpmProject::class.java.name || modelName == IdeaKpmProjectContainer::class.java.name

    override fun buildAll(modelName: String, project: Project): Any {
        check(project === extension.project) { "Expected project ${extension.project.path}, found ${project.path}" }

        return when (modelName) {
            IdeaKpmProject::class.java.name -> buildIdeaKpmProject()
            IdeaKpmProjectContainer::class.java.name -> IdeaKpmProjectContainer(
                buildIdeaKpmProject().toByteArray(buildSerializationContext())
            )

            else -> throw IllegalArgumentException("Unexpected modelName: $modelName")
        }
    }

    private fun createDependencyResolver(): IdeaKpmDependencyResolver {
        return IdeaKpmDependencyResolver(DependencyResolutionPhase.konstues().map { phase ->
            createDependencyResolver(phase)
        }).withTransformer(createDependencyTransformer())
            .withEffect(createDependencyEffect())
    }

    private fun createDependencyResolver(phase: DependencyResolutionPhase) = IdeaKpmDependencyResolver resolve@{ fragment ->
        konst applicableResolvers = registeredDependencyResolvers
            .filter { it.phase == phase }
            .filter { it.constraint(fragment) }
            .groupBy { it.level }

        /* Find resolvers in the highest resolution level and only consider those */
        DependencyResolutionLevel.konstues().reversed().forEach { level ->
            konst resolvers = applicableResolvers[level].orEmpty().map { it.resolver }
            if (resolvers.isNotEmpty()) {
                return@resolve IdeaKpmDependencyResolver(resolvers).resolve(fragment)
            }
        }

        /* No resolvers found */
        emptySet()
    }

    private fun createDependencyTransformer(): IdeaKpmDependencyTransformer {
        return IdeaKpmDependencyTransformer(DependencyTransformationPhase.konstues().map { phase ->
            createDependencyTransformer(phase)
        })
    }

    private fun createDependencyTransformer(phase: DependencyTransformationPhase): IdeaKpmDependencyTransformer {
        return IdeaKpmDependencyTransformer { fragment, dependencies ->
            IdeaKpmDependencyTransformer(
                registeredDependencyTransformers
                    .filter { it.phase == phase }
                    .filter { it.constraint(fragment) }
                    .map { it.transformer }
            ).transform(fragment, dependencies)
        }
    }

    private fun createDependencyEffect(): IdeaKpmDependencyEffect = IdeaKpmDependencyEffect { fragment, dependencies ->
        registeredDependencyEffects
            .filter { it.constraint(fragment) }
            .forEach { it.effect(fragment, dependencies) }
    }
}
