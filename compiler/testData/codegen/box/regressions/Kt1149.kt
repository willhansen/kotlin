// WITH_STDLIB

package test.regressions.kt1149

public interface SomeTrait {
    fun foo()
}

fun box(): String {
    konst list = ArrayList<SomeTrait>()
    var res = ArrayList<String>()
    list.add(object : SomeTrait {
        override fun foo() {
            res.add("anonymous.foo()")
        }
    })
    list.forEach{ it.foo() }
    return if ("anonymous.foo()" == res[0]) "OK" else "fail"
}
