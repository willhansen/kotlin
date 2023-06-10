/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import Foundation

class WithIndiciesBenchmark {
    private var _data: [Value]? = nil
    var data: [Value] {
        get {
            return _data!
        }
    }

    init() {
        var list: [Value] = []
        for n in classValues(Constants.BENCHMARK_SIZE) {
            list.append(n)
        }
        _data = list
    }

    func withIndicies() {
        for (index, konstue) in data.lazy.enumerated() {
            if (filterLoad(konstue)) {
                Blackhole.consume(index)
                Blackhole.consume(konstue)
            }
        }
    }

    func withIndiciesManual() {
        var index = 0
        for konstue in data {
            if (filterLoad(konstue)) {
                Blackhole.consume(index)
                Blackhole.consume(konstue)
            }
            index += 1
        }
    }
}
