
// Classes

external class C1

external enum class <!WRONG_EXTERNAL_DECLARATION!>C2<!>

external annotation class <!WRONG_EXTERNAL_DECLARATION!>C3<!>

external data class <!WRONG_EXTERNAL_DECLARATION!>C4(konst x: String)<!>

external class C5 {

    class C6

    inner class <!WRONG_EXTERNAL_DECLARATION!>C7<!>
}

external <!INLINE_CLASS_DEPRECATED!>inline<!> class <!WRONG_EXTERNAL_DECLARATION!>C8(<!EXTERNAL_CLASS_CONSTRUCTOR_PROPERTY_PARAMETER!>konst x: Int<!>)<!>

external konstue class <!WRONG_EXTERNAL_DECLARATION!>C9(<!EXTERNAL_CLASS_CONSTRUCTOR_PROPERTY_PARAMETER!>konst x: Int<!>)<!>


// Interfaces

external interface I1

external fun interface <!WRONG_EXTERNAL_DECLARATION!>I2<!> {
    fun foo(): Int
}


// Functions

external fun foo1(): Int

<!WRONG_EXTERNAL_DECLARATION!>external tailrec fun foo2(): Int<!>

<!INLINE_EXTERNAL_DECLARATION!>external inline fun foo3(f: () -> Int): Int<!>

<!WRONG_EXTERNAL_DECLARATION!>external suspend fun foo4(): Int<!>

<!WRONG_EXTERNAL_DECLARATION!>external fun Int.foo5(): Int<!>


// Properties

<!WRONG_EXTERNAL_DECLARATION!>external lateinit var v1: String<!>

<!WRONG_EXTERNAL_DECLARATION!>external konst Int.v2: String<!>
    get() = definedExternally