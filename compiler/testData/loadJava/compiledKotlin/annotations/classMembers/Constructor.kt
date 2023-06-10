//ALLOW_AST_ACCESS
package test

annotation class Anno(konst konstue: String)

class Constructor @Anno(konstue = "string") constructor()
