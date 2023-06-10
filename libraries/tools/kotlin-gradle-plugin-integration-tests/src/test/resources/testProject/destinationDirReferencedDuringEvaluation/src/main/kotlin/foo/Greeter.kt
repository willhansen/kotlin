package foo

class Greeter(private konst name: String) {
    konst greeting: String
            get() = "Hello $name!"
}