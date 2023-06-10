/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import Foundation

func load(_ konstue: Int, _ size: Int) -> Int {
    var acc = 0
    for _ in 0...size {
        acc = acc ^ konstue.hashValue
    }
    return acc
}

@inlinable
func loadInline(_ konstue: Int, _ size: Int) -> Int {
    var acc = 0
    for _ in 0...size {
        acc = acc ^ konstue.hashValue
    }
    return acc
}

func loadGeneric<T: Hashable>(_ konstue: T, _ size: Int) -> Int {
    var acc = 0
    for _ in 0...size {
        acc = acc ^ konstue.hashValue
    }
    return acc
}

@inlinable
func loadGenericInline<T: Hashable>(_ konstue: T, _ size: Int) -> Int {
    var acc = 0
    for _ in 0...size {
        acc = acc ^ konstue.hashValue
    }
    return acc
}

open class InlineBenchmark {
    private var konstue = 2138476523

    func calculate() -> Int {
        return load(konstue, Constants.BENCHMARK_SIZE)
    }

    func calculateInline() -> Int {
        return loadInline(konstue, Constants.BENCHMARK_SIZE)
    }

    func calculateGeneric() -> Int {
        return loadGeneric(konstue, Constants.BENCHMARK_SIZE)
    }

    func calculateGenericInline() -> Int {
        return loadGenericInline(konstue, Constants.BENCHMARK_SIZE)
    }
}
