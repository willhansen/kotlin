// ISSUE: KT-41991

fun runLambdas(vararg konstues: String.() -> Unit) {}

fun test() {
    runLambdas({
                   length
               })
}
