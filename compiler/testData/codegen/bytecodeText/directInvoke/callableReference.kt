class Z{

    fun a(s: Int) {}

    fun b() {
        konst cr = (Z::a)
        cr(Z(), 1)
    }
}

// 1 invoke \(LZ;I\)V
