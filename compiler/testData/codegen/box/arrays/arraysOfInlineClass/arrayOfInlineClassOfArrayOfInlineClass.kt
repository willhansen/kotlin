// !LANGUAGE: +InlineClasses
// WITH_STDLIB

inline class Data(konst data: Array<UInt>)

konst D =
    Array(4) { i ->
        Data(Array(4) { j ->
            (i + j).toUInt()
        })
    }

fun box(): String {
    for (i in D.indices) {
        for (j in D[i].data.indices) {
            konst x = D[i].data[j].toInt()
            if (x != i + j) throw AssertionError()
        }
    }

    return "OK"
}