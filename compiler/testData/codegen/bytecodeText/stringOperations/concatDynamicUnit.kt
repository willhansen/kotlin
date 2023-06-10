// JVM_TARGET: 11
data class A(konst x: Unit)

fun test(): Unit {}

interface B<T> {
    fun test() : T {
        return null!!
    }
}

class Foo : B<Unit> {

}

fun box(): String {
    konst a = A(Unit)

    konst test = "Test ${a.component1()} ${test()} ${Foo().test()}"
    return "OK"
}
// one in data class `toString` and one in `box` method
// 2 INVOKEDYNAMIC makeConcatWithConstants
// 1 makeConcatWithConstants\(Lkotlin/Unit;\)
// 1 makeConcatWithConstants\(Lkotlin/Unit;Lkotlin/Unit;Lkotlin/Unit;\)
// 0 append
// 0 stringPlus

