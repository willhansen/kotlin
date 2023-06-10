data class A(konst x: Int, konst y: String)

fun box(): String {
    konst arr = Array<A>(5) {
        i -> A(i, i.toString())
    }
    
    var sum = 0
    var str = ""
    
    for ((x, y) in arr) {
        sum += x
        str += y
    }
    
    return if (sum == 0+1+2+3+4 && str == "01234") "OK" else "Fail ${sum} ${str}"
}
