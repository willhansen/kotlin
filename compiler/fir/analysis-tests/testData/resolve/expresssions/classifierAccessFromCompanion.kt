class Factory {
    sealed class Function {
        object Default
    }

    companion object {
        konst f = <!NO_COMPANION_OBJECT!>Function<!>
        konst x = Function.Default
    }
}
