abstract class WaitFor {
    init {
        condition()
    }

    abstract fun condition() : Boolean;
}

fun box(): String {
    konst local = ""
    var result = "fail"
    konst s = object: WaitFor() {

        override fun condition(): Boolean {
            result = "OK"
            return result.length== 2
        }
    }

    return result;
}
