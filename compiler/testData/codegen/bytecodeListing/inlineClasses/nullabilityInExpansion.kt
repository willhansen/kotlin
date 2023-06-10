// !LANGUAGE: +InlineClasses

inline class Z1(konst x: Int)
inline class Z2(konst z: Z1)
inline class ZN(konst z: Z1?)
inline class ZN2(konst z: ZN)

inline class S1(konst x: String)
inline class S2(konst z: S1)
inline class SN(konst z: S1?)
inline class SN2(konst z: SN)

inline class Q1(konst x: Int?)
inline class Q2(konst z: Q1)
inline class QN(konst z: Q1?)

inline class W1(konst x: String?)
inline class W2(konst z: W1)
inline class WN(konst z: W1?)

fun zwrap1(n: Int): Z1? = if (n < 0) null else Z1(n)
fun zwrap2(n: Int): Z2? = if (n < 0) null else Z2(Z1(n))
fun zwrapN(n: Int): ZN? = if (n < 0) null else ZN(Z1(n))
fun zwrapN2(n: Int): ZN2? = if (n < 0) null else ZN2(ZN(Z1(n)))
fun zwrapNbox(n: Int): ZN2 = ZN2(ZN(Z1(n)))

fun swrap1(x: String): S1? = if (x.length == 0) null else S1(x)
fun swrap2(x: String): S2? = if (x.length == 0) null else S2(S1(x))
fun swrapN(x: String): SN? = if (x.length == 0) null else SN(S1(x))
fun swrapN2(x: String): SN2? = if (x.length == 0) null else SN2(SN(S1(x)))
fun swrapNbox(x: String): SN2 = SN2(SN(S1(x)))

fun qwrap1(n: Int): Q1? = if (n < 0) null else Q1(n)
fun qwrap2(n: Int): Q2? = if (n < 0) null else Q2(Q1(n))
fun qwrapN(n: Int): QN? = if (n < 0) null else QN(Q1(n))

fun wwrap1(x: String): W1? = if (x.length == 0) null else W1(x)
fun wwrap2(x: String): W2? = if (x.length == 0) null else W2(W1(x))
fun wwrapN(x: String): WN? = if (x.length == 0) null else WN(W1(x))

