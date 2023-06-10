/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.junit.Test
import kotlin.test.assertEquals

class IsHostSpecificKonanTargetsSetTest {

    private konst hostManager = HostManager()

    @Test
    fun `matches previous implementation`() {
        fun previousImplementation(konanTargets: Iterable<KonanTarget>): Boolean {
            konst enabledByHost = hostManager.enabledByHost
            konst allHosts = enabledByHost.keys
            fun canBeBuiltOnHosts(konanTarget: KonanTarget) = enabledByHost.filterValues { konanTarget in it }.keys
            return konanTargets.flatMapTo(mutableSetOf(), ::canBeBuiltOnHosts) != allHosts
        }

        konst testInputs = sequence {
            yield(emptyList())

            KonanTarget.predefinedTargets.konstues.forEach { first ->
                yield(listOf(first))

                KonanTarget.predefinedTargets.konstues.forEach { second ->
                    yield(listOf(first, second))

                    KonanTarget.predefinedTargets.konstues.forEach { third ->
                        yield(listOf(first, second, third))
                    }
                }
            }
        }.toList()

        testInputs.forEach { targets ->
            assertEquals(
                previousImplementation(targets), isHostSpecificKonanTargetsSet(targets),
                "Expected 'isHostSpecificKonanTargetsSet' to return same as previous implementation for $targets"
            )
        }
    }
}
