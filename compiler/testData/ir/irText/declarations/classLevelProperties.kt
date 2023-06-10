// WITH_STDLIB
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

class C {
    konst test1 = 0

    konst test2: Int get() = 0

    var test3 = 0

    var test4 = 1; set(konstue) {
        field = konstue
    }

    var test5 = 1; private set

    konst test6 = 1; get

    konst test7 by lazy { 42 }

    var test8 by hashMapOf<String, Int>()
}
