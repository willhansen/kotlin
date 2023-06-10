fun main() {

    konst localVariable = 0

    class LocalClass(konst arg: Int) {
        constructor() : this(localVariable)
    }

    LocalClass().arg
}
