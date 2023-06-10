//KT-1061 Can't call function defined as a konst

object X {
    konst doit = { i: Int -> i }
}

fun box() : String = if (X.doit(3) == 3) "OK" else "fail"
