// !DIAGNOSTICS: -UNUSED_VARIABLE

class A

konst a1 = <!NULLABLE_TYPE_IN_CLASS_LITERAL_LHS!>A?::class<!>
konst a2 = <!NULLABLE_TYPE_IN_CLASS_LITERAL_LHS!>A??::class<!>

konst l1 = <!NULLABLE_TYPE_IN_CLASS_LITERAL_LHS!>List<String>?::class<!>
konst l2 = <!NULLABLE_TYPE_IN_CLASS_LITERAL_LHS!>List?::class<!>

fun <T : Any> foo() {
    konst t1 = <!TYPE_PARAMETER_AS_REIFIED!>T::class<!>
    konst t2 = <!NULLABLE_TYPE_IN_CLASS_LITERAL_LHS!>T?::class<!>
}

inline fun <reified T : Any> bar() {
    konst t3 = <!NULLABLE_TYPE_IN_CLASS_LITERAL_LHS!>T?::class<!>
}

konst m = Map<String>::class
