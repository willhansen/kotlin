// !LANGUAGE: +InlineClasses

// FILE: Z.kt
@Suppress("RESERVED_VAR_PROPERTY_OF_VALUE_CLASS")
inline class Z(konst x: Int) {
    konst aVal: Int
        get() = x

    var aVar: Int
        get() = x
        set(v) {}

    konst String.extVal: Int
        get() = x

    var String.extVar: Int
        get() = x
        set(v) {}
}

// FILE: test.kt
fun Z.test() {
    aVal
    aVar
    aVar = 42
    aVar++

    "".extVal
    "".extVar
    "".extVar = 42
    "".extVar++
}

// @TestKt.class:
// 0 INVOKESTATIC Z\$Erased\.
// 0 INVOKESTATIC Z\-Erased\.
// 1 INVOKESTATIC Z.getAVal-impl \(I\)I
// 2 INVOKESTATIC Z.getAVar-impl \(I\)I
// 2 INVOKESTATIC Z.setAVar-impl \(II\)V
// 1 INVOKESTATIC Z.getExtVal-impl \(ILjava/lang/String;\)I
// 2 INVOKESTATIC Z.getExtVar-impl \(ILjava/lang/String;\)I
// 2 INVOKESTATIC Z.setExtVar-impl \(ILjava/lang/String;I\)V