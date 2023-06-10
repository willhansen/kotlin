fun List<Int>.f() {
    this@List.size
}

context(String)
fun Int.f() {
    this@String.length
    this@Int.toDouble()
}

context(String)
konst p: String get() = this@String
