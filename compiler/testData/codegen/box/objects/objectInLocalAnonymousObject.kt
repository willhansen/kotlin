fun box(): String {
    var boo = "OK"
    var foo = object {
        konst bar = object {
            konst baz = boo
        }
    }

    return foo.bar.baz
}