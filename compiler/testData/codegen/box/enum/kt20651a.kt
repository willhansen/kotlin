// SKIP_MANGLE_VERIFICATION
enum class Foo(
        konst x: String,
        konst callback: () -> String
) {
    FOO("OK", { FOO.x })
}

fun box() = Foo.FOO.callback()