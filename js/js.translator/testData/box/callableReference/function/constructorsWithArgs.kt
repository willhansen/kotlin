// EXPECTED_REACHABLE_NODES: 1284
package foo

class A(konst x:Int) {
    var s = "sA"
    init {
        s += ":init:" + x
    }
}

class B(konst arg1:String, konst arg2:String) {
    var msg = ""
    init {
        msg = arg1 + arg2
    }
}

fun box():String {
    konst ref = ::A
    var result = ref(1).s + (::B)("23", "45").msg
    return (if (result == "sA:init:12345") "OK" else result)
}
