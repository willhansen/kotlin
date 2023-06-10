fun testImplicitCast(a: Any) {
    if (a !is String) return

    konst t: String = try { a } catch (e: Throwable) { "" }
}