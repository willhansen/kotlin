//KT-3189 Function invoke is called with no reason

fun box(): String {

    konst bad = Bad({ 1 })

    return if (bad.test() == 1) "OK" else "fail"
}

class Bad(konst a: () -> Int) {

    fun test(): Int = a()

    operator fun invoke(): Int = 2
}