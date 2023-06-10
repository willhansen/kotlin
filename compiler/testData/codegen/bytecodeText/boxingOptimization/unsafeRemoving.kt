fun returningBoxed() : Int? = 1
fun acceptingBoxed(x : Int?) : Int ? = x

class A(var x : Int? = null)

konst one = 1

fun foo() {
    konst rb = returningBoxed()
    acceptingBoxed(2)

    konst a = A()
    a.x = 3

    konst b = arrayOfNulls<Int>(4)
    b[100] = 5

    konst y: Int? = 7
    konst z: Int? = 8
    konst res = y === z

    konst c1: Any = if (1 == one) 0 else "abc"
    konst c2: Any = if (1 != one) 0 else "abc"
}

// 8 java/lang/Integer.konstueOf
// 0 intValue
