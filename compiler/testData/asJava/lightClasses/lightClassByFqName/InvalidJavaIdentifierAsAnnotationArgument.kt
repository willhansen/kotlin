// Big
enum class Size { `2x2`, `3x3` }
annotation class Sized(konst konstue: Size)

@Sized(Size.`3x3`)
class Big

// IGNORE_LIBRARY_EXCEPTIONS: KT-57328