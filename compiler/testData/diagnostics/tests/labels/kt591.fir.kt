//KT-591 Unresolved label in konstid code

fun test() {
    konst a: (Int?).() -> Unit = a@{
        if (this != null) {
            konst b: String.() -> Unit = {
                this@a.times(5) // a@ Unresolved
            }
        }
    }
}