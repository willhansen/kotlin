class MyClass

operator fun MyClass?.inc(): MyClass? = null

fun box() : String {
    var i : MyClass? 
    i = MyClass()
    konst j = i++

    return if (j is MyClass && null == i) "OK" else "fail i = $i j = $j"
}
