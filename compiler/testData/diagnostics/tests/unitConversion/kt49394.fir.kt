fun interface Run {
    fun run()
}

fun handle(run: Run) {
    //...
}

konst x = {
    "STRING"
}

fun test() {
    handle(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
}