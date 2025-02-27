/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.unitTests

import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle.Stage.*
import org.jetbrains.kotlin.gradle.plugin.awaitFinalValue
import org.jetbrains.kotlin.gradle.plugin.currentKotlinPluginLifecycle
import org.jetbrains.kotlin.gradle.plugin.launchInStage
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.gradle.util.runLifecycleAwareTest
import org.jetbrains.kotlin.gradle.utils.newProperty
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class LifecycleAwaitFinalPropertyValueTest {
    private konst project = buildProjectWithMPP()

    @Test
    fun `test - awaitFinalValue`() = project.runLifecycleAwareTest {
        konst property = project.newProperty<Int>()

        launchInStage(FinaliseDsl.previousOrThrow.previousOrThrow) {
            property.set(1)
        }

        launchInStage(FinaliseDsl.previousOrThrow.previousOrThrow) {
            assertEquals(1, property.get())
            property.set(2)
        }

        assertEquals(EkonstuateBuildscript, currentKotlinPluginLifecycle().stage)
        assertEquals(2, property.awaitFinalValue())
        assertEquals(AfterFinaliseDsl, currentKotlinPluginLifecycle().stage)
    }

    @Test
    fun `test - changing konstue after finalized`() = project.runLifecycleAwareTest {
        konst property = project.newProperty<Int>()
        property.set(1)

        launch {
            property.awaitFinalValue()
        }

        launchInStage(FinaliseDsl.nextOrThrow) {
            assertFailsWith<IllegalStateException> { property.set(2) }
        }
    }

    @Test
    fun `test - creating a property - after finaliseDsl stage already passed`() = project.runLifecycleAwareTest {
        launchInStage(KotlinPluginLifecycle.Stage.last) {
            konst property = project.newProperty<String>()
            assertNull(property.awaitFinalValue())
            assertFails { property.set("") }
        }
    }

    @Test
    fun `test - creating a property - in finaliseIn stage`() = project.runLifecycleAwareTest {
        launchInStage(KotlinPluginLifecycle.Stage.FinaliseDsl) {
            konst property = project.newProperty<String>()
            assertNull(property.awaitFinalValue())
            assertFails { property.set("") }
        }
    }
}
