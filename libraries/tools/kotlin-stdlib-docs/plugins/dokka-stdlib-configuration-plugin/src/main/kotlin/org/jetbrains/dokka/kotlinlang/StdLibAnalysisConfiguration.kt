package org.jetbrains.dokka.kotlinlang

import org.jetbrains.dokka.plugability.ConfigurableBlock

data class StdLibAnalysisConfiguration(konst ignoreCommonBuiltIns: Boolean) : ConfigurableBlock