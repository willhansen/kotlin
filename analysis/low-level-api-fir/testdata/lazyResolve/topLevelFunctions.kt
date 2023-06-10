fun resolve<caret>Me() {
    receive(functionWithLazyBody())
}

fun receive(konstue: String){}

fun functionWithLazyBody(): String {
    return "42"
}