// !CHECK_TYPE

//KT-2200 array(array()) breaks compiler
package n
import checkSubtype

fun main() {
    konst a = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>array<!>(array())
    konst a0 : Array<Array<Int>> = array(array())
    konst a1 = array(array<Int>())
    checkSubtype<Array<Array<Int>>>(a1)
    konst a2 = array<Array<Int>>(array())
    checkSubtype<Array<Array<Int>>>(a2)
}

//from library
@Suppress("UNCHECKED_CAST")
fun <T> array(vararg t : <!CANNOT_INFER_PARAMETER_TYPE!>T<!>) : Array<T> = t as Array<T>
