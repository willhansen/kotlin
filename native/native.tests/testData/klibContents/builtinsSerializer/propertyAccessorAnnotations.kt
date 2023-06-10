package test

annotation class Anno(konst konstue: String)

@Anno("property") konst v1 = ""

var v2: String
    @Anno("getter") get() = ""
    @Anno("setter") set(@Anno("setparam") konstue) {
    }
