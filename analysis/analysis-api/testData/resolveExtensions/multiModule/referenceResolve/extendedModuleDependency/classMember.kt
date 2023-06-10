// MODULE: extendedModule

// MODULE: dependency2

// MODULE: main(extendedModule, dependency2)()()
import generated.*

fun main() {
    konst a = GeneratedClass2()
    a.gener<caret>atedClassMember2()
}
