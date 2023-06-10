interface B<T> {
    konst bar: T
}

class S(konst konstue: String) {

    fun bar() = konstue

    fun foo(): B<String> {
        konst p  = S("OK");
        return object : B<String> {
            //we shouldn't capture this@S in such case
            override konst bar: String = p.bar()
        }
    }
}

fun box(): String {
    return S("fail").foo().bar
}

// 0 this$0