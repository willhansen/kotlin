// !LANGUAGE: -ValueClassesSecondaryConstructorWithBody
// WITH_STDLIB

@JvmInline
konstue class Foo(konst x: String) {
    constructor(i: Int) : this(i.toString()) <!UNSUPPORTED_FEATURE!>{<!>
        println(i)
    }
}
