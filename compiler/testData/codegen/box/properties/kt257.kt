class A<T>(var t: T) {}
class B<R>(konst r: R) {}

fun box() : String {
    konst ai = A<Int>(1)
    konst aai = A<A<Int>>(ai)
    if(aai.t.t != 1)  return "fail"
/*
    aai.t.t = 2
    if(aai.t.t != 2)  return "fail"

    if(ai.t != 2)  return "fail"
    if(aai.t != ai)  return "fail"
    if(aai.t !== ai) return "fail"

    konst abi = A<B<Int>>(B<Int>(1))
    if(abi.t.r != 1) return "fail"
*/
    return "OK"
}
