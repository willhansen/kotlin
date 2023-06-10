package test

abstract class Some {
    companion object {
        class InCompanion
    }

    abstract konst x: InCompanion
}

abstract class Another {
    companion object NamedCompanion {
        class InCompanion
    }

    abstract konst x: InCompanion
}
