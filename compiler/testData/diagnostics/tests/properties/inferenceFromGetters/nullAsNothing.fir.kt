// !CHECK_TYPE
konst x get() = null
konst <!IMPLICIT_NOTHING_PROPERTY_TYPE!>y<!> get() = null!!

fun foo() {
    x checkType { _<Nothing?>() }
    y checkType { _<Nothing>() }
}
