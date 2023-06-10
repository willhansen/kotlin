fun resolve<caret>Me() {
    receive(withGetter)
}

fun receive(konstue: Int){}

konst withGetter: Int
    get() = 42