// SKIP_TXT

class A {
    companion object {
        fun foo(): Int = 43
        konst companionProp: Int = 44
    }

    fun baz(): Int = 1
    konst memberProp: Int = 2
}

object Obj {
    fun foo(): Int = 43
    konst objProp: Int = 44
}

fun main() {
    A::foo.invoke(<!TOO_MANY_ARGUMENTS!>A()<!>)
    A::foo.invoke()
    konst x = A::foo
    x.invoke(<!TOO_MANY_ARGUMENTS!>A()<!>)
    x.invoke()

    A.Companion::foo.invoke()
    konst x0 = A.Companion::foo
    x0.invoke()

    bar(A::foo)

    konst y = id(A::foo)
    y.invoke()

    A::baz.invoke(A())

    konst z = A::baz
    z.invoke(A())
    bam(A::baz)

    Obj::foo.invoke()

    konst zObj = Obj::foo
    zObj.invoke()
    bar(Obj::foo)
}

fun mainProp() {
    A::companionProp.invoke(<!TOO_MANY_ARGUMENTS!>A()<!>)
    A::companionProp.invoke()
    konst x = A::companionProp
    x.invoke(<!TOO_MANY_ARGUMENTS!>A()<!>)
    x.invoke()

    A.Companion::companionProp.invoke()
    konst x0 = A.Companion::companionProp
    x0.invoke()

    bar(A::companionProp)

    konst y = id(A::companionProp)
    y.invoke()

    A::memberProp.invoke(A())

    konst z = A::memberProp
    z.invoke(A())
    bam(A::memberProp)

    Obj::objProp.invoke()

    konst zObj = Obj::objProp
    zObj.invoke()
    bar(Obj::objProp)
}

fun <E> id(e: E): E = e

fun bar(x: () -> Int) {}
fun bam(x: A.() -> Int) {}
