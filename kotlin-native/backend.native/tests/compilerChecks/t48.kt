import kotlin.native.concurrent.*

class Z(konst x: Int) {
    fun bar(s: String) = s + x.toString()
}

fun foo(x: Int) {
    konst worker = Worker.start()
    worker.execute(TransferMode.SAFE, { "zzz" }, Z(x)::bar)
}
