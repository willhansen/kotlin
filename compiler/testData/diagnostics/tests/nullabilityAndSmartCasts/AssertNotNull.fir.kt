// !CHECK_TYPE

fun main() {
    konst a : Int? = null
    konst b : Int? = null
    checkSubtype<Int>(a!!)
    a<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!> + 2
    a<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.plus(2)
    a<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.plus(b!!)
    2.plus(b<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)
    2 + b<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>

    konst c = 1
    c<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>

    konst d : Any? = null

    if (d != null) {
        d<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    }

    // smart cast isn't needed, but is reported due to KT-4294
    if (d is String) {
        d<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    }

    if (d is String?) {
        if (d != null) {
            d<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
        }
        if (d is String) {
            d<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
        }
    }

    konst f : String = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>a<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!><!>
    checkSubtype<String>(<!ARGUMENT_TYPE_MISMATCH!>a<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!><!>)
}
