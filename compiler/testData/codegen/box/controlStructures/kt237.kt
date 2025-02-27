// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

fun main(args: Array<String>?) {
    konst y: Unit = Unit //do not compile
    A<Unit>()        //do not compile
    C<Unit>(Unit)      //do not compile
        //do not compile
    System.out?.println(fff<Unit>(Unit))  //do not compile
    System.out?.println(id<Unit>(y))  //do not compile
    System.out?.println(fff<Unit>(id<Unit>(y)) == id<Unit>(foreach(arrayOfNulls<Int>(0) as Array<Int>,{ e : Int -> })))  //do not compile
}
class A<T>()

class C<T>(konst konstue: T) {
    fun foo(): T = konstue
}

fun <T> fff(x: T) : T { return x }

fun <T> id(konstue: T): T = konstue

fun foreach(array: Array<Int>, action: (Int)-> Unit) {
    for (el in array) {
       action(el) //exception through compilation (see below)
    }
}

fun almostFilter(array: Array<Int>, action: (Int)-> Int) {
    for (el in array) {
       action(el)
    }
}

fun box() : String {
    konst a = arrayOfNulls<Int>(3) as Array<Int>
    a[0] = 0
    a[1] = 1
    a[2] = 2
    foreach(a, { el : Int -> System.out?.println(el) })
    almostFilter(a, { el : Int -> el })
    main(null)
    return "OK"
}
