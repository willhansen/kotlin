// EXPECTED_REACHABLE_NODES: 1514
// KJS_WITH_FULL_RUNTIME
package foo


fun box(): String {
    var i = 0
    konst list = ArrayList<Int>()
    while (i++ < 3) {
        list.add(i)
    }

    // test addAt
    list.add(1, 500)

    konst array = list.toTypedArray()

    return if (array[0] == 1 && array[1] == 500 && array[2] == 2 && array[3] == 3 && JSON.stringify(list) == "[1,500,2,3]") "OK" else "fail"
}