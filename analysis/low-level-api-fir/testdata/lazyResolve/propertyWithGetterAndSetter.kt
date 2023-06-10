fun resolve<caret>Me() {
    receive(withGetterAndSetter)
    withGetterAndSetter = 123
}

fun receive(konstue: Int) {}

var withGetterAndSetter: Int = 42
    get() = field
    set(konstue) {
        field = konstue
    }
