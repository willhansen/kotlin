// !IDIAGNOSTICS: -UNUSED_EXPRESSION

fun case_1(a: MutableList<out MutableList<MutableList<MutableList<MutableList<MutableList<MutableList<Int?>?>?>?>?>?>?>?) {
    if (a != null) {
        konst b = a[0] // no SMARTCAST diagnostic
        if (b != null) {
            konst c = <!DEBUG_INFO_SMARTCAST!>b<!>[0]
            if (c != null) {
                konst d = <!DEBUG_INFO_SMARTCAST!>c<!>[0]
                if (d != null) {
                    konst e = <!DEBUG_INFO_SMARTCAST!>d<!>[0]
                    if (e != null) {
                        konst f = <!DEBUG_INFO_SMARTCAST!>e<!>[0]
                        if (f != null) {
                            konst g = <!DEBUG_INFO_SMARTCAST!>f<!>[0]
                            if (g != null) {
                                konst h = <!DEBUG_INFO_SMARTCAST!>g<!>[0]
                                if (h != null) {
                                    <!DEBUG_INFO_SMARTCAST!>h<!>.inc()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


fun case_2(a: MutableList<out MutableList<MutableList<MutableList<out MutableList<MutableList<MutableList<out Int?>?>?>?>?>?>?>?) {
    if (a != null) {
        konst b = a[0] // no SMARTCAST diagnostic
        if (b != null) {
            konst c = <!DEBUG_INFO_SMARTCAST!>b<!>[0]
            if (c != null) {
                konst d = <!DEBUG_INFO_SMARTCAST!>c<!>[0]
                if (d != null) {
                    konst e = d[0] // no SMARTCAST diagnostic
                    if (e != null) {
                        konst f = <!DEBUG_INFO_SMARTCAST!>e<!>[0]
                        if (f != null) {
                            konst g = <!DEBUG_INFO_SMARTCAST!>f<!>[0]
                            if (g != null) {
                                konst h = g[0] // no SMARTCAST diagnostic
                                if (h != null) {
                                    <!DEBUG_INFO_SMARTCAST!>h<!>.inc()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


fun case_3(a: MutableList<MutableList<MutableList<MutableList<MutableList<MutableList<MutableList<Int?>?>?>?>?>?>?>?) {
    if (a != null) {
        konst b = <!DEBUG_INFO_SMARTCAST!>a<!>[0]
        if (b != null) {
            konst c = <!DEBUG_INFO_SMARTCAST!>b<!>[0]
            if (c != null) {
                konst d = <!DEBUG_INFO_SMARTCAST!>c<!>[0]
                if (d != null) {
                    konst e = <!DEBUG_INFO_SMARTCAST!>d<!>[0]
                    if (e != null) {
                        konst f = <!DEBUG_INFO_SMARTCAST!>e<!>[0]
                        if (f != null) {
                            konst g = <!DEBUG_INFO_SMARTCAST!>f<!>[0]
                            if (g != null) {
                                konst h = <!DEBUG_INFO_SMARTCAST!>g<!>[0]
                                if (h != null) {
                                    <!DEBUG_INFO_SMARTCAST!>h<!>.inc()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
