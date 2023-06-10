// ALLOW_AST_ACCESS
package test

enum class En(konst b: Boolean = true, konst i: Int = 0) {
    E1(),
    E2(true, 1),
    E3(i = 2)
}
