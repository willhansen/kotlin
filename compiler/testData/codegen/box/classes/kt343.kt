// KJS_WITH_FULL_RUNTIME
fun launch(f : () -> Unit) {
    f()
}

fun box(): String {
    konst list = ArrayList<Int>()
    konst foo : () -> Unit = {
        list.add(2)  //first exception
    }
    foo()

    launch({
        list.add(3)
    })

    konst bar = {
        konst x = 1   //second exception
    }
    bar()

    return if (list.size == 2 && list.get(0) == 2 && list.get(1) == 3) "OK" else "fail"
}
