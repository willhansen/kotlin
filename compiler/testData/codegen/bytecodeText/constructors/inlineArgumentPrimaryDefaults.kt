inline class A(konst x: Int)
class B(konst a: A = A(0))

// @B.class:
// 1 private <init>\(I\)V
// 1 public synthetic <init>\(IILkotlin/jvm/internal/DefaultConstructorMarker;\)V
// 1 public synthetic <init>\(ILkotlin/jvm/internal/DefaultConstructorMarker;\)V
// 0 private <init>\(\)V
