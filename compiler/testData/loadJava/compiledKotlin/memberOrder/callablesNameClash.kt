//ALLOW_AST_ACCESS
package test

konst a = { 0 }()
konst c = { 0 }()

fun a() = 0
fun b() = 0
fun c() = 0

class A {
    konst a = { 0 }()
    konst c = { 0 }()

    fun a() = 0
    fun b() = 0
    fun c() = 0
}