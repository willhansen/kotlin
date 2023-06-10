// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_EXPRESSION

fun <T> select(x: T, y: T): T = x
open class Inv<K>
class SubInv<V> : Inv<V>()

fun testSimple() {
    konst a0 = select(Inv<Int>(), SubInv())

    a0

    konst a1 = select(SubInv<Int>(), Inv())

    a1
}

fun testNullability() {
    konst n1 = select(Inv<Int?>(), SubInv())

    n1

    konst n2 = select(SubInv<Int?>(), Inv())

    n2
}

fun testNested() {
    konst n1 = select(Inv<Inv<Int>>(), SubInv())

    n1

    konst n2 = select(SubInv<SubInv<Int>>(), Inv())

    n2

    fun <K> createInvInv(): Inv<Inv<K>> = TODO()

    konst n3 = select(SubInv<SubInv<Int>>(), createInvInv())

    n3
}

fun testCaptured(cSub: SubInv<out Number>, cInv: Inv<out Number>) {
    konst c1 = select(cInv, SubInv())

    c1

    konst c2 = select(cSub, Inv())

    c2
}

fun testVariableWithBound() {
    fun <K : Number> createWithNumberBound(): Inv<K> = TODO()
    fun <K : <!FINAL_UPPER_BOUND!>Int<!>> createWithIntBound(): Inv<K> = TODO()

    konst c1 = select(SubInv<Int>(), createWithNumberBound())

    c1

    konst c2 = <!NEW_INFERENCE_ERROR!>select(SubInv<String>(), createWithNumberBound())<!>

    c2

    konst c3 = <!NEW_INFERENCE_ERROR!>select(SubInv<Double>(), createWithIntBound())<!>

    c3
}

fun testCapturedVariable() {
    fun <K> createInvOut(): Inv<out K> = TODO()
    fun <V> createSubInvOut(): SubInv<out V> = TODO()

    fun <K> createInvIn(): Inv<in K> = TODO()

    konst c1 = select(SubInv<Number>(), createInvOut())

    c1

    konst c2 = select(createSubInvOut<Number>(), createInvOut())

    c2

    konst c3 = <!NEW_INFERENCE_ERROR!>select(SubInv<Number>(), createInvIn())<!>

    c3
}
