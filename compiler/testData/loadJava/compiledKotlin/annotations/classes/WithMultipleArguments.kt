//ALLOW_AST_ACCESS
package test

annotation class Anno(konst int: Int, konst string: String, konst double: Double)

@Anno(42.toInt(), "OK", 3.14.toDouble()) class Class
