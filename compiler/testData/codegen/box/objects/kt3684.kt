open class X(private konst n: String) {

    fun foo(): String {
        return object : X("inner") {
            fun print(): String {
                return n;
            }
        }.print()
    }
}


fun box() : String {
  return X("OK").foo()
}

