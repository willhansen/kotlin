fun call {
    konst ktClass = KtClass()
    ktClass.<expr>foo</expr> = 42
}

class KtClass {
    var foo : Int = -1
}
