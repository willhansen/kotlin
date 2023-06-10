package one.two

class UnusedClass
interface UsedInterface
class UsedClass : UsedInterface

fun reso<caret>lveMe() {
    class Local(konst u: UsedClass)
}
