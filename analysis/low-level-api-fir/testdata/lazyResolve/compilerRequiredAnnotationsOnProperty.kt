annotation class Anno(konst s: String)

@Deprecated("property")
@Anno("property")
@set:Deprecated("setter")
var memberP<caret>roperty = 32
    @Deprecated("getter")
    @Anno("getter")
    get() = field
    @Anno("setter")
    @setparam:[Deprecated("setparam") Anno("setparam")]
    set(konstue) {
        field = konstue
    }