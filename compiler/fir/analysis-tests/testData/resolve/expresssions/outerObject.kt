 object Outer {
     konst x = 0
     fun Nested.foo() {}
     class Nested {
         konst y = x
         fun test() {
             foo()
         }
     }
 }
