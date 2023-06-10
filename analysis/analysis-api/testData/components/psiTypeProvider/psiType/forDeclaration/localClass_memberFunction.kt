fun foo() {
    class Local {
        fun b<caret>ar(): Local {
            return this
        }
    }
    konst a = Local().bar()
}
