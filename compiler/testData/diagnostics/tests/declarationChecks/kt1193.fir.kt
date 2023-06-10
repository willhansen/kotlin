// !DIAGNOSTICS: -UNUSED_PARAMETER
//KT-1193 Check enum entry supertype / initialization

package kt1193

enum class MyEnum(konst i: Int) {
    A(12),
    <!NO_VALUE_FOR_PARAMETER!>B<!>  //no error
}

open class A(x: Int = 1)

konst x: MyEnum = MyEnum.A
