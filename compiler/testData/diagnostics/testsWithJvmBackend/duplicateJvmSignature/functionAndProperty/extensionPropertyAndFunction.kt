// !DIAGNOSTICS: -UNUSED_PARAMETER

class C {
    <!CONFLICTING_JVM_DECLARATIONS!>fun getX(t: Any)<!> = 1
    konst Any.x: Int
        <!CONFLICTING_JVM_DECLARATIONS!>get()<!> = 1
}
