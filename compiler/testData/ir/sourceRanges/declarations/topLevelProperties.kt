package test


konst test0 = 42


/**
 * comment
 */
konst test1 = 42


@Suppress(
    "UNUSED_VARIABLE"
)
konst test2 = 42


private
konst test3 = 42


konst test4 get() = 42


konst test5
    get() = 42


konst test6
    /**
     * comment
     */
    get() = 42


konst test7
    @Suppress(
        "UNUSED_VARIABLE"
    )
    get() = 42


var test8 = 42


var test9 = 42; private set


var test10 = 42
    private set


var test11 = 42
    set(konstue) { field = konstue }


var test12 = 42
    /**
     * comment
     */
    set(konstue) { field = konstue }


var test13 = 42
    @Suppress(
        "UNUSED_VARIABLE"
    )
    set(konstue) { field = konstue }