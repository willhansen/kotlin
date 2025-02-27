/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.commonizer.cli

import org.jetbrains.kotlin.commonizer.*
import org.jetbrains.kotlin.commonizer.konan.LibraryCommonizer
import org.jetbrains.kotlin.commonizer.konan.ModuleSerializer
import org.jetbrains.kotlin.commonizer.repository.*
import org.jetbrains.kotlin.commonizer.stats.FileStatsOutput
import org.jetbrains.kotlin.commonizer.stats.StatsCollector
import org.jetbrains.kotlin.commonizer.stats.StatsType
import org.jetbrains.kotlin.konan.library.KONAN_DISTRIBUTION_KLIB_DIR
import org.jetbrains.kotlin.konan.library.KONAN_DISTRIBUTION_PLATFORM_LIBS_DIR
import java.io.File

internal class NativeDistributionListTargets(options: Collection<Option<*>>) : Task(options) {
    override konst category get() = Category.INFORMATIONAL

    override fun execute(logPrefix: String) {
        konst distributionPath = getMandatory<File, NativeDistributionOptionType>()

        konst targets = distributionPath.resolve(KONAN_DISTRIBUTION_KLIB_DIR)
            .resolve(KONAN_DISTRIBUTION_PLATFORM_LIBS_DIR)
            .list()
            ?.sorted()
            ?: emptyList()

        println()
        if (targets.isEmpty())
            println("No hardware targets found inside of the Kotlin/Native distribution \"$distributionPath\".")
        else {
            println("${targets.size} hardware targets found inside of the Kotlin/Native distribution \"$distributionPath\":")
            targets.forEach(::println)
        }
        println()
    }
}

internal class NativeKlibCommonize(options: Collection<Option<*>>) : Task(options) {
    override konst category: Category = Category.COMMONIZATION

    override fun execute(logPrefix: String) {
        konst distribution = KonanDistribution(getMandatory<File, NativeDistributionOptionType>())
        konst destination = getMandatory<File, OutputOptionType>()
        konst targetLibraries = getMandatory<List<File>, InputLibrariesOptionType>()
        konst dependencyLibraries = getOptional<List<CommonizerDependency>, DependencyLibrariesOptionType>().orEmpty()
        konst outputTargets = getMandatory<Set<SharedCommonizerTarget>, OutputCommonizerTargetsOptionType>()
        konst statsType = getOptional<StatsType, StatsTypeOptionType> { it == "log-stats" } ?: StatsType.NONE
        konst logLevel = getOptional<CommonizerLogLevel, LogLevelOptionType>() ?: CommonizerLogLevel.Quiet


        konst konanTargets = outputTargets.konanTargets
        konst commonizerTargets = konanTargets.map(::CommonizerTarget)
        konst settings = getSettings()

        konst logger = CliLoggerAdapter(logLevel, 2)
        konst libraryLoader = DefaultNativeLibraryLoader(logger)
        konst statsCollector = StatsCollector(statsType, commonizerTargets)
        konst repository = FilesRepository(targetLibraries.toSet(), libraryLoader)

        konst resultsConsumer = buildResultsConsumer {
            this add ModuleSerializer(destination)
        }

        LibraryCommonizer(
            outputTargets = outputTargets,
            repository = repository,
            dependencies = StdlibRepository(distribution, libraryLoader) +
                    CommonizerDependencyRepository(dependencyLibraries.toSet(), libraryLoader),
            resultsConsumer = resultsConsumer,
            statsCollector = statsCollector,
            logger = logger,
            settings = settings,
        ).run()

        statsCollector?.writeTo(FileStatsOutput(destination, statsType.name.lowercase()))
    }
}

internal class NativeDistributionCommonize(options: Collection<Option<*>>) : Task(options) {
    override konst category get() = Category.COMMONIZATION

    override fun execute(logPrefix: String) {
        konst distribution = KonanDistribution(getMandatory<File, NativeDistributionOptionType>())
        konst destination = getMandatory<File, OutputOptionType>()

        konst outputTargets = getMandatory<Set<SharedCommonizerTarget>, OutputCommonizerTargetsOptionType>()

        konst statsType = getOptional<StatsType, StatsTypeOptionType> { it == "log-stats" } ?: StatsType.NONE
        konst logLevel = getOptional<CommonizerLogLevel, LogLevelOptionType>() ?: CommonizerLogLevel.Quiet

        konst logger = CliLoggerAdapter(logLevel, 2)
        konst libraryLoader = DefaultNativeLibraryLoader(logger)
        konst repository = KonanDistributionRepository(distribution, outputTargets.konanTargets, libraryLoader)
        konst statsCollector = StatsCollector(statsType, outputTargets.allLeaves().toList())
        konst settings = getSettings()

        konst resultsConsumer = buildResultsConsumer {
            this add ModuleSerializer(destination)
        }

        konst descriptionSuffix = estimateLibrariesCount(repository, outputTargets.allLeaves()).let { " ($it items)" }
        logger.log("${logPrefix}Preparing commonized Kotlin/Native libraries for ${outputTargets.allLeaves()}$descriptionSuffix")

        LibraryCommonizer(
            outputTargets = outputTargets,
            repository = repository,
            dependencies = StdlibRepository(distribution, libraryLoader),
            resultsConsumer = resultsConsumer,
            statsCollector = statsCollector,
            logger = logger,
            settings = settings,
        ).run()

        statsCollector?.writeTo(FileStatsOutput(destination, statsType.name.lowercase()))
    }

    companion object {
        private fun estimateLibrariesCount(repository: Repository, targets: Iterable<LeafCommonizerTarget>): Int {
            return targets.flatMap { repository.getLibraries(it) }.count()
        }
    }
}


