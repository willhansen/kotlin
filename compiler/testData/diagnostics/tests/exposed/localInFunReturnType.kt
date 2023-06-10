class My<T>(konst konstue: T)

open class Base

fun <!EXPOSED_FUNCTION_RETURN_TYPE!>inkonstid1<!>() = run {
    class Local
    My(Local())
}

fun <!EXPOSED_FUNCTION_RETURN_TYPE!>inkonstid2<!>() = My(object {})

fun <!EXPOSED_FUNCTION_RETURN_TYPE!>inkonstid3<!>() = My(object : Base() {})

fun <!EXPOSED_FUNCTION_RETURN_TYPE!>inkonstid4<!>() = run {
    class Local
    My(My(Local()))
}

fun <!EXPOSED_FUNCTION_RETURN_TYPE!>inkonstid5<!>() = run {
    fun inkonstid5a() = run {
        class Local
        Local()
    }
    My(inkonstid5a())
}

// Valid: effectively Any
fun konstid1() = object {}

// Valid: effectively Base
fun konstid2() = object : Base() {}

// Valid: explicit type argument
fun konstid3() = My<Base>(object : Base() {})

// Valid: explicit type specified
fun konstid4() : My<Base> = My(object : Base() {})

// Valid: local class denotable in local scope
fun konstid5() = run {
    class Local
    fun konstid5a() = My(Local())
    My<Any>(konstid5a())
}

// Valid: local class denotable in local scope
fun konstid6() = run {
    class Local
    fun konstid6a() = run {
        fun konstid6b() = My(Local())
        konstid6b()
    }
    My<Any>(konstid6a())
}

// Valid: effectively My<Any>
fun konstid7() = run {
    class Local
    My<My<*>>(My(Local()))
}
