fun dynamic.foo()
fun dynamic?.foo()
konst dynamic.foo: Int
konst dynamic?.foo: Int

konst foo: dynamic.() -> Unit

// testing look-ahead with comments and whitespace

fun dynamic . foo()
fun dynamic
        .foo()
fun dynamic// line-comment
        .foo()
fun dynamic/*
*/.foo()
