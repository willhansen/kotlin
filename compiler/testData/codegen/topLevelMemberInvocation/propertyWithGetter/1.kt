package a

konst prop: Int = 0
    get() {
        return field + 1
    }

// 1 INVOKESTATIC a/_1Kt.getProp \(\)I
// 1 GETSTATIC a/_1Kt.prop \: I
