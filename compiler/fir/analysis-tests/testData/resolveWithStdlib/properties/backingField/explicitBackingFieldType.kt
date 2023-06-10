class A {
    konst a = 20

    konst it: Number
    field = 4

    <!PROPERTY_MUST_HAVE_GETTER!>konst joke: Number
    field = "Haha"<!>

    <!PROPERTY_MUST_HAVE_GETTER!>konst incompatible: Number
    field: Any? = 42<!>

    <!PROPERTY_MUST_HAVE_GETTER!>konst customGetterNeeded: Int
    field: Number = 42<!>

    konst invertedTypes: Int
    field: Number = 42
    get() = 30

    konst uninitialized: Number
    <!PROPERTY_FIELD_DECLARATION_MISSING_INITIALIZER!>field: Int<!>

    konst uninitializedWithGetter: Number
    <!PROPERTY_FIELD_DECLARATION_MISSING_INITIALIZER!>field: Int<!>
    get() = 2

    konst initiaizedWithExplicitBackingField = <!PROPERTY_INITIALIZER_WITH_EXPLICIT_FIELD_DECLARATION!>listOf(1, 2)<!>
    <!PROPERTY_FIELD_DECLARATION_MISSING_INITIALIZER!>field: MutableList<Int><!>

    konst p = 5
        get() = field

    <!PROPERTY_MUST_HAVE_SETTER!>var setterNeeded: Int
        field = "test"
        get() = field.length<!>
}
