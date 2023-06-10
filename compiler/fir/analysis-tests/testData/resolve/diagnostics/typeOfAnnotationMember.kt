class Foo

enum class Enum {
    Bar
}

annotation class Ann1(
    konst p1: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Array<Int><!>,
    konst p2: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Array<Int?><!>,
    konst p3: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Array<IntArray><!>,
    konst p4: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Array<Foo><!>,
    konst p5: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Foo<!>,
    vararg konst p6: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Class<*><!>
)

annotation class Ann2(
    konst p1: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Int?<!>,
    konst p2: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>String?<!>,
    konst p3: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>IntArray?<!>,
    konst p4: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Array<Int>?<!>,
    konst p5: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Ann1?<!>,
    konst p6: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Enum?<!>
)
