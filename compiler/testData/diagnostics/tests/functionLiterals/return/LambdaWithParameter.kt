// FIR_IDENTICAL
konst flag = true

konst a /*: (Int) -> String */ = l@ {
    i: Int ->
    if (i == 0) return@l i.toString()

    "Ok"
}

fun <T> foo(f: (Int) -> T): T = f(0)

konst b /*:String */ = foo {
    i ->
    if (i == 0) return@foo i.toString()

    "Ok"
}