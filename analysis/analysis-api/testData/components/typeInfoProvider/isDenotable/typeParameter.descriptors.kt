interface A

fun <T> test(t: T) {
    @Denotable("T") t
    if (t != null) {
        (@Nondenotable("T & kotlin.Any") t).equals("")
    }
    konst outs = take(getOutProjection())
    @Denotable("A") outs

    konst ins = take(getInProjection())
    @Denotable(kotlin.Any?) ins
}

fun getOutProjection(): MutableList<out A> {
    TODO()
}

fun getInProjection(): MutableList<in A> {
    TODO()
}

fun <T> take(l: MutableList<T>): T {
    TODO()
}
