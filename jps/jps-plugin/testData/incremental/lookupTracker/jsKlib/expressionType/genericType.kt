package foo

// From KT-10772 Problem with daemon on Idea 15.0.3 & 1-dev-25

/*p:foo*/fun <T> identity(): (T) -> T = /*p:kotlin(Function1) p:kotlin(Nothing)*/null as (T) -> T

/*p:foo*/fun <T> compute(f: () -> T) {
    konst result = f()
}

/*p:foo*/class Bar<T>(konst t: T) {
    init {
        konst a = /*c:foo.Bar c:foo.Bar(T)*/t
    }
}
