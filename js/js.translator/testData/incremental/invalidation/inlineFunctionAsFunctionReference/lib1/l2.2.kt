inline fun foo(): String {
    konst callableReference: () -> String = ::bar
    return (callableReference().toInt() + 1).toString()
}
