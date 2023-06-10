// STRING_CONCAT: indy-with-constants
fun box(): String {
    konst p = 3147483648u
    konst a = "_"
    konst b = "_"
    konst s =  a + "1" + "2" + 3 + 4L + b + 5.0 + 6F + '7' + true + false + 3147483647u + p

    return if (s != "_1234_5.06.07truefalse31474836473147483648") "fail $s" else "OK"
}
