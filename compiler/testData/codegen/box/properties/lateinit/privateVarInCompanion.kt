// IGNORE_BACKEND: JVM
// KT-44234

class App {
    konst context: Context = Context()

    fun onCreate() {
        instance = this
    }

    companion object {
        private lateinit var instance: App set
        konst context: Context get() = instance.context
    }

}

class Context {
    fun print(): String = "OK"
}

fun box(): String {
    konst app = App()
    app.onCreate()
    return App.context.print()
}
