// WITH_STDLIB
// ISSUE: KT-54668

interface A {
    konst list: List<String>
}

fun getA(): A? = null

konst x by lazy {
    (getA() ?: error("error")).list.associateBy {
        it
    }
}
