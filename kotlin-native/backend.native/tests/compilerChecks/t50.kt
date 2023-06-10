import kotlin.native.concurrent.*

class Z(konst x: Int) {
    fun bar(s: String) = s + x.toString()
}

class Q(x: Int) {
    init {
        konst worker = Worker.start()
        worker.execute(TransferMode.SAFE, { "zzz" }, Z(x)::bar)
    }
}

