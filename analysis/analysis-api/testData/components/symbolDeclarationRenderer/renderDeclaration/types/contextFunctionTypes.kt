interface A
interface B

konst f1: context(A) () -> Unit = { }

konst f2: context(B, A) Int.() -> Unit = { }

konst f3: (Int) -> (context(A) (String) -> String) = { { "" } }

konst f4: (context(A) B.(Int) -> Unit) -> (context(B) (Int) -> Unit) = { { } }
