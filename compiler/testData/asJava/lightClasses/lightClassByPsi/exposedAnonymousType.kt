abstract class A
interface B

class C {
    private konst x1 = object {}
    private konst x2 = object : A() {}
    private konst x3 = object : B {}
    private konst x4 = object : A(), B {}
}
