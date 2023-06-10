// !LANGUAGE: -JvmStaticInInterface
// !DIAGNOSTICS: -UNUSED_VARIABLE
open class B {
    public open konst base1 : Int = 1
    public open konst base2 : Int = 1
}

class A {
    companion object : B() {
        var p1:Int = 1
            @JvmStatic set(p: Int) {
                p1 = 1
            }

        @JvmStatic konst z = 1;

        @JvmStatic override konst base1: Int = 0

        override konst base2: Int = 0
            @JvmStatic get
    }

    object A : B() {
        var p:Int = 1
            @JvmStatic set(p1: Int) {
                p = 1
            }

        @JvmStatic konst z = 1;

        <!OVERRIDE_CANNOT_BE_STATIC!>@JvmStatic override konst base1: Int = 0<!>

        @JvmStatic <!NON_FINAL_MEMBER_IN_OBJECT!>open<!> fun f() {}

        override konst base2: Int = 0
            <!OVERRIDE_CANNOT_BE_STATIC!>@JvmStatic get<!>
    }

    var p:Int = 1
        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic set(p1: Int)<!> {
            p = 1
        }

    <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic konst z2<!> = 1;
}
