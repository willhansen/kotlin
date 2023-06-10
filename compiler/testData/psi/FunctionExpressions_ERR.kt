konst a = fun )
konst a = fun foo)

konst a = fun @[a] T.foo(a : ) : bar

konst a = fun @[a()] T.foo<>(a : foo) : bar
konst a = fun @[a()] T.<>(a : foo) : bar

konst a = fun T.foo<T, , T>(a : foo) : bar
konst a = fun T.foo<, T, , T>(a : foo) : bar
konst a = fun T.foo<T, T>(, a : foo, , a: b) : bar

konst a = fun foo() : = a;

konst public_fun = public fun ()
konst open_fun = open fun ()
konst final_fun = final fun ()

konst where_fun = fun () where T: V
fun where_fun() = fun () where T: V

fun outer() {
    bar(fun )

    bar(fun T)
    bar(fun T.)
    bar(fun @[a])


    bar(public fun ())
    bar(open fun ())
    bar(final fun ())

    bar(fun () where T: V)
}