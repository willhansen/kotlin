fun foo() {
    class Local {
    }
    konst a<caret> = fun (): Local {
        return Local()
    }
}
