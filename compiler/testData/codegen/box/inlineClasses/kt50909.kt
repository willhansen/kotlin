// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

data class Parent(konst child: Parent.Child?) {
    konst result =
        if (this.child == null) foo(this.child)
        else "Fail"

    @JvmInline
    konstue class Child(konst konstue: String)
}

fun foo(x: String?): String =
    x ?: "OK"

fun box(): String =
    Parent(null).result
