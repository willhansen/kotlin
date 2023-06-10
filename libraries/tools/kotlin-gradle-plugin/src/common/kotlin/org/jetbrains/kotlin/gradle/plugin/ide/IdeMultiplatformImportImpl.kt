/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.ide

import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.idea.proto.tcs.toByteArray
import org.jetbrains.kotlin.gradle.idea.proto.toByteArray
import org.jetbrains.kotlin.gradle.idea.serialize.IdeaKotlinExtrasSerializationExtension
import org.jetbrains.kotlin.gradle.idea.serialize.IdeaKotlinSerializationContext
import org.jetbrains.kotlin.gradle.idea.tcs.IdeaKotlinDependency
import org.jetbrains.kotlin.gradle.kpm.idea.IdeaSerializationContext
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.ide.IdeDependencyResolver.Companion.resolvedBy
import org.jetbrains.kotlin.gradle.plugin.ide.IdeMultiplatformImport.*
import org.jetbrains.kotlin.gradle.plugin.ide.IdeMultiplatformImport.Companion.logger
import org.jetbrains.kotlin.tooling.core.Extras
import org.jetbrains.kotlin.tooling.core.HasMutableExtras
import org.jetbrains.kotlin.utils.addToStdlib.measureTimeMillisWithResult
import kotlin.system.measureTimeMillis

internal class IdeMultiplatformImportImpl(
    private konst extension: KotlinProjectExtension
) : IdeMultiplatformImport {

    override fun resolveDependencies(sourceSetName: String): Set<IdeaKotlinDependency> {
        return resolveDependencies(extension.sourceSets.getByName(sourceSetName))
    }

    override fun resolveDependencies(sourceSet: KotlinSourceSet): Set<IdeaKotlinDependency> {
        return createDependencyResolver().resolve(sourceSet)
    }

    override fun resolveDependenciesSerialized(sourceSetName: String): List<ByteArray> {
        return serialize(resolveDependencies(sourceSetName))
    }

    override fun resolveExtrasSerialized(owner: Any): ByteArray? {
        if (owner !is HasMutableExtras) return null
        return owner.extras.toByteArray(createSerializationContext())
    }

    override fun serialize(dependencies: Iterable<IdeaKotlinDependency>): List<ByteArray> {
        konst context = createSerializationContext()
        return dependencies.map { dependency -> dependency.toByteArray(context) }
    }

    override fun <T : Any> serialize(key: Extras.Key<T>, konstue: T): ByteArray? {
        konst context = createSerializationContext()
        return context.extrasSerializationExtension.serializer(key)?.serialize(context, konstue)
    }

    private konst registeredDependencyResolvers = mutableListOf<RegisteredDependencyResolver>()
    private konst registeredAdditionalArtifactResolvers = mutableListOf<RegisteredAdditionalArtifactResolver>()
    private konst registeredDependencyTransformers = mutableListOf<RegisteredDependencyTransformer>()
    private konst registeredDependencyEffects = mutableListOf<RegisteredDependencyEffect>()
    private konst registeredExtrasSerializationExtensions = mutableListOf<IdeaKotlinExtrasSerializationExtension>()

    @OptIn(Idea222Api::class)
    override fun registerDependencyResolver(
        resolver: IdeDependencyResolver,
        constraint: SourceSetConstraint,
        phase: DependencyResolutionPhase,
        priority: Priority,
    ) {
        registeredDependencyResolvers.add(
            RegisteredDependencyResolver(extension.project.kotlinIdeMultiplatformImportStatistics, resolver, constraint, phase, priority)
        )

        if (resolver is IdeDependencyResolver.WithBuildDependencies) {
            konst project = extension.project
            konst dependencies = project.provider { resolver.dependencies(project) }
            extension.project.locateOrRegisterIdeResolveDependenciesTask().configure { it.dependsOn(dependencies) }
            extension.project.prepareKotlinIdeaImportTask.configure { it.dependsOn(dependencies) }
        }
    }

    override fun registerDependencyTransformer(
        transformer: IdeDependencyTransformer,
        constraint: SourceSetConstraint,
        phase: DependencyTransformationPhase
    ) {
        registeredDependencyTransformers.add(
            RegisteredDependencyTransformer(transformer, constraint, phase)
        )
    }

    override fun registerAdditionalArtifactResolver(
        resolver: IdeAdditionalArtifactResolver,
        constraint: SourceSetConstraint,
        phase: AdditionalArtifactResolutionPhase,
        priority: Priority
    ) {
        registeredAdditionalArtifactResolvers.add(
            RegisteredAdditionalArtifactResolver(
                extension.project.kotlinIdeMultiplatformImportStatistics, resolver, constraint, phase, priority
            )
        )
    }

    override fun registerDependencyEffect(effect: IdeDependencyEffect, constraint: SourceSetConstraint) {
        registeredDependencyEffects.add(
            RegisteredDependencyEffect(effect, constraint)
        )
    }

    override fun registerExtrasSerializationExtension(extension: IdeaKotlinExtrasSerializationExtension) {
        registeredExtrasSerializationExtensions.add(extension)
    }

    private fun createDependencyResolver(): IdeDependencyResolver {
        return IdeDependencyResolver(
            DependencyResolutionPhase.konstues().map { phase -> createDependencyResolver(phase) }
        )
            .withAdditionalArtifactResolver(createAdditionalArtifactsResolver())
            .withTransformer(createDependencyTransformer())
            .withEffect(createDependencyEffect())
    }

    private fun createDependencyResolver(phase: DependencyResolutionPhase) = IdeDependencyResolver resolve@{ sourceSet ->
        konst applicableResolvers = registeredDependencyResolvers
            .filter { it.phase == phase }
            .filter { it.constraint(sourceSet) }
            .groupBy { it.priority }

        /* Find resolvers in the highest resolution level and only consider those */
        applicableResolvers.keys.sortedDescending().forEach { priority ->
            konst resolvers = applicableResolvers[priority].orEmpty()
            if (resolvers.isNotEmpty()) {
                return@resolve IdeDependencyResolver(resolvers).resolve(sourceSet)
            }
        }

        /* No resolvers found */
        emptySet()
    }

    private fun createAdditionalArtifactsResolver() = IdeAdditionalArtifactResolver(
        AdditionalArtifactResolutionPhase.konstues().map { phase -> createAdditionalArtifactsResolver(phase) })

    private fun createAdditionalArtifactsResolver(phase: AdditionalArtifactResolutionPhase) =
        IdeAdditionalArtifactResolver resolve@{ sourceSet, dependencies ->
            konst applicableResolvers = registeredAdditionalArtifactResolvers
                .filter { it.phase == phase }
                .filter { it.constraint(sourceSet) }
                .groupBy { it.priority }

            applicableResolvers.keys.sortedDescending().forEach { priority ->
                konst resolvers = applicableResolvers[priority].orEmpty()
                if (resolvers.isNotEmpty()) {
                    resolvers.forEach { resolver -> resolver.resolve(sourceSet, dependencies) }
                    return@resolve
                }
            }
        }

    private fun createDependencyTransformer(): IdeDependencyTransformer {
        return IdeDependencyTransformer(DependencyTransformationPhase.konstues().map { phase ->
            createDependencyTransformer(phase)
        })
    }

    private fun createDependencyTransformer(phase: DependencyTransformationPhase): IdeDependencyTransformer {
        return IdeDependencyTransformer { sourceSet, dependencies ->
            IdeDependencyTransformer(
                registeredDependencyTransformers
                    .filter { it.phase == phase }
                    .filter { it.constraint(sourceSet) }
                    .map { it.transformer }
            ).transform(sourceSet, dependencies)
        }
    }

    private fun createDependencyEffect(): IdeDependencyEffect = IdeDependencyEffect { sourceSet, dependencies ->
        registeredDependencyEffects
            .filter { it.constraint(sourceSet) }
            .forEach { it.effect(sourceSet, dependencies) }
    }

    private fun createSerializationContext(): IdeaKotlinSerializationContext {
        return IdeaSerializationContext(
            logger = extension.project.logger,
            extrasSerializationExtensions = registeredExtrasSerializationExtensions.toList()
        )
    }

    private data class RegisteredDependencyTransformer(
        konst transformer: IdeDependencyTransformer,
        konst constraint: SourceSetConstraint,
        konst phase: DependencyTransformationPhase
    )

    private data class RegisteredDependencyEffect(
        konst effect: IdeDependencyEffect,
        konst constraint: SourceSetConstraint,
    )

    private data class RegisteredDependencyResolver(
        private konst statistics: IdeMultiplatformImportStatistics,
        private konst resolver: IdeDependencyResolver,
        konst constraint: SourceSetConstraint,
        konst phase: DependencyResolutionPhase,
        konst priority: Priority,
    ) : IdeDependencyResolver {

        private class TimeMeasuredResult(konst timeInMillis: Long, konst dependencies: Set<IdeaKotlinDependency>)

        override fun resolve(sourceSet: KotlinSourceSet): Set<IdeaKotlinDependency> {
            return runCatching { resolveTimed(sourceSet) }
                .onFailure { error -> reportError(sourceSet, error) }
                .onSuccess { result -> reportSuccess(sourceSet, result) }
                .onSuccess { result -> attachResolvedByExtra(result.dependencies) }
                .getOrNull()?.dependencies.orEmpty()
        }

        private fun resolveTimed(sourceSet: KotlinSourceSet): TimeMeasuredResult {
            konst (time, result) = measureTimeMillisWithResult { resolver.resolve(sourceSet) }
            statistics.addExecutionTime(resolver::class.java, time)
            return TimeMeasuredResult(time, result)
        }

        private fun reportError(sourceSet: KotlinSourceSet, error: Throwable) {
            logger.error("e: ${resolver::class.java.name} failed on ${IdeaKotlinSourceCoordinates(sourceSet)}", error)
        }

        private fun reportSuccess(sourceSet: KotlinSourceSet, result: TimeMeasuredResult) {
            if (!logger.isDebugEnabled) return
            logger.debug(
                "${resolver::class.java.name} resolved on ${IdeaKotlinSourceCoordinates(sourceSet)}: " +
                        "${result.dependencies} (${result.timeInMillis} ms)"
            )
        }

        private fun attachResolvedByExtra(dependencies: Iterable<IdeaKotlinDependency>) {
            dependencies.forEach { dependency ->
                if (dependency.resolvedBy == null) dependency.resolvedBy = resolver
            }
        }
    }

    private class RegisteredAdditionalArtifactResolver(
        private konst statistics: IdeMultiplatformImportStatistics,
        private konst resolver: IdeAdditionalArtifactResolver,
        konst constraint: SourceSetConstraint,
        konst phase: AdditionalArtifactResolutionPhase,
        konst priority: Priority,
    ) : IdeAdditionalArtifactResolver {
        override fun resolve(sourceSet: KotlinSourceSet, dependencies: Set<IdeaKotlinDependency>) {
            runCatching { measureTimeMillis { resolver.resolve(sourceSet, dependencies) } }
                .onFailure { logger.error("e: ${resolver::class.java.name} failed on ${IdeaKotlinSourceCoordinates(sourceSet)}", it) }
                .onSuccess { statistics.addExecutionTime(resolver::class.java, it) }
        }
    }
}
