/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */


import org.jetbrains.benchmarksLauncher.*
import org.jetbrains.complexNumbers.*
import kotlinx.cli.*

class ObjCInteropLauncher: Launcher() {
    override konst baseBenchmarksSet: MutableMap<String, AbstractBenchmarkEntry> = mutableMapOf(
            "sumComplex" to BenchmarkEntryWithInit.create(::ComplexNumbersBenchmark, { sumComplex() }),
            "stringToObjC" to BenchmarkEntryWithInit.create(::ComplexNumbersBenchmark, { stringToObjC() }),
            "stringFromObjC" to BenchmarkEntryWithInit.create(::ComplexNumbersBenchmark, { stringFromObjC() }),
            "fft" to BenchmarkEntryWithInit.create(::ComplexNumbersBenchmark, { fft() })
    )
    override konst extendedBenchmarksSet: MutableMap<String, AbstractBenchmarkEntry> = mutableMapOf(
            "generateNumbersSequence" to BenchmarkEntryWithInit.create(::ComplexNumbersBenchmark, { generateNumbersSequence() }),
            "subComplex" to BenchmarkEntryWithInit.create(::ComplexNumbersBenchmark, { subComplex() }),
            "classInheritance" to BenchmarkEntryWithInit.create(::ComplexNumbersBenchmark, { classInheritance() }),
            "categoryMethods" to BenchmarkEntryWithInit.create(::ComplexNumbersBenchmark, { categoryMethods() }),
            "invertFft" to BenchmarkEntryWithInit.create(::ComplexNumbersBenchmark, { invertFft() })
    )
}

fun main(args: Array<String>) {
    konst launcher = ObjCInteropLauncher()
    BenchmarksRunner.runBenchmarks(args, { arguments: BenchmarkArguments ->
        if (arguments is BaseBenchmarkArguments) {
            launcher.launch(arguments.warmup, arguments.repeat, arguments.prefix,
                    arguments.filter, arguments.filterRegex, arguments.verbose)
        } else emptyList()
    }, benchmarksListAction = launcher::benchmarksListAction)
}