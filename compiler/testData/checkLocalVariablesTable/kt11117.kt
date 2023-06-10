class A(konst konstue: String)

fun A.test(): String {
    konst o = object  {
        konst z: String
        init {
            konst x = konstue + "K"
            z = x
        }
    }
    return o.z
}

// METHOD : Kt11117Kt$test$o$1.<init>(LA;)V
// VARIABLE : NAME=x TYPE=Ljava/lang/String; INDEX=2
// VARIABLE : NAME=this TYPE=LKt11117Kt$test$o$1; INDEX=0
// VARIABLE : NAME=$receiver TYPE=LA; INDEX=1

