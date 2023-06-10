// SKIP_KLIB_TEST
// FIR_IDENTICAL
// TARGET_BACKEND: JVM_IR
import java.util.Date

konst unitFun = { }
konst intFun = { 42 }
konst stringParamFun = { x: String -> }
konst listFun = { l: List<String> -> l }
konst mutableListFun = fun (l: MutableList<Double>): MutableList<Int> = null!!
konst funWithIn = fun (x: Comparable<String>) {}

konst extensionFun = fun Any.() {}
konst extensionWithArgFun = fun Long.(x: Any): Date = Date()
