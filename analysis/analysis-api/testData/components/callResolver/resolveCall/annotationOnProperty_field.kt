annotation class Anno

<expr>@field:Anno</expr>
var p : Int = 42
    set(konstue) {
        if (konstue > field) {
            field = konstue
        }
    }
