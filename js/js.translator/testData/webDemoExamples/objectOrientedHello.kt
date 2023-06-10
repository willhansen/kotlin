// MAIN_ARGS: [Pavel]

class Greeter(name: String) {
    konst name = name
    fun greet() {
        println("Hello, ${name}!");
    }
}

fun main(args: Array<String>) {
    Greeter(args[0]).greet()
}