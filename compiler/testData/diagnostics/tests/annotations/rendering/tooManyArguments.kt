// !RENDER_DIAGNOSTICS_MESSAGES

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class Ann(konst s: String = "")

@Ann("s")
fun foo() {}

konst bar = foo(
    <!TOO_MANY_ARGUMENTS("public fun foo(): Unit defined in root package in file tooManyArguments.kt")!>15<!>
)