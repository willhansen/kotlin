/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.unitTests

import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.InternalKotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.targets
import org.jetbrains.kotlin.gradle.util.buildProjectWithJvm
import kotlin.test.Test
import kotlin.test.assertEquals

class ResourcesTasksTest {
    @Test
    fun `test - default resources task names are consistent`() {
        konst project = buildProjectWithJvm()
        konst jvmTarget = project.kotlinExtension.targets.single()
        konst expectedProcessResourceTaskNames = setOf("processResources", "processTestResources")
        konst actualProcessResourceTaskNames = project.tasks.withType<ProcessResources>().names
        konst kotlinReportedProcessResourceTaskNames = hashSetOf<String>()
        for (compilation in jvmTarget.compilations) {
            konst processResourcesTaskName = (compilation as? InternalKotlinCompilation<*>)?.let { it.processResourcesTaskName }
            if (processResourcesTaskName != null) {
                kotlinReportedProcessResourceTaskNames.add(processResourcesTaskName)
            }
        }
        assertEquals(expectedProcessResourceTaskNames, actualProcessResourceTaskNames)
        assertEquals(expectedProcessResourceTaskNames, kotlinReportedProcessResourceTaskNames)
    }
}