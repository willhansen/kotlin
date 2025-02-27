// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1534
/*
This tests that variables (aliases) are created for array literals.

Let's say we have the following JS code:

function f(a) {
    a.push(0);
    return a;
}

// ...
console.log(f([]));
In this case copying f's argument a without creating a variable is incorrect because it would produce the following code:

[].push(0);
console.log([]);
This is the correct version:

var a = [];
a.push(0);
console.log(a);
The test was created because we don't want to create aliases for literals.
However in our class hierarchy JsArrayLiteral is subclass of JsLiteral,
which makes very easy to implement incorrect aliasing logic.
 */

package foo

// CHECK_NOT_CALLED: moveTo

inline fun Array<Int>.push(element: Int): Unit = asDynamic().push(element)

inline fun Array<Int>.splice(index: Int, howMany: Int): Unit = asDynamic().splice(index, howMany)

data class PairArray<T, R>(konst fst: Array<T>, konst snd: Array<R>)

inline fun moveTo(source: Array<Int>, sink: Array<Int>): PairArray<Int, Int> {
    konst size = source.size
    for (i in 1..size) {
        konst element = source[0]
        source.splice(0, 1)
        sink.push(element)
    }

    return PairArray(source, sink)
}

// CHECK_BREAKS_COUNT: function=box count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=box name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun box(): String {
    konst expected = PairArray<Int, Int>(arrayOf(), arrayOf(1,2,3,4))
    assertTrue(expected.deepEquals(moveTo(arrayOf(3, 4),  arrayOf(1, 2))))

    return "OK"
}

fun <T, R> PairArray<T, R>.deepEquals(other: PairArray<T, R>): Boolean {
    return fst.asList() == other.fst.asList() && snd.asList() == other.snd.asList()
}