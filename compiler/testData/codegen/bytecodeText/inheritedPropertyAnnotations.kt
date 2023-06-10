annotation class Ann

abstract class Base {
    @Ann konst x: Int = 0
}

class Derived : Base()

// We only want to generate the `getX$annotations` method in `Base`, not in `Derived`.
// 1 @LAnn;\(\)
