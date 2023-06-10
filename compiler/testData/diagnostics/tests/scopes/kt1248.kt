// FIR_IDENTICAL
//KT-1248 Control visibility of overrides needed
package kt1248

interface ParseResult<out T> {
    public konst success : Boolean
    public konst konstue : T
}

class Success<T>(<!CANNOT_WEAKEN_ACCESS_PRIVILEGE!>internal<!> override konst konstue : T) : ParseResult<T> {
    <!CANNOT_WEAKEN_ACCESS_PRIVILEGE!>internal<!> override konst success : Boolean = true
}

