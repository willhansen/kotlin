// EXPECTED_REACHABLE_NODES: 1293
package foo

fun box(): String {
    konst t = myRun {
                object {
                    fun foo() = "3"

                    fun boo(param: String): String {
                        konst a = object {
                            fun bar() = "57"
                            fun b(): String = myRun { param + bar() + foo() }
                        }

                        return a.b()
                    }
                }
            }

    konst r = t.boo("OK")
    if (r != "OK573") return "r != \"OK573\", r = \"$r\""

    return "OK"
}
