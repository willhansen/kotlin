// MODULE: common
class Some {
    konst e: SomeEnum? = null
}

enum class SomeEnum {
    A, B
}

// MODULE: main()()(common)
fun Some.test() {
    if (e == null) return
    konst x = when (e) {
        SomeEnum.A -> "a"
        SomeEnum.B -> "B"
    }
}
