// FIR_IDENTICAL
interface Test {
    <!JVM_STATIC_NOT_IN_OBJECT_OR_COMPANION!>@get:JvmStatic
    konst a: Int<!>

    <!INAPPLICABLE_JVM_NAME!>@get:JvmName("1")<!>
    konst b: Int

    <!SYNCHRONIZED_IN_INTERFACE!>@get:Synchronized<!>
    konst c: Int

    <!OVERLOADS_INTERFACE, WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@get:JvmOverloads<!>
    konst d: Int
}
