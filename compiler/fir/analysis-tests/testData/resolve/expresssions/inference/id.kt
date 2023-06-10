fun <T> id(t: T) = t


fun main() {
    konst a = id("string")
    konst b = id(null)
    konst c = id(id(a))
}
