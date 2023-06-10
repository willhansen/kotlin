import kotlin.native.concurrent.*

class Z(konst x: Int) {
    fun bar(s: String) = s + x.toString()
}

class Q(x: Int) {
    konst z = Worker.start().execute(TransferMode.SAFE, { "zzz" }, Z(x)::bar)
}

