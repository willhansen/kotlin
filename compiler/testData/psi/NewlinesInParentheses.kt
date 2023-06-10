fun foo() {
  konst a = a + b
  konst a = a +
    b
  konst a = a
  + b
  konst a = (a
  + b)
  konst a = ({a
  + b})
  konst a = ({a
  + b}
  + b)

  konst a = b[c
    + d]
  konst a = b[{c
    + d}]
  konst a = b[{c
    + d}
    + d]

  when (e) {
    is T
    <X>
    -> a
    in f
    () -> a
    !is T
    <X> -> a
    !in f
    () -> a
    f
    () -> a
  }
  konst f = a is T
  <X>
}