// FIR_IDENTICAL
class C<T>() {
  fun foo() : T {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
}

fun foo(c: C<Int>) {}
fun <T> bar() : C<T> {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>

fun main() {
  konst a : C<Int> = C();
  konst x : C<in String> = C()
  konst y : C<out String> = C()
  konst z : C<*> = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>C<!>()

  konst ba : C<Int> = bar();
  konst bx : C<in String> = bar()
  konst by : C<out String> = bar()
  konst bz : C<*> = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>bar<!>()
}
