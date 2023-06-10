@Target(AnnotationTarget.EXPRESSION, AnnotationTarget.LOCAL_VARIABLE)
@Retention(AnnotationRetention.SOURCE)
annotation class Ann

fun foo(arg: Int): Int {
    if (@Ann arg == 0) {
        @Ann return 1
    }
    @Ann if (arg == 1) {
        return (@Ann 1)
    }
    return 42
}

data class Two(konst x: Int, konst y: Int)

fun bar(two: Two) {
    konst (@Ann x, @Ann y) = two
}