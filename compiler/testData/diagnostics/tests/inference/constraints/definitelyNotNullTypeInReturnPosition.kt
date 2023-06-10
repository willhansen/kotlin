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
            konst x12 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L & Any>>")!>foo1(<!DEBUG_INFO_EXPRESSION_TYPE("L & Any & L?")!>x<!>)<!>
            konst x13 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L & Any>>")!>foo1(<!DEBUG_INFO_EXPRESSION_TYPE("L & L & Any")!>y<!>)<!>
        }
        if (x != null && y != null) {
            konst x120 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L & Any>")!>foo12(x)<!>
            konst x121 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L & Any>")!>foo12(y)<!>
        }
        if (x != null) {
            konst x137 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>Foo13(y).foo1(x)<!>
        }
        if (y != null) {
            konst x138 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L?>")!>Foo13(x).foo1(y)<!>
        }
        if (x != null && y != null) {
            konst x153 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L?>")!>foo15(<!DEBUG_INFO_SMARTCAST!>x<!>)<!>
            konst x154 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L?>")!>foo15(<!DEBUG_INFO_SMARTCAST!>y<!>)<!>
        }
        if (x != null && y != null) {
            konst x163 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L & Any>")!>foo16(<!DEBUG_INFO_SMARTCAST!>x<!>)<!>
            konst x164 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L & Any>")!>foo16(<!DEBUG_INFO_SMARTCAST!>y<!>)<!>
        }
    }

    konst x00 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo0(x)<!>
    konst x01 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo0(y)<!>

    konst x10 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>foo1(x)<!>
    konst x11 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>foo1(y)<!>

    konst x12 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L & Any>>")!>foo1(<!DEBUG_INFO_EXPRESSION_TYPE("L & Any")!>x!!<!>)<!>
    konst x13 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L & Any>>")!>foo1(<!DEBUG_INFO_EXPRESSION_TYPE("L & Any")!>y!!<!>)<!>

    konst x20 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>foo2(x)<!>
    konst x21 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>foo2(y)<!>

    konst x30 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>foo3(x)<!>
    konst x31 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>foo3(y)<!>

    konst x40 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<out L>>")!>foo4(x)<!>
    konst x41 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<out L>>")!>foo4(y)<!>

    konst x50 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<in L>")!>foo5(x)<!>
    konst x51 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<in L>")!>foo5(y)<!>

    konst x60 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<L & Any>")!>foo6(x)<!>
    konst x61 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<L & Any>")!>foo6(y)<!>

    konst x70 = <!DEBUG_INFO_EXPRESSION_TYPE("InBar<L>")!>foo7(x)<!>
    konst x71 = <!DEBUG_INFO_EXPRESSION_TYPE("InBar<L>")!>foo7(y)<!>

    konst x80 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>foo8(x)<!>
    konst x81 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>foo8(y)<!>

    konst x90 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>foo9(x)<!>
    konst x91 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>foo9(y)<!>

    konst x100 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo10(x, <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>Foo(Bar())<!>)<!>
    konst x101 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo10(y, <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>Foo(Bar())<!>)<!>

    konst x110 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo11(x, <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>Foo(Bar())<!>)<!>
    konst x111 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo11(y, <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>Foo(Bar())<!>)<!>

    konst x120 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L & Any>")!>foo12(x!!)<!>
    konst x121 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L & Any>")!>foo12(y!!)<!>

    konst x122 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo12(<!TYPE_MISMATCH!>x<!>)<!>
    konst x123 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo12(<!TYPE_MISMATCH!>y<!>)<!>

    konst x133 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L?>")!>Foo13(x).foo1(y)<!>
    konst x135 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>Foo13(y).foo1(y)<!>
    konst x137 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>Foo13(y).foo1(x!!)<!>
    konst x138 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L?>")!>Foo13(x).foo1(y!!)<!>

    konst x140 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<kotlin.String>")!>foo14("y")<!>
    konst x141 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<kotlin.String>")!>foo14("x")<!>

    konst x151 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L?>")!>foo15(x)<!>
    konst x152 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L?>")!>foo15(y)<!>
    konst x153 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L?>")!>foo15(x!!)<!>
    konst x154 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L?>")!>foo15(y!!)<!>

    konst x161 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo16(x)<!>
    konst x162 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo16(y)<!>
    konst x163 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L & Any>")!>foo16(x!!)<!>
    konst x164 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L & Any>")!>foo16(y!!)<!>

    konst x170 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo17(x)<!>
    konst x171 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo17(y)<!>

    konst x180 = <!DEBUG_INFO_EXPRESSION_TYPE("Type is unknown")!><!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>Bar<!>()<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>foo18<!>(x)
    konst x181 = <!DEBUG_INFO_EXPRESSION_TYPE("Type is unknown")!><!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>Bar<!>()<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>foo18<!>(y)

    konst x200: L = <!DEBUG_INFO_EXPRESSION_TYPE("Type is unknown")!><!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>Bar<!>()<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>foo19<!>()
    konst x201: L = <!DEBUG_INFO_EXPRESSION_TYPE("Type is unknown")!><!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>Bar<!>()<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>foo19<!>()

    konst x210 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Foo<OutBar<L>>>")!>foo21(x)<!>
    konst x211 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Foo<OutBar<L>>>")!>foo21(y)<!>

    konst x220 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Foo<InBar<L>>>")!>foo22(x)<!>
    konst x221 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Foo<InBar<L>>>")!>foo22(y)<!>

    konst x230 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Foo<Bar<out L>>>")!>foo23(x)<!>
    konst x231 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Foo<Bar<out L>>>")!>foo23(y)<!>

    konst x240 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Foo<Bar<in L>>>")!>foo24(x)<!>
    konst x241 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Foo<Bar<in L>>>")!>foo24(y)<!>

    konst x250 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<out L & Any>")!>foo25(x)<!>
    konst x251 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<out L & Any>")!>foo25(y)<!>

    konst x260 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<out Foo<out Bar<out L & Any>>>")!>foo26(x)<!>
    konst x261 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<out Foo<out Bar<out L & Any>>>")!>foo26(y)<!>

    konst x270 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<out Foo<Bar<out L>>>")!>foo27(x)<!>
    konst x271 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<out Foo<Bar<out L>>>")!>foo27(y)<!>

    konst x280 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<OutBar<OutBar<L & Any>>>")!>foo28(x)<!>
    konst x281 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<OutBar<OutBar<L & Any>>>")!>foo28(y)<!>

    konst x290 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<Bar<OutBar<L>>>")!>foo29(x)<!>
    konst x291 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<Bar<OutBar<L>>>")!>foo29(y)<!>

    konst x300 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<Bar<out OutBar<L & Any>>>")!>foo30(x)<!>
    konst x301 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<Bar<out OutBar<L & Any>>>")!>foo30(y)<!>

    konst x310 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBarAliasUseSite<L & Any> /* = Bar<out L & Any> */")!>foo31(x)<!>
    konst x311 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBarAliasUseSite<L & Any> /* = Bar<out L & Any> */")!>foo31(y)<!>

    konst x320 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBarAliasDecSite<L & Any> /* = OutBar<L & Any> */")!>foo32(x)<!>
    konst x321 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBarAliasDecSite<L & Any> /* = OutBar<L & Any> */")!>foo32(y)<!>

    konst x330 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<InBar<OutBar<L>>>")!>foo33(x)<!>
    konst x331 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<InBar<OutBar<L>>>")!>foo33(y)<!>

    konst x340 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<Bar<in OutBar<L>>>")!>foo34(x)<!>
    konst x341 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<Bar<in OutBar<L>>>")!>foo34(y)<!>

    konst x350 = <!DEBUG_INFO_EXPRESSION_TYPE("InBar<L>")!>foo35(x)<!>
    konst x351 = <!DEBUG_INFO_EXPRESSION_TYPE("InBar<L>")!>foo35(y)<!>

    konst x360 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<in L>")!>foo36(x)<!>
    konst x361 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<in L>")!>foo36(y)<!>

    konst vx01 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>x.vfoo0<!>
    konst vx02 = <!DEBUG_INFO_EXPRESSION_TYPE("Foo<Bar<L>>")!>y.vfoo0<!>

    konst vx11 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<Bar<out OutBar<L & Any>>>")!>x.vfoo1<!>
    konst vx12 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<Bar<out OutBar<L & Any>>>")!>y.vfoo1<!>

    konst vx21 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<Bar<in OutBar<L>>>")!>x.vfoo2<!>
    konst vx22 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<Bar<in OutBar<L>>>")!>y.vfoo2<!>

    konst x370 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo37(x)<!>
    konst x371 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo37(y)<!>

    konst x380 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<Bar<L>>")!>foo38(x)<!>
    konst x381 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<Bar<L>>")!>foo38(y)<!>

    konst x390 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<Bar<Bar<L>>>")!>foo39(x)<!>
    konst x391 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<Bar<Bar<L>>>")!>foo39(y)<!>

    konst x400 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<Bar<L>>")!>foo40(x)<!>
    konst x401 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<Bar<L>>")!>foo40(y)<!>

    konst x410 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo41(x)<!>
    konst x411 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!>foo41(y)<!>

    konst x420 = <!DEBUG_INFO_EXPRESSION_TYPE("IFoo<L>")!>foo42(x)<!>
    konst x421 = <!DEBUG_INFO_EXPRESSION_TYPE("IFoo<L>")!>foo42(y)<!>

    konst x430 = <!DEBUG_INFO_EXPRESSION_TYPE("{IBar<L> & IBar<out L> & IFoo<L> & IFoo<out L>}")!>foo43(x)<!>
    konst x431 = <!DEBUG_INFO_EXPRESSION_TYPE("{IBar<L> & IBar<out L> & IFoo<L> & IFoo<out L>}")!>foo43(y)<!>

    // Change after fix KT-37380
    konst x440 = <!DEBUG_INFO_EXPRESSION_TYPE("{IBar<L> & IFoo<String>}")!>foo44(x)<!>
    konst x441 = <!DEBUG_INFO_EXPRESSION_TYPE("{IBar<L> & IFoo<String>}")!>foo44(y)<!>

    konst x450 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<OutBar<Bar<L>>>")!>foo45(x)<!>
    konst x451 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<OutBar<Bar<L>>>")!>foo45(y)<!>

    konst x460 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<OutBar<OutBar<L>>>")!>foo46(x)<!>
    konst x461 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<OutBar<OutBar<L>>>")!>foo46(y)<!>

    konst x470 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<OutBar<OutBar<L & Any>>>")!>foo47(x)<!>
    konst x471 = <!DEBUG_INFO_EXPRESSION_TYPE("OutBar<OutBar<OutBar<L & Any>>>")!>foo47(y)<!>

    fun <R> takeLambda(block: () -> R): R = materialize()
    konst x480 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!><!DEBUG_INFO_LEAKING_THIS!>takeLambda<!> { foo48 { <!TYPE_MISMATCH("Any; L")!>x<!> } }<!>
    konst x481 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<L>")!><!DEBUG_INFO_LEAKING_THIS!>takeLambda<!> { foo48 { <!TYPE_MISMATCH("Any; L")!>y<!> } }<!>
    konst x482 = <!DEBUG_INFO_EXPRESSION_TYPE("Bar<kotlin.Nothing>")!><!DEBUG_INFO_LEAKING_THIS!>takeLambda<!> { foo48 { null } }<!>
}

fun <T : Comparable<T>> nullsLast() = null as Foo<T?>
fun <K> take(x: Foo<K>, comparator: Foo<K>): Foo<K> {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun <L> test() {
    <!DEBUG_INFO_EXPRESSION_TYPE("Foo<kotlin.String?>")!>take(null as Foo<String?>, <!DEBUG_INFO_EXPRESSION_TYPE("Foo<kotlin.String?>")!>nullsLast()<!>)<!>
}

class Inv1<T>
class Inv2<T>
fun <K : Comparable<K>> Inv1<K>.assertStableSorted() {}
fun <K : Comparable<K>> Inv2<K>.assertStableSorted() = Inv1<K>().assertStableSorted()
