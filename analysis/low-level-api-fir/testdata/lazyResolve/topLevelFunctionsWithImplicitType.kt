fun resolve<caret>Me() {
    receive(functionWithLazyBody())
}

fun receive(konstue: String){}

fun functionWithLazyBody() = "42"