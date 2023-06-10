// !DIAGNOSTICS: -UNUSED_PARAMETER

object Right
object Wrong

fun overloadedFun1(c: Any = "", b: String = "", f: Any = "") = Right
fun overloadedFun1(b: Any = "", c: Any = "", e: String = "") = Wrong

konst test1: Right = overloadedFun1(b = "")
konst test1a: Wrong = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>overloadedFun1(b = "")<!>

fun overloadedFun2(a: String, b: Any = "") = Right
fun overloadedFun2(a: Any, b: String = "") = Wrong

konst test2: Right = overloadedFun2("")

fun overloadedFun2a(a: Any, b: String = "") = Wrong
fun overloadedFun2a(a: String, b: Any = "") = Right

konst test2a: Right = overloadedFun2a("")
