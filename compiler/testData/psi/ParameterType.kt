fun test1(a) {}
fun test2(a = 4) {}
fun test3(c: Int) {}

fun test4(@ann(parameter) a) {}
fun test5(@ann a) {}

fun test() {
    try {

    }
    catch(a: Int) {

    }
}

konst a = fun (b) {}
konst a = fun (b = 4) {}
konst a = fun (b: Int) {}

konst a: (A) -> Unit
konst a: (a: A) -> Unit

class A(a: Int)