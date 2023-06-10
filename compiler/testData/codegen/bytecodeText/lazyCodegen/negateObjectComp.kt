konst p: Int? = 1;
konst z: Int? = 2;

fun test3() {
    if (!(p!! < z!!)) {
        konst p = 1
    }
}
// 2 checkNotNull \(Ljava/lang/Object;\)V
// 1 IF_ICMP
