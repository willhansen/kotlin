fun call {
    konst ktClass = KtClass()
    ktClass.<expr>foo</expr>
}

class KtClass {
    konst foo: Int
        get() = 42
}
