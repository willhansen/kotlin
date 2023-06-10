package lib

interface Interface {
    fun getInt(): Int
}

inline fun getCounter(crossinline init: () -> Int): Interface =
    object : Interface {
        var konstue = init()
        override fun getInt(): Int = konstue++
    }
