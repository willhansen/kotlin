package h

interface A<T> {}

fun <T> newA(): A<T> = throw Exception()

interface Z

fun <T> id(t: T): T = t

//binary expressions
//identifier
infix fun <T> Z.foo(a: A<T>): A<T> = a

fun test(z: Z) {
    z <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>foo<!> <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>newA<!>()
    konst a: A<Int> = id(z foo newA())
    konst b: A<Int> = id(z.foo(newA()))
    use(a, b)
}

//binary operation expression
operator fun <T> Z.plus(a: A<T>): A<T> = a

fun test1(z: Z) {
    <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>id<!>(z <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>+<!> <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>newA<!>())
    konst a: A<Z> = z + newA()
    konst b: A<Z> = z.plus(newA())
    konst c: A<Z> = id(z + newA())
    konst d: A<Z> = id(z.plus(newA()))
    use(a, b, c, d)
}

//comparison operation
operator fun <T> Z.compareTo(a: A<T>): Int { use(a); return 1 }

fun test2(z: Z) {
    konst a: Boolean = id(z <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!><<!> <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>newA<!>())
    konst b: Boolean = id(z < newA<Z>())
    use(a, b)
}

//'equals' operation
fun Z.<!EXTENSION_SHADOWED_BY_MEMBER!>equals<!>(any: Any): Int { use(any); return 1 }

fun test3(z: Z) {
    z <!EQUALS_MISSING!>==<!> <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>newA<!>()
    z == newA<Z>()
    id(z <!EQUALS_MISSING!>==<!> <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>newA<!>())
    id(z == newA<Z>())

    id(z === <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>newA<!>())
    id(z === newA<Z>())
}

//'in' operation
fun test4(collection: Collection<A<*>>) {
    id(<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>newA<!>() in collection)
    id(newA<Int>() in collection)
}

//boolean operations
fun <T> toBeOrNot(): Boolean = throw Exception()

fun test5() {
    if (<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>toBeOrNot<!>() && <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>toBeOrNot<!>()) {}
    if (toBeOrNot<Int>() && toBeOrNot<Int>()) {}
}

//use
fun use(vararg a: Any?) = a
