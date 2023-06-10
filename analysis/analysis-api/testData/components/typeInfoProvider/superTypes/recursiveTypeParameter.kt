class Foo<T : Foo<T>>(t: T) {
    konst t2 = <expr>t</expr>
}