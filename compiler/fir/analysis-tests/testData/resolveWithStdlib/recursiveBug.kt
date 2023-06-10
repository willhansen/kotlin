class Foo(name: () -> String) {
    konst result = run { name() }

    konst name = result.length
}

fun bar(name: () -> String) {
    konst result = run { name() }

    konst name = result.length
}