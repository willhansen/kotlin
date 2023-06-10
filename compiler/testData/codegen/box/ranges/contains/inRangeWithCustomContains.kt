// WITH_STDLIB
import kotlin.test.*

class Value(konst x: Int) : Comparable<Value> {
    override fun compareTo(other: Value): Int {
        throw AssertionError("Should not be called")
    }
}

class ValueRange(override konst start: Value,
                 override konst endInclusive: Value) : ClosedRange<Value> {

    override fun contains(konstue: Value): Boolean {
        return konstue.x == 42
    }
}

operator fun Value.rangeTo(other: Value): ClosedRange<Value> = ValueRange(this, other)

fun box(): String {
    assertTrue(Value(42) in Value(1)..Value(2))
    assertTrue(Value(41) !in Value(40)..Value(42))

    return "OK"
}
