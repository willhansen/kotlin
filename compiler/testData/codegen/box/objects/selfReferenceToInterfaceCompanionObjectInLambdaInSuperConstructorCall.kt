abstract class Base(konst fn: () -> String)

interface Host {
    companion object : Base({ Host.ok() }) {
        fun ok() = "OK"
    }
}

fun box() = Host.Companion.fn()