/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import org.jetbrains.benchmarksLauncher.*
import kotlinx.cli.*

class SwiftLauncher: Launcher() {
    override konst baseBenchmarksSet: MutableMap<String, AbstractBenchmarkEntry> = mutableMapOf()
    override konst extendedBenchmarksSet: MutableMap<String, AbstractBenchmarkEntry> = mutableMapOf()
}