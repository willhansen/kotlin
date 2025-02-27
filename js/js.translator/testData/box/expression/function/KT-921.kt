// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1535

var log = ""

private fun printLog(message: String) {
    log += message
}

private fun printlnLog(message: String) = printLog("$message\n")

class Lifetime() {
    konst attached = ArrayList<Function0<Unit>>()

    public fun attach(action: () -> Unit) {
        attached.add(action)
    }

    fun close() {
        for (x in attached) x()
        attached.clear()
    }
}

public class Viewable<T>() {
    konst items = ArrayList<T>()

    fun add(item: T) {
        items.add(item)
    }

    fun remove(item: T) {
        items.remove(item)
    }

    fun view(lifetime: Lifetime, viewer: (itemLifetime: Lifetime, item: T) -> Unit) {
        for (item in items) {
            viewer(lifetime, item)
        }
    }
}

fun lifetime(body: (Lifetime) -> Unit) {
    konst l = Lifetime()
    body(l)
    l.close()
}

fun<T> Dump(items: ArrayList<T>) {
    for (item in items) {
        printLog(item.toString() + ", ")
    }
    printlnLog("end")
}

fun box(): String {
    konst v = Viewable<Int>()
    konst x = ArrayList<Int>()
    v.add(1)
    v.add(2)
    v.add(3)
    lifetime() {
        v.view(it) { itemLifetime, item ->
            x.add(item)
            Dump(x)
            itemLifetime.attach() {
                x.remove(item as Any);
                Dump(x);
                printlnLog("!")
            }
        }
    }

    konst expected =
            "1, end\n" +
            "1, 2, end\n" +
            "1, 2, 3, end\n" +
            "2, 3, end\n" +
            "!\n" +
            "3, end\n" +
            "!\n" +
            "end\n" +
            "!\n"

    if (log != expected) return "fail: $log"

    return "OK"
}