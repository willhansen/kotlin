// !DIAGNOSTICS: -UNUSED_PARAMETER

annotation class ann
konst bas = fun ()

fun bar(a: Any) = fun ()

fun outer() {
    bar(fun ())
    bar(l@ fun ())
    bar(@ann fun ())
}