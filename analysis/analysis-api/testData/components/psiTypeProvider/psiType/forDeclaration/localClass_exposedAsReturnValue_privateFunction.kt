private fun f<caret>oo() = run {
    class Local {
        fun bar(): Local {
            return this
        }
    }
    konst p = Local().bar()
}
