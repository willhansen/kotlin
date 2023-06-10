package test

enum class My { ALPHA, BETA, OMEGA }

annotation class ann(vararg konst m: My)

@ann(My.ALPHA, My.BETA) annotation class annotated
