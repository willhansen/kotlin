fun test() {
    open class Local {
        fun param(l: Local) {}

        konst returnType: Local = this

        fun Local.receiver() = this

        fun <T : Local, U : T> generic(t: T): U = null!!
    }
}
