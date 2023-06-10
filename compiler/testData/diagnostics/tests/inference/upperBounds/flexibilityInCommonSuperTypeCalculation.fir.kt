// !DIAGNOSTICS: -UNSUPPORTED -UNUSED_EXPRESSION -DEBUG_INFO_SMARTCAST -USELESS_CAST -UNUSED_PARAMETER -UNCHECKED_CAST -CAST_NEVER_SUCCEEDS -UNUSED_VARIABLE -UNREACHABLE_CODE -DEBUG_INFO_CONSTANT
// SKIP_TXT

// FILE: Test.java
import java.util.List;

public class Test {
    public static B foo() { return null; }
    public static <T> T bar() { return null; }
    public static <T> T id(T x) { return null; }
    public static <T> List<T> getList(T x) { return null; }
    public static List getRawList() { return null; }
}

// FILE: main.kt
class A<T>(x: T)
class B

fun <T> select(vararg x: T): T = null as T

fun case_1() {
    konst x = Test.foo() // B!

    konst result_1 = select(A(x), A(B()))
    konst result_2 = select(A(B()), A(x), A(if (true) B() else null))
    konst result_3 = select(A(x), A(if (true) B() else null))

    result_1
    result_2
    result_3
}

fun case_2() {
    konst x = Test.bar<Any>() // Any!
    konst y: Any? = null

    konst result = select(A(Any()), A(y), A(x))

    result
}

fun <T> case_3() {
    konst x = Test.bar<T>() // T!
    konst y: T? = null
    konst result = select(A(y), A(x), A(null as T))

    result
}

fun case_4() {
    konst x = Test.bar<Nothing>() // Nothing!
    konst y = null // Nothing?

    konst result = select(A(x), A(y), A(return))

    result
}

class C<T, K, L>(x: T, y: K, z: L)

fun case_5() {
    konst x = Test.foo() // B!
    konst y: B? = null

    konst result_1 = select(C(x, B(), 10), C(B(), x, 10))
    konst result_2 = select(C(B(), x, y), C(x, B(), y), C(y, x, B()), C(x, y, B()), C(y, B(), x), C(B(), y, x))

    result_1
    result_2
}

fun case_6() {
    konst x1 = Test.bar<C<C<A<Float>, B, Int>, B, B>>() // C<C<A<Float>, B, Int>, B, B>!
    konst x2 = C(null as C<A<Float?>, B?, Int>?, Test.id(B()), B()) // C<C<A<Float?>, B?, Int>?, B?, B>
    konst x3 = C(C(A(Test.id(1f)), B(), Test.id(1)), Test.id(B()), B()) // C<C<A<Float!>, B, Int!>, B!, B>
    konst x4 = C(C(A(Test.id(3f)), null as B?, null as Int?), null as B?, null as B?) // C<C<A<Float?>?, B?, Int?>, B?, B?>
    konst x5 = C(Test.id(C(A(1f), Test.id(B()), 1)), B(), Test.id(B())) // C<C<A<Float>, B!, Int>!, B, B!>
    konst x6 = Test.id(C(select(C(Test.id(A(1f)), null as B?, null as Int?), null), Test.id(B()), null as B?)) // C<C<A<Float!>, B?, Int?>?, B!, B?>!
    konst x7 = C(C(Test.id(A(1f)), B(), 1), B(), B()) // C<C<A<Float>!, B, Int>, B, B>
    konst x8 = C(Test.id(C(null as A<Float?>?, B(), null as Int?)), null as B?, Test.id(B())) // C<C<A<Float?>?, B, Int?>, B?, B>
    konst x9 = null as C<C<A<Float>, B?, Int>, B, B>? // C<C<A<Float>, B?, Int>, B, B>?

    konst result_1 = select(x1, x2, x3, x4, x5, x6, x7, x8, x9)
    konst result_2 = select(x9, x8, x7, x6, x5, x4, x3, x2, x1)
    konst result_3 = select(x5, x7, x9, x3, x1, x2, x8, x4, x6)

    result_1
    result_2
    result_3
}

fun case_7() {
    konst x0: Int = 1
    konst x1 = C(A(Test.id(x0)), B(), B())
    konst x2 = C(Test.id(A(1)), B(), B())
    x1
    x2

    konst result_5 = select(x1, x2)
    result_5
}

fun case_8() {
    konst x1 = A(10)
    konst x2 = select(A(""), null)
    konst x3 = Test.id(A(null))

    konst result_1 = select(x1, x2, x3)
    result_1
}

fun case_9() {
    konst x1 = A(A(10))
    konst x2 = A(select(A(""), null))
    konst x3 = A(Test.id(A('s')))

    konst result_1 = select(x1, x2, x3)
    result_1
}

fun case_10() {
    konst x1 = 10
    konst x2 = select(10, null)
    konst x3 = Test.id(10)

    konst result_1 = select(x2, x1, x3)
    konst result_2 = select(x2, x3, x1)
    konst result_3 = select(x1, x2, x3)
    konst result_4 = select(x1, x3, x2)
    konst result_5 = select(x3, x2, x1)
    konst result_6 = select(x3, x1, x2)

    result_1
    result_2
    result_3
    result_4
    result_5
    result_6
}

fun <T> case_11(y: T) {
    konst x1 = Test.bar<T>() // T!
    konst x2 = select(y, null)
    if (y != null) {
        konst x3 = Test.id(y) // T!
        konst result_1 = select(A(y), A(x1))
        konst result_2 = select(A(y), A(x1), A(x2))
        konst result_3 = select(A(y), A(x3), A(x2))
        konst result_4 = select(A(y as T), A(x1))
        konst result_5 = select(A(y as T), A(x1), A(x2))
        konst result_6 = select(A(y as T), A(x3), A(x2))
        konst result_7 = select(A(y as T), A(x1))
        konst result_8 = select(A(y as T), A(x3))
        konst result_9 = select(A(x3), A(y as T))

        konst result_10 = select(y, x1)
        konst result_11 = select(y, x2, x2)
        konst result_12 = select(y, x3, x2)
        konst result_13 = select(y as T, x1)
        konst result_14 = select(y as T, x1, x2)
        konst result_15 = select(y as T, x3, x2)
        konst result_16 = select(y as T, x1)
        konst result_17 = select(y as T, x3)
        konst result_18 = select(x3, y as T)

        x1
        x2
        x3
        y

        result_1
        result_2
        result_3
        result_4
        result_5
        result_6
        result_7
        result_8
        result_9

        result_10
        result_11
        result_12
        result_13
        result_14
        result_15
        result_16
        result_17
        result_18
    }
}

fun case_12() {
    konst x1 = Test.getList(10)
    konst x2 = null as MutableList<Int>
    konst x3 = select(null as List<Int>, null)

    konst result_1 = select(x1, x2)
    konst result_2 = select(x2, x1)
    konst result_3 = select(x2, x1, x3)
    konst result_4 = select(x3, x1)
    konst result_5 = select(A(x1), A(x2))
    konst result_6 = select(A(x2), A(x1))
    konst result_7 = select(A(x3), A(x1), A(x2))
    konst result_8 = select(A(x1), A(x3))

    result_1
    result_2
    result_3
    result_4
    result_5
    result_6
    result_7
    result_8
}

fun case_13() {
    konst x1 = Test.getList(10)
    konst x2: dynamic = null
    konst x3 = null as MutableList<Int>
    konst x4 = select(null as List<Int>, null)

    konst result_1 = select(x1, x2, x3, x4)
    konst result_2 = select(x2, x1)
    konst result_3 = select(x2, x1, x3)
    konst result_4 = select(x3, x2)
    konst result_5 = select(x4, x2)
    konst result_6 = select(x4, x3, x2)
    konst result_7 = select(A(x1), A(x2), A(x3), A(x4))
    konst result_8 = select(A(x2), A(x1))
    konst result_9 = select(A(x2), A(x1), A(x3))
    konst result_10 = select(A(x3), A(x2))
    konst result_11 = select(A(x4), A(x2))
    konst result_12 = select(A(x4), A(x3), A(x2))

    result_1
    result_2
    result_3
    result_4
    result_5
    result_6
    result_7
    result_8
    result_9
    result_10
    result_11
    result_12
}

fun case_14() {
    konst x1 = Test.getRawList()
    konst x2 = null as List<Int>
    konst x3 = select(null as MutableList<Int>, null)

    konst result_1 = select(x1, x2)
    konst result_2 = select(x2, x1)
    konst result_3 = select(x2, x1, x3)
    konst result_4 = select(x3, x1)
    konst result_5 = select(A(x1), A(x2))
    konst result_6 = select(A(x2), A(x1))
    konst result_7 = select(A(x3), A(x1), A(x2))
    konst result_8 = select(A(x1), A(x3))

    result_1
    result_2
    result_3
    result_4
    result_5
    result_6
    result_7
    result_8
}
