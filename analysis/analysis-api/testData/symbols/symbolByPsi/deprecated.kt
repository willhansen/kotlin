// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE_K1
// WITH_STDLIB

@Deprecated("don't use i")
konst i: Int = 1

@get:Deprecated("don't use getter of i2")
konst i2: Int = 1

@set:Deprecated("don't use getter of i3")
var i3: Int = 1

@get:Deprecated("don't use getter of i4")
@set:Deprecated("don't use setter of i4")
var i4: Int = 1

@Deprecated("don't use f")
fun f(): Int = 1

@Deprecated("don't use MyClass")
class MyClass

class Foo {
    @Deprecated("don't use i2")
    konst i2: Int = 1

    @Deprecated("don't use f2")
    fun f2(): Int = 1
}

@Deprecated("don't use j", level = DeprecationLevel.ERROR)
konst j: Int = 2

@Deprecated("don't use j2", level = DeprecationLevel.HIDDEN)
konst j2: Int = 2

@java.lang.Deprecated
konst j3: Int = 2
