// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_EXPRESSION -CAST_NEVER_SUCCEEDS -UNUSED_VARIABLE -UNCHECKED_CAST

class Foo<T>(x: T)
class Bar<S>
class OutBar<out S>
class InBar<in S>

interface IBar<S>
interface IFoo<S>

typealias OutBarAliasUseSite<T> = Bar<out T>
typealias OutBarAliasDecSite<T> = OutBar<T>

fun <T> materialize(): T = null as T

fun <K> foo0(x: K?): Bar<K> = materialize()
fun <K> foo1(x: K?): Foo<Bar<K>> = materialize()
fun <K, T: K> foo2(x: K?): Foo<Bar<T>> = materialize()
fun <T, K: T> foo3(x: K?): Foo<Bar<T>> = materialize()
fun <K> foo4(x: K?): Foo<Bar<out K>> = materialize()
fun <K> foo5(x: K?): Bar<in K> = materialize()
fun <K> foo6(x: K?): OutBar<K> = materialize()
fun <K> foo7(x: K?): InBar<K> = materialize()
fun <T, K: T, S: K, M: S> foo8(x: T?): Foo<Bar<M>> = materialize()
fun <T, K: T, S: K, M: S> foo9(x: M?): Foo<Bar<T>> = materialize()
fun <T: J, K: T, S: K, M: S, J: L, L> foo10(x: L?, y: Foo<Bar<T>>): Bar<M> = materialize()
fun <T: J, K: T, S: K, M: S, J: L, L> foo11(x: M?, y: Foo<Bar<T>>): Bar<L> = materialize()
fun <K: Any> foo12(x: K?): Bar<K> = materialize()

class Foo13<T>(x: T) {
    fun <K: T> foo1(x: K?): Bar<T> = materialize()
}

fun <K> foo14(x: K?): Bar<K> where K: Comparable<K>, K: CharSequence = materialize()
fun <K: T?, T> foo15(x: T): Bar<K> = materialize()
fun <K: T?, T> foo16(x: K): Bar<T> = materialize()
fun <K: T?, T> foo17(x: K): Bar<T> = null as Bar<T>
fun <K> foo19(x: Bar<K>): K = null as K
fun <K> Bar<K>.foo20(): K = null as K

fun <K> foo21(x: K?): Foo<Foo<OutBar<K>>> = materialize()
fun <K> foo22(x: K?): Foo<Foo<InBar<K>>> = materialize()
fun <K> foo23(x: K?): Foo<Foo<Bar<out K>>> = materialize()
fun <K> foo24(x: K?): Foo<Foo<Bar<in K>>> = materialize()

fun <K> foo25(x: K?): Bar<out K> = materialize()
fun <K> foo26(x: K?): Foo<out Foo<out Bar<out K>>> = materialize()
fun <K> foo27(x: K?): Foo<out Foo<Bar<out K>>> = materialize()
fun <K> foo28(x: K?): OutBar<OutBar<OutBar<K>>> = materialize()
fun <K> foo29(x: K?): OutBar<Bar<OutBar<K>>> = materialize()
fun <K> foo30(x: K?): OutBar<Bar<out OutBar<K>>> = materialize()
fun <K> foo31(x: K?): OutBarAliasUseSite<K> = materialize()
fun <K> foo32(x: K?): OutBarAliasDecSite<K> = materialize()
fun <K> foo33(x: K?): OutBar<InBar<OutBar<K>>> = materialize()
fun <K> foo34(x: K?): OutBar<Bar<in OutBar<K>>> = materialize()
fun <K> foo35(x: K?): InBar<K> = materialize()
fun <K> foo36(x: K?): Bar<in K> = materialize()
fun <K, T: Bar<K>> foo37(x: K?): T = materialize()
fun <K, T: Bar<S>, S: Bar<K>> foo38(x: K?): T = materialize()
fun <K, T: Bar<S>, S: Bar<K>> foo39(x: K?): Bar<T> = materialize()
fun <K, T: Bar<K>> foo40(x: K?): Bar<T> = materialize()
fun <K, T: Bar<K>> foo41(x: K?): T = materialize()
fun <K, S: K, T> foo42(x: K?): T where T: IFoo<S> = materialize()
fun <K, S: K, T> foo43(x: K?): T where T: IBar<S>, T: IFoo<S> = materialize()
fun <K, S, T: S> foo44(x: K?): T where S: IFoo<String>, S: IBar<K> = materialize()
fun <K, T: OutBar<S>, S: Bar<K>> foo45(x: K?): OutBar<T> = materialize()
fun <K, T: OutBar<S>, S: OutBar<K>> foo46(x: K?): Bar<T> = materialize()
fun <K, T: OutBar<S>, S: OutBar<K>> foo47(x: K?): OutBar<T> = materialize()
fun <U: Any> foo48(fn: Function0<U?>): Bar<U> = materialize()

konst <K> K?.vfoo0: Foo<Bar<K>> get() = materialize()
konst <K> K?.vfoo1: OutBar<Bar<out OutBar<K>>> get() = materialize()
konst <K> K?.vfoo2: OutBar<Bar<in OutBar<K>>> get() = materialize()

class Main<L>(x: L?, y: L) {
    init {
        if (x != null && y != null) {
            konst x12 = foo1(x)
            konst x13 = foo1(y)
        }
        if (x != null && y != null) {
            konst x120 = foo12(x)
            konst x121 = foo12(y)
        }
        if (x != null) {
            konst x137 = Foo13(y).foo1(x)
        }
        if (y != null) {
            konst x138 = Foo13(x).foo1(y)
        }
        if (x != null && y != null) {
            konst x153 = foo15(x)
            konst x154 = foo15(y)
        }
        if (x != null && y != null) {
            konst x163 = foo16(x)
            konst x164 = foo16(y)
        }
    }

    konst x00 = foo0(x)
    konst x01 = foo0(y)

    konst x10 = foo1(x)
    konst x11 = foo1(y)

    konst x12 = foo1(x!!)
    konst x13 = foo1(y!!)

    konst x20 = foo2(x)
    konst x21 = foo2(y)

    konst x30 = foo3(x)
    konst x31 = foo3(y)

    konst x40 = foo4(x)
    konst x41 = foo4(y)

    konst x50 = foo5(x)
    konst x51 = foo5(y)

    konst x60 = foo6(x)
    konst x61 = foo6(y)

    konst x70 = foo7(x)
    konst x71 = foo7(y)

    konst x80 = foo8(x)
    konst x81 = foo8(y)

    konst x90 = foo9(x)
    konst x91 = foo9(y)

    konst x100 = foo10(x, Foo(Bar()))
    konst x101 = foo10(y, Foo(Bar()))

    konst x110 = foo11(x, Foo(Bar()))
    konst x111 = foo11(y, Foo(Bar()))

    konst x120 = foo12(x!!)
    konst x121 = foo12(y!!)

    konst x122 = foo12(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
    konst x123 = foo12(<!ARGUMENT_TYPE_MISMATCH!>y<!>)

    konst x133 = Foo13(x).foo1(y)
    konst x135 = Foo13(y).foo1(y)
    konst x137 = Foo13(y).foo1(x!!)
    konst x138 = Foo13(x).foo1(y!!)

    konst x140 = foo14("y")
    konst x141 = foo14("x")

    konst x151 = foo15(x)
    konst x152 = foo15(y)
    konst x153 = foo15(x!!)
    konst x154 = foo15(y!!)

    konst x161 = foo16(x)
    konst x162 = foo16(y)
    konst x163 = foo16(x!!)
    konst x164 = foo16(y!!)

    konst x170 = foo17(x)
    konst x171 = foo17(y)

    konst x180 = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>Bar<!>().<!UNRESOLVED_REFERENCE!>foo18<!>(x)
    konst x181 = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>Bar<!>().<!UNRESOLVED_REFERENCE!>foo18<!>(y)

    konst x200: L = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>Bar<!>().<!UNRESOLVED_REFERENCE!>foo19<!>()
    konst x201: L = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>Bar<!>().<!UNRESOLVED_REFERENCE!>foo19<!>()

    konst x210 = foo21(x)
    konst x211 = foo21(y)

    konst x220 = foo22(x)
    konst x221 = foo22(y)

    konst x230 = foo23(x)
    konst x231 = foo23(y)

    konst x240 = foo24(x)
    konst x241 = foo24(y)

    konst x250 = foo25(x)
    konst x251 = foo25(y)

    konst x260 = foo26(x)
    konst x261 = foo26(y)

    konst x270 = foo27(x)
    konst x271 = foo27(y)

    konst x280 = foo28(x)
    konst x281 = foo28(y)

    konst x290 = foo29(x)
    konst x291 = foo29(y)

    konst x300 = foo30(x)
    konst x301 = foo30(y)

    konst x310 = foo31(x)
    konst x311 = foo31(y)

    konst x320 = foo32(x)
    konst x321 = foo32(y)

    konst x330 = foo33(x)
    konst x331 = foo33(y)

    konst x340 = foo34(x)
    konst x341 = foo34(y)

    konst x350 = foo35(x)
    konst x351 = foo35(y)

    konst x360 = foo36(x)
    konst x361 = foo36(y)

    konst vx01 = x.vfoo0
    konst vx02 = y.vfoo0

    konst vx11 = x.vfoo1
    konst vx12 = y.vfoo1

    konst vx21 = x.vfoo2
    konst vx22 = y.vfoo2

    konst x370 = foo37(x)
    konst x371 = foo37(y)

    konst x380 = foo38(x)
    konst x381 = foo38(y)

    konst x390 = foo39(x)
    konst x391 = foo39(y)

    konst x400 = foo40(x)
    konst x401 = foo40(y)

    konst x410 = foo41(x)
    konst x411 = foo41(y)

    konst x420 = foo42(x)
    konst x421 = foo42(y)

    konst x430 = foo43(x)
    konst x431 = foo43(y)

    // Change after fix KT-37380
    konst x440 = foo44(x)
    konst x441 = foo44(y)

    konst x450 = foo45(x)
    konst x451 = foo45(y)

    konst x460 = foo46(x)
    konst x461 = foo46(y)

    konst x470 = foo47(x)
    konst x471 = foo47(y)

    fun <R> takeLambda(block: () -> R): R = materialize()
    konst x480 = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>takeLambda<!> { foo48 { <!ARGUMENT_TYPE_MISMATCH!>x<!> } }
    konst x481 = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>takeLambda<!> { foo48 { <!ARGUMENT_TYPE_MISMATCH!>y<!> } }
    konst x482 = takeLambda { foo48 { null } }
}

fun <T : Comparable<T>> nullsLast() = null as Foo<T?>
fun <K> take(x: Foo<K>, comparator: Foo<K>): Foo<K> {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun <L> test() {
    take(null as Foo<String?>, nullsLast())
}

class Inv1<T>
class Inv2<T>
fun <K : Comparable<K>> Inv1<K>.assertStableSorted() {}
fun <K : Comparable<K>> Inv2<K>.assertStableSorted() = Inv1<K>().assertStableSorted()
