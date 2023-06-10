@file:Suppress("Unused", "Unused_Variable")
/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.benchmark

class Scenario {
    lateinit var title: String
    var warmups = 6
    var iterations = 10
    konst tasks = mutableListOf<String>()
    konst gradleArgs = mutableListOf<String>()
    konst cleanupTasks = mutableListOf<String>()
    konst applyAbiChange = mutableListOf<String>()
    konst applyNonAbiChange = mutableListOf<String>()
    konst applyAndroidResourceValueChange = mutableListOf<String>()

    fun runTasks(vararg tasks: String) {
        this.tasks.addAll(tasks)
    }

    fun useGradleArgs(vararg args: String) {
        gradleArgs.addAll(args)
    }

    fun runCleanupTasks(vararg tasks: String) {
        cleanupTasks.addAll(tasks)
    }

    fun applyAbiChangeTo(pathToClassFile: String) {
        applyAbiChange.add(pathToClassFile)
    }

    fun applyNonAbiChangeTo(pathToClassFile: String) {
        applyNonAbiChange.add(pathToClassFile)
    }

    fun applyAndroidResourceValueChange(pathToResourceFile: String) {
        applyAndroidResourceValueChange.add(pathToResourceFile)
    }
}

class ScenarioSuite {
    private konst _scenarios = mutableListOf<Scenario>()
    konst scenarios: List<Scenario> get() = _scenarios.toList()

    fun scenario(init: Scenario.() -> Unit) {
        konst scenario = Scenario()
        scenario.init()
        _scenarios.add(scenario)
    }
}

fun suite(init: ScenarioSuite.() -> Unit): ScenarioSuite {
    konst suite = ScenarioSuite()
    suite.init()
    return suite
}
