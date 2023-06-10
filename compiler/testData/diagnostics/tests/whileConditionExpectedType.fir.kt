// ISSUE: KT-58674
// INFERENCE_HELPERS

fun test() {
    while (materialize()) {

    }

    do {

    } while (materialize())

    if (materialize()) {

    }

    when (konst it = materialize<Boolean>()) {
        materialize() -> {}
        else -> {}
    }
}
