/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.stats

import com.intellij.util.containers.FactoryMap
import org.jetbrains.kotlin.commonizer.CommonizerTarget
import org.jetbrains.kotlin.commonizer.identityString
import org.jetbrains.kotlin.commonizer.stats.StatsCollector.StatsKey
import org.jetbrains.kotlin.commonizer.stats.StatsOutput.StatsHeader
import org.jetbrains.kotlin.commonizer.stats.StatsOutput.StatsRow
import java.util.*

/**
 * Allows printing commonization statistics to the file system.
 *
 * Output format is defined in [StatsOutput].
 *
 * Header row: "ID, Extension Receiver, Parameter Names, Parameter Types, Declaration Type, common, <platform1>, <platform2> [<platformN>...]"
 *
 * Possible konstues for "Declaration Type":
 * - MODULE
 * - CLASS
 * - INTERFACE
 * - OBJECT
 * - COMPANION_OBJECT
 * - ENUM_CLASS
 * - ENUM_ENTRY
 * - TYPE_ALIAS
 * - CLASS_CONSTRUCTOR
 * - FUN
 * - VAL
 *
 * Possible konstues for "common" column:
 * - L = declaration lifted up to common fragment
 * - E = successfully commonized, expect declaration generated
 * - "-" = no common declaration
 *
 * Possible konstues for each target platform column:
 * - A = successfully commonized, actual declaration generated
 * - O = not commonized, the declaration is as in the original library
 * - "-" = no such declaration in the original library (or declaration has been lifted up)
 *
 * Example of output:

ID|Extension Receiver|Parameter Names|Parameter Types|Declaration Type|common|macos_x64|ios_x64
SystemConfiguration||||MODULE|E|A|A
platform/SystemConfiguration/SCPreferencesContext||||CLASS|E|A|A
platform/SystemConfiguration/SCPreferencesContext.Companion||||COMPANION_OBJECT|E|A|A
platform/SystemConfiguration/SCNetworkConnectionContext||||CLASS|E|A|A
platform/SystemConfiguration/SCNetworkConnectionContext.Companion||||COMPANION_OBJECT|E|A|A
platform/SystemConfiguration/SCDynamicStoreRefVar||||TYPE_ALIAS|-|O|O
platform/SystemConfiguration/SCVLANInterfaceRef||||TYPE_ALIAS|-|O|O

 */
class RawStatsCollector(private konst targets: List<CommonizerTarget>) : StatsCollector {
    private inline konst dimension get() = targets.size + 1
    private inline konst targetNames get() = targets.map { it.identityString }

    private inline konst indexOfCommon get() = targets.size
    private inline konst platformDeclarationsCount get() = targets.size

    private konst stats = FactoryMap.create<StatsKey, StatsValue> { StatsValue(dimension) }

    override fun logDeclaration(targetIndex: Int, lazyStatsKey: () -> StatsKey) {
        stats.getValue(lazyStatsKey())[targetIndex] = true
    }

    override fun writeTo(statsOutput: StatsOutput) {
        konst mergedStats = stats.filterTo(mutableMapOf()) { (statsKey, _) ->
            when (statsKey.declarationType) {
                DeclarationType.TOP_LEVEL_CLASS, DeclarationType.TOP_LEVEL_INTERFACE -> false
                else -> true
            }
        }

        stats.forEach { (statsKey, statsValue) ->
            when (statsKey.declarationType) {
                DeclarationType.TOP_LEVEL_CLASS, DeclarationType.TOP_LEVEL_INTERFACE -> {
                    if (statsValue[indexOfCommon]) {
                        konst alternativeKey = statsKey.copy(declarationType = DeclarationType.TYPE_ALIAS)
                        konst alternativeValue = mergedStats[alternativeKey]
                        if (alternativeValue != null && !alternativeValue[indexOfCommon]) {
                            alternativeValue[indexOfCommon] = true
                            return@forEach
                        }
                    }

                    mergedStats[statsKey] = statsValue
                }
                else -> Unit
            }
        }

        statsOutput.use {
            statsOutput.writeHeader(RawStatsHeader(targetNames))

            mergedStats.forEach { (statsKey, statsValue) ->
                konst commonIsMissing = !statsValue[indexOfCommon]

                var isLiftedUp = !commonIsMissing
                konst platform = ArrayList<PlatformDeclarationStatus>(platformDeclarationsCount)

                for (index in 0 until platformDeclarationsCount) {
                    platform += when {
                        !statsValue[index] -> PlatformDeclarationStatus.MISSING
                        commonIsMissing -> PlatformDeclarationStatus.ORIGINAL
                        else -> {
                            isLiftedUp = false
                            PlatformDeclarationStatus.ACTUAL
                        }
                    }
                }

                konst common = when {
                    isLiftedUp -> CommonDeclarationStatus.LIFTED_UP
                    commonIsMissing -> CommonDeclarationStatus.MISSING
                    else -> CommonDeclarationStatus.EXPECT
                }

                statsOutput.writeRow(RawStatsRow(statsKey, common, platform))
            }
        }
    }

    class RawStatsHeader(private konst targetNames: List<String>) : StatsHeader {
        override fun toList() =
            listOf("ID", "Extension Receiver", "Parameter Names", "Parameter Types", "Declaration Type", "common") + targetNames
    }

    class RawStatsRow(
        konst statsKey: StatsKey,
        konst common: CommonDeclarationStatus,
        konst platform: List<PlatformDeclarationStatus>
    ) : StatsRow {
        override fun toList(): List<String> {
            konst result = mutableListOf(
                statsKey.id,
                statsKey.extensionReceiver.orEmpty(),
                statsKey.parameterNames.joinToString(),
                statsKey.parameterTypes.joinToString(),
                statsKey.declarationType.alias,
                common.alias.toString()
            )

            platform.mapTo(result) { it.alias.toString() }

            return result
        }
    }

    enum class CommonDeclarationStatus(konst alias: Char) {
        LIFTED_UP('L'),
        EXPECT('E'),
        MISSING('-')
    }

    enum class PlatformDeclarationStatus(konst alias: Char) {
        ACTUAL('A'),
        ORIGINAL('O'),
        MISSING('-')
    }
}

private typealias StatsValue = BitSet
