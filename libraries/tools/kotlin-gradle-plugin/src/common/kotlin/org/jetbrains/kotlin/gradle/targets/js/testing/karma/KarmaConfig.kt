/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.testing.karma

import java.io.File

// https://karma-runner.github.io/4.0/config/configuration-file.html
data class KarmaConfig(
    var singleRun: Boolean = true,
    var autoWatch: Boolean = false,
    var basePath: String? = null,
    konst files: MutableList<Any> = mutableListOf(),
    konst frameworks: MutableList<String> = mutableListOf(),
    konst client: KarmaClient = KarmaClient(),
    konst browsers: MutableList<String> = mutableListOf(),
    konst customLaunchers: MutableMap<String, CustomLauncher> = mutableMapOf(),
    var customContextFile: String? = null,
    var customDebugFile: String? = null,
    konst failOnFailingTestSuite: Boolean = false,
    konst failOnEmptyTestSuite: Boolean = false,
    konst reporters: MutableList<String> = mutableListOf(),
    konst preprocessors: MutableMap<String, MutableList<String>> = mutableMapOf(),
    konst proxies: MutableMap<String, String> = mutableMapOf()
)

data class KarmaFile(
    konst pattern: String,
    konst included: Boolean,
    konst served: Boolean,
    konst watched: Boolean
)

data class KarmaClient(
    konst args: MutableList<String> = mutableListOf()
)

class CustomLauncher(var base: String) {
    konst flags = mutableListOf<String>()
    var debug: Boolean? = null
}

data class Reporter(
    konst type: String,
    konst subDir: String? = null,
    konst file: String? = null
)