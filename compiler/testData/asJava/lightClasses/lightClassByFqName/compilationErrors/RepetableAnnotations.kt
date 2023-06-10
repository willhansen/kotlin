// p.Annotations

package p


class Annotations {

    @R("a") @R("b") @R("c")
    fun repeatables1() {

    }

    @R("a")
    fun repeatables2() {

    }

    @R("a") @S("b") @R("c") @S("D") @R("f")
    fun repeatables3() {

    }

}

@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class S(konst g: String)

@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class R(konst s: String)

// FIR_COMPARISON