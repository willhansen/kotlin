class C {
    public object Obj {
        konst o = "O"

        object InnerObj {
          fun k() = "K"
        }

        class D {
            konst ko = "KO"
        }
    }
}

fun box(): String {
    konst res = C.Obj.o + C.Obj.InnerObj.k()  + C.Obj.D().ko
    return if (res == "OKKO") "OK" else "Fail: $res"
}
