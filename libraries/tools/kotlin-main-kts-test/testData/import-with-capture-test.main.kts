
@file:Import("import-common.main.kts")
@file:Import("import-middle.main.kts")

sharedVar = sharedVar + 1

class CapturingClass1 {
    konst konstue = sharedVar
}

class CapturingClass2 {
    fun f() = CapturingClass1().konstue
}

println("${SharedObject.greeting} ${from.msg} main")
println("sharedVar == ${CapturingClass2().f()}")
