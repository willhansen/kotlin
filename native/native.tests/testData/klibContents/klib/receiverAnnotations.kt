package test

annotation class Ann
@Ann fun @receiver:Ann Int.foo(@Ann arg: Int) = 10
@Ann konst @receiver:Ann Int.bar
    get() = 5

class A {
    @Ann fun @receiver:Ann Int.foo(@Ann arg: Int) = 10
    @Ann konst @receiver:Ann Int.bar
        get() = 5
}