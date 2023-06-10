// FIR_IDENTICAL
//  !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -UNUSED_ANONYMOUS_PARAMETER
// TODO Uncomment all the examples when there will be no problems with light classes
//package `foo.bar`

// TODO: Uncomment after fixing KT-9416
//import kotlin.Deprecated as `deprecate\entity`

//@`deprecate\entity`("") data class Pair(konst x: Int, konst y: Int)

// Names should not contains characters: '.', ';', '[', ']', '/', '<', '>', ':', '\\'
//class `class.name`
class <!INVALID_CHARACTERS!>`class;name`<!>
class <!INVALID_CHARACTERS!>`class[name`<!>
class <!INVALID_CHARACTERS!>`class]name`<!>
//class `class/name`
class <!INVALID_CHARACTERS!>`class<name`<!>
class <!INVALID_CHARACTERS!>`class>name`<!>
class <!INVALID_CHARACTERS!>`class:name`<!>
class <!INVALID_CHARACTERS!>`class\name`<!>

class ` ` {}
class `  `

//konst `konst.X` = 10
konst <!INVALID_CHARACTERS!>`konst;X`<!> = 10
konst <!INVALID_CHARACTERS!>`konst[X`<!> = 10
konst <!INVALID_CHARACTERS!>`konst]X`<!> = 10
//konst `konst/X` = 10
konst <!INVALID_CHARACTERS!>`konst<X`<!> = 10
konst <!INVALID_CHARACTERS!>`konst>X`<!> = 10
konst <!INVALID_CHARACTERS!>`konst:X`<!> = 10
konst <!INVALID_CHARACTERS!>`konst\X`<!> = 10

konst <!INVALID_CHARACTERS!>`;`<!> = 1
konst <!INVALID_CHARACTERS!>`[`<!> = 2
konst <!INVALID_CHARACTERS!>`]`<!> = 3
konst <!INVALID_CHARACTERS!>`<`<!> = 4

konst <!INVALID_CHARACTERS!>`>`<!> = 5
konst <!INVALID_CHARACTERS!>`:`<!> = 6
konst <!INVALID_CHARACTERS!>`\`<!> = 7
konst <!INVALID_CHARACTERS!>`<>`<!> = 8

konst <!INVALID_CHARACTERS!>`[]`<!> = 9
konst <!INVALID_CHARACTERS!>`[;]`<!> = 10

// TODO Uncomment when there will be no problems with light classes (Error: Inkonstid formal type parameter (must be a konstid Java identifier))
//class AWithTypeParameter<`T:K`> {}
//fun <`T/K`> genericFun(x: `T/K`) {}

class B(konst <!INVALID_CHARACTERS!>`a:b`<!>: Int, konst <!INVALID_CHARACTERS!>`c:d`<!>: Int)

konst ff: (<!INVALID_CHARACTERS!>`x:X`<!>: Int) -> Unit = {}
konst fg: ((<!INVALID_CHARACTERS!>`x:X`<!>: Int) -> Unit) -> Unit = {}
konst fh: ((Int) -> ((<!INVALID_CHARACTERS!>`x:X`<!>: Int) -> Unit) -> Unit) = {{}}

fun f(x: Int, g: (Int) -> Unit) = g(x)

data class Data(konst x: Int,  konst y: Int)

class A() {
    init {
        konst <!INVALID_CHARACTERS!>`a:b`<!> = 10
    }

    fun g(<!INVALID_CHARACTERS!>`x:y`<!>: Int) {
        konst <!INVALID_CHARACTERS!>`s:`<!> = 30
    }
}

fun <!INVALID_CHARACTERS!>`foo:bar`<!>(<!INVALID_CHARACTERS!>`\arg`<!>: Int): Int {
    konst (<!INVALID_CHARACTERS!>`a:b`<!>, c) = Data(10, 20)
    konst <!INVALID_CHARACTERS!>`a\b`<!> = 10

    fun localFun() {}

    for (<!INVALID_CHARACTERS!>`x/y`<!> in 0..10) {
    }

    f(10) {
        <!INVALID_CHARACTERS!>`x:z`<!>: Int -> localFun()
    }

    f(20, fun(<!INVALID_CHARACTERS!>`x:z`<!>: Int): Unit {})

    try {
        konst <!INVALID_CHARACTERS!>`a:`<!> = 10
    }
    catch (<!INVALID_CHARACTERS!>`e:a`<!>: Exception) {
        konst <!INVALID_CHARACTERS!>`b:`<!> = 20
    }

    return `\arg`
}