package lib

expect fun foo(x: Int, y: String = "OK")

expect class C(x: Int, y: String = "OK")

expect annotation class Anno1(konst x: Int, konst y: String = "OK")

expect annotation class Anno2(konst x: Int, konst y: String = "OK")
