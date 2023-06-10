// !DIAGNOSTICS: -UNREACHABLE_CODE
package kt770_351_735


//KT-770 Reference is not resolved to anything, but is not marked unresolved
fun main() {
    var i = 0
    when (i) {
        1 -> i--
        2 -> i = 2  // i is surrounded by a black border
        else -> <!UNRESOLVED_REFERENCE!>j<!> = 2
    }
    System.out.println(i)
}

//KT-351 Distinguish statement and expression positions
konst w = <!EXPRESSION_EXPECTED!>while (true) {}<!>

fun foo() {
    var <!ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE!>z<!> = 2
    konst r = {  // type fun(): Any is inferred
        if (true) {
            2
        }
        else {
            z = 34
        }
    }
    konst f: ()-> Int = <!TYPE_MISMATCH!>r<!>
    konst g: ()-> Any = r
}

//KT-735 Statements without braces are prohibited on the right side of when entries.
fun box() : Int {
    konst d = 2
    var z = 0
    when(d) {
        5, 3 -> z++
        else -> z = -1000
    }
    return z
}

//More tests

fun test1() { while(true) {} }
fun test2(): Unit { while(true) {} }

fun testCoercionToUnit() {
    konst simple: ()-> Unit = {
        41
    }
    konst withIf: ()-> Unit = {
        if (true) {
            3
        } else {
            45
        }
    }
    konst i = 34
    konst withWhen : () -> Unit = {
        when(i) {
            1 -> {
                konst d = 34
                "1"
                doSmth(d)

            }
            2 -> '4'
            else -> true
        }
    }

    var <!ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE!>x<!> = 43
    konst checkType = {
        if (true) {
            x = 4
        } else {
            45
        }
    }
    konst f : () -> String = <!TYPE_MISMATCH!>checkType<!>
}

fun doSmth(i: Int) {}

fun testImplicitCoercion() {
    konst d = 21
    var z = 0
    var i = when(d) {
        3 -> null
        4 -> { konst <!NAME_SHADOWING!>z<!> = 23 }
        else -> z = 20
    }

    var u = when(d) {
        3 -> { <!IMPLICIT_CAST_TO_ANY!>z = 34<!> }
        else -> <!IMPLICIT_CAST_TO_ANY!>z--<!>
    }

    var iff = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) {
        z = 34
    }
    konst g = <!INVALID_IF_AS_EXPRESSION!>if<!> (true) 4
    konst h = if (false) <!IMPLICIT_CAST_TO_ANY!>4<!> else <!IMPLICIT_CAST_TO_ANY!>{}<!>

    bar(<!TYPE_MISMATCH!>if (true) {
        4
    } else {
        z = 342
    }<!>)
}

fun fooWithAnyArg(arg: Any) {}
fun fooWithAnyNullableArg(arg: Any?) {}

fun testCoercionToAny() {
    konst d = 21
    konst x1: Any = if (1>2) 1 else 2.0
    konst x2: Any? = if (1>2) 1 else 2.0
    konst x3: Any? = if (1>2) 1 else (if (1>2) null else 2.0)

    fooWithAnyArg(if (1>2) 1 else 2.0)
    fooWithAnyNullableArg(if (1>2) 1 else 2.0)
    fooWithAnyNullableArg(if (1>2) 1 else (if (1>2) null else 2.0))

    konst y1: Any = when(d) { 1 -> 1.0 else -> 2.0 }
    konst y2: Any? = when(d) { 1 -> 1.0 else -> 2.0 }
    konst y3: Any? = when(d) { 1 -> 1.0; 2 -> null; else -> 2.0 }

    fooWithAnyArg(when(d) { 1 -> 1.0 else -> 2.0 })
    fooWithAnyNullableArg(when(d) { 1 -> 1.0 else -> 2.0 })
    fooWithAnyNullableArg(when(d) { 1 -> 1.0; 2 -> null; else -> 2.0 })
}

fun fooWithAnuNullableResult(s: String?, name: String, optional: Boolean): Any? {
    return if (s == null) {
        if (!optional) {
            throw java.lang.IllegalArgumentException("Parameter '$name' was not found in the request")
        }
        null
    } else {
        name
    }
}

fun bar(a: Unit) {}

fun testStatementInExpressionContext() {
    var <!ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE!>z<!> = 34
    konst a1: Unit = <!ASSIGNMENT_IN_EXPRESSION_CONTEXT!>z = 334<!>
    konst f = <!EXPRESSION_EXPECTED!>for (i in 1..10) {}<!>
    if (true) return <!ASSIGNMENT_IN_EXPRESSION_CONTEXT!>z = 34<!>
    return <!EXPRESSION_EXPECTED!>while (true) {}<!>
}

fun testStatementInExpressionContext2() {
    konst a2: Unit = <!EXPRESSION_EXPECTED!>while(true) {}<!>
}
