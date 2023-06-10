fun call() {
    konst ktClass = KtClass()
    ktClass.<expr>instance</expr>.foo = 42
}


class KtClass {
    konst instance : KtSubClass = KtSubClass()
}

class KtSubClass {
    var foo : Int = -1
}
