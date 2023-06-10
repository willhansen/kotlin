fun call(f: () -> Unit) {
    f()
}

enum class E(konst f: () -> String) {
    A({
          var konstue = "Fail"
          call {
              konstue = "OK"
          }
          konstue
    })
}

fun box(): String {
    return E.A.f()
}
