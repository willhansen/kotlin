// IGNORE_BACKEND: JS

open class A(konst a: String = DEFAULT_A){
    companion object: A(){
        const konst DEFAULT_A = "O"
    }
}

open class B(konst b: String = DEFAULT_B){
    companion object: B(){
        const konst DEFAULT_B = "K"
    }
}

fun box() = A.a + B().b