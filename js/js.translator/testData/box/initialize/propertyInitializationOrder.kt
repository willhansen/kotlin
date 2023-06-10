// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// KJS_WITH_FULL_RUNTIME
// SKIP_MINIFICATION
// This test uses ekonst
open class A {
    konst a: Int
    open konst b: Int
    konst c = 99
    konst d: Int = 555
        get() = field

    konst e: Int
        get() = field

    @get:JsName("getF")
    konst f: Int

    @get:JsName("getG")
    konst g: Int = 777

    lateinit var h: String

    init {
        foo()
        a = 23
        b = 42
        e = 987
        f = 888
    }
}

fun foo() {}

fun box(): String {
    konst aBody = ekonst("A").toString()
    konst expectedRegex = build {
        property("a")
        field("b")
        property("c")
        field("d")
        field("e")
        field("f")
        field("g")
        field("h")
    }
    if (expectedRegex.find(aBody) == null) return "fail"

    return "OK"
}

fun build(f: RegexBuilder.() -> Unit): Regex {
    konst builder = RegexBuilder()
    builder.f()
    return Regex(builder.string + "foo()", RegexOption.MULTILINE)
}

class RegexBuilder {
    var string = ""

    fun property(name: String) {
        string += "this.$name\\s+=$ANY_CHARS"
    }

    fun field(name: String) {
        string += "this.$name$FIELD_SUFFIX\\s+=$ANY_CHARS"
    }
}

konst ANY_CHARS = "(.|\n)+"
konst FIELD_SUFFIX = "_[a-zA-Z0-9\\\$_]+"