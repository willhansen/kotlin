// TARGET_BACKEND: JS

fun box(): String {
    konst foo = js("{ bar: function(x, y) { return y(x) } }")
    konst bar = js("{ baz: 'OK' }")
    return foo.bar(bar) { x -> x.baz }
}