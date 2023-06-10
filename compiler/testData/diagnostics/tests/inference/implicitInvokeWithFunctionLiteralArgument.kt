// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

class TestClass {
    inline operator fun <T> invoke(task: () -> T) = task()
}

fun <T> test(konstue: T, test: TestClass): T {
    <!UNREACHABLE_CODE!>konst x =<!> test { return konstue }
    <!UNREACHABLE_CODE!>x checkType { _<Nothing>() }<!>

    <!UNREACHABLE_CODE!>return konstue<!>
}

// ---

class Future<T>

interface FutureCallback<E> {
    operator fun <T> invoke(f: (E) -> T): Future<T>
}

fun test(cb: FutureCallback<String>) {
    konst a = cb { it[0] }
    a checkType { _<Future<Char>>() }

    konst b = cb { it }
    b checkType { _<Future<String>>() }

    konst c = cb {}
    c checkType { _<Future<Unit>>() }

    cb.let { callback ->
        konst d = callback { it.length }
        d checkType { _<Future<Int>>() }
    }
}