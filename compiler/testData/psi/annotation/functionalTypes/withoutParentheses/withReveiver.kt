// Issue: KT-31734

konst x: (@Foo suspend Int.() -> Unit) get() = {}

konst x: @Foo Int.() -> Unit get() = {}

konst x: @Foo Int.(x: kotlin.Int) -> Unit get() = {}

konst x: @Foo ((x: @Foo (@Foo () -> Int) -> Int.(@Foo () -> Int) -> Int) -> Unit) get() = {}

konst x: suspend @Foo (x: @Foo (@Foo ((x: kotlin.Any) -> Int).(x: kotlin.Any) -> Int) -> Int) -> Unit get() = {}

konst x: Comparable<@Foo @Bar(10) @Foo Unit.(x: kotlin.Any) -> Unit> get() = {}

konst x: Any = {} as @Foo suspend Int.(x: @Foo Foo) -> (y: @Foo Bar) -> Unit

fun foo(x: (@Foo ((@Foo kotlin.Any)->(Int)).(@Foo kotlin.Any)->()->Unit)) = x

fun foo(x: suspend @Foo(10) @Bar Comparable<T>.(kotlin.Any) -> Unit = { x: Int -> x }) {}

fun foo() {
    konst x: @Foo suspend @Bar (Coomparable<kotlin.Any>) -> Unit.(Coomparable<kotlin.Any>) -> Unit = {}
}

fun foo() {
    konst x = { x: suspend @Foo @Foo () -> Unit.(Coomparable<@Foo @Bar(10) @Foo () -> Unit>) -> () -> Unit -> x }
}

abstract class A {
    abstract var x: @Foo suspend (@Foo () -> Unit).(suspend (Int) -> ((Int) -> Unit)) -> Int
}

fun foo(vararg x: @Foo @Bar(10) @Foo Any.(Any) -> Unit) = 10

fun foo(): @Foo.Bar suspend Nothing.(Nothing) -> Unit = {}

fun foo(): () -> @Foo.Bar suspend Iterable<@Foo.Bar Int.(Bar) -> Unit>.(Bar) -> Unit = {}

konst x: Any get() = fun(): @Foo (@Foo (Coomparable<Nothing>) -> Unit).(Coomparable<Nothing>) -> Unit {}

fun foo() {
    var x: (@Foo (()->Unit)-> @Foo Int.()->Unit) -> Unit = {}
}

fun foo(x: Any) {
    if (x as @Foo @Bar(10) @Foo ()->Unit.(()->Unit) -> Unit is suspend @Foo @Bar(10) @Foo ((()->Unit).()->Unit) -> Unit) {}
}

fun foo(y: Any) {
    var x = y as (@Foo suspend (suspend (()->Unit)->Int).(()->Unit) -> (Float.()->Unit) -> Unit) -> Unit
}
