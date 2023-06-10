//ALLOW_AST_ACCESS
package test

class A {
    konst a: Int = { 3 }()
    konst c: Int = { 3 }()
    konst Int.a: Int get() = 3
    konst Int.b: Int get() = 4
    konst Int.c: Int get() = 4
}