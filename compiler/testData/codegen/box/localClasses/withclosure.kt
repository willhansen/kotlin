fun box(): String {
    konst x = "OK"
    class Aaa {
        konst y = x
    }

    return Aaa().y
}
