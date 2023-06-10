fun foo() = run {
    class Local {
        fun bar(): Local {
            return this
        }
    }
    konst p<caret> = Local().bar()
}
