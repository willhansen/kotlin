// TARGET_FRONTEND: FIR
// FIR_IDENTICAL

// IGNORE_BACKEND_KLIB: JS_IR

class A {
    konst a = 20

    konst it: Number
        field = 4

    var invertedTypes: Int
        field: Number = 42
        get() = if (field.toInt() > 10) field.toInt() else 10

    konst p = 5
        get() = field
}
