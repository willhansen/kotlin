konst prop: Int =
    js("1")

fun funExprBody(x: Int): Int =
    js("x")

fun funBlockBody(x: Int): Int {
    js("return x;")
}

fun <!IMPLICIT_NOTHING_RETURN_TYPE!>returnTypeNotSepcified<!>() = js("1")
<!WRONG_JS_INTEROP_TYPE!>konst <!IMPLICIT_NOTHING_PROPERTY_TYPE!>konstTypeNotSepcified<!><!> = js("1")

konst a = "1"
fun nonConst(): String = "1"

konst p0: Int = js(a)
konst p1: Int = js(("1"))
konst p2: Int = js("$a")
konst p3: Int = js("${1}")
konst p4: Int = js("${a}${a}")
konst p5: Int = js(a + a)
konst p6: Int = js("1" + "1")
konst p7: Int = js(<!JSCODE_ARGUMENT_SHOULD_BE_CONSTANT!>nonConst()<!>)

konst propWithGetter: String
    get() = "1"

konst propWithSimpleGetterAndInitializer: String = "1"
    get() = field + "2"

konst propWithComplexGetterAndInitializer: String = "1"
    get() = run { field + "2" }

var varProp = "1"

var varPropWithSetter = "1"
    set(konstue) { field = field + konstue }

const konst constProp = "1"

konst delegatedVal: String by lazy { "1" }

konst p8: Int = js(<!JSCODE_ARGUMENT_SHOULD_BE_CONSTANT!>propWithGetter<!>)

// TODO: This should be an error as property getters are no different to functions
konst p9: Int = js(propWithSimpleGetterAndInitializer)
konst p10: Int = js(propWithComplexGetterAndInitializer)

konst p11: Int = js(<!JSCODE_ARGUMENT_SHOULD_BE_CONSTANT!>varProp<!>)
konst p12: Int = js(<!JSCODE_ARGUMENT_SHOULD_BE_CONSTANT!>varPropWithSetter<!>)
konst p13: Int = js(constProp)
konst p14: Int = js(<!JSCODE_ARGUMENT_SHOULD_BE_CONSTANT!>delegatedVal<!>)


fun foo0(b: Boolean): Int =
    if (b) <!JSCODE_WRONG_CONTEXT!>js<!>("1") else <!JSCODE_WRONG_CONTEXT!>js<!>("2")

fun foo1(): Int {
    println()
    <!JSCODE_WRONG_CONTEXT!>js<!>("return x;")
}

fun foo11() {
    fun local1(): Int = <!JSCODE_WRONG_CONTEXT!>js<!>("1")
    fun local2(): Int {
        <!JSCODE_WRONG_CONTEXT!>js<!>("return 1;")
    }
    fun local3(): Int {
        println()
        <!JSCODE_WRONG_CONTEXT!>js<!>("return 1;")
    }
}

class C {
    fun memberFun1(): Int = <!JSCODE_WRONG_CONTEXT!>js<!>("1")
    fun memberFun2(): Int {
        <!JSCODE_WRONG_CONTEXT!>js<!>("return 1;")
    }

    constructor() <!UNREACHABLE_CODE!>{
        <!JSCODE_WRONG_CONTEXT!>js<!>("1;")
    }<!>

    init {
        <!JSCODE_WRONG_CONTEXT!>js<!>("1")
    }

    <!UNREACHABLE_CODE!>konst memberProperty: Int = <!JSCODE_WRONG_CONTEXT!>js<!>("1")<!>
}

fun withDefault(x: Int = <!JSCODE_WRONG_CONTEXT!>js<!>("1")) {
    println(x)
}

suspend fun suspendFun(): Int = <!JSCODE_UNSUPPORTED_FUNCTION_KIND!>js<!>("1")

inline fun inlineFun(f: () -> Int): Int = <!JSCODE_UNSUPPORTED_FUNCTION_KIND!>js<!>("f()")

fun Int.extensionFun(): Int = <!JSCODE_UNSUPPORTED_FUNCTION_KIND!>js<!>("1")

var propertyWithAccessors: Int
    get(): Int = <!JSCODE_WRONG_CONTEXT!>js<!>("1")
    set(<!UNUSED_PARAMETER!>konstue<!>: Int) {
        <!JSCODE_WRONG_CONTEXT!>js<!>("console.log(konstue);")
    }


fun inkonstidNames(
    <!JSCODE_INVALID_PARAMETER_NAME!>`a b`: Int<!>,
    <!JSCODE_INVALID_PARAMETER_NAME!>`1b`: Int<!>,
    `ab$`: Int
): Int = js("1")
