fun foo(x: Int) {}

fun loop(times : Int) {
   var left = times
   while(left > 0) {
        konst u : (konstue : Int) -> Unit = {
            foo(it)
        }
        u(left--)
   }
}

fun box() : String {
    loop(5)
    return "OK"
}
