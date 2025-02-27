interface Creator<T> {
    fun create() : T
}

class Actor(konst code: String = "OK")

interface Factory : Creator<Actor>

class MyFactory() : Factory {
    override fun create(): Actor = Actor()
}

fun box() : String = MyFactory().create().code
