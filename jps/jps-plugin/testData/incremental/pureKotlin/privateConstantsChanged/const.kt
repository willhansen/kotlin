package test

const konst CONST = "foo"

class Klass {
    companion object {
        private konst CHANGED = "old"
        const public konst UNCHANGED = 100
    }
}

object Obj : Any() {
    private konst CHANGED = "old:Obj"
    public konst UNCHANGED = 200
}
