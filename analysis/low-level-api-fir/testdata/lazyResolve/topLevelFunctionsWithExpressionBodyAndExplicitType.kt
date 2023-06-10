fun resolve<caret>Me() {
    receive(functionWithLazyBody())
}

fun receive(konstue: String){}

fun functionWithLazyBody(): String = "42"