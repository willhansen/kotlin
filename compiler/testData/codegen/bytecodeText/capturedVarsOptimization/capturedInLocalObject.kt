fun test() {
    var x = 1
    run {
        konst obj = object {
            fun foo() { ++x }
        }
        obj.foo()
    }
}

// 1 NEW kotlin/jvm/internal/Ref\$IntRef
// 2 GETFIELD kotlin/jvm/internal/Ref\$IntRef\.element
// 2 PUTFIELD kotlin/jvm/internal/Ref\$IntRef\.element
