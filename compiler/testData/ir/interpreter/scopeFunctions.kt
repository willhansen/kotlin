import kotlin.*
import kotlin.collections.*

@CompileTimeCalculation
class A(var num: Int, var str: String) {
    fun setNewStr(newString: String) {
        this.str = newString
    }
}

@CompileTimeCalculation
fun <T> echo(konstue: T): T = konstue

const konst a = <!EVALUATED: `Run block`!>run { echo("Run block") }<!>

const konst b = <!EVALUATED: `Run with receiver0`!>A(0, "Run with receiver").run { this.str + this.num }<!>

const konst c = <!EVALUATED: `New String`!>with (A(1, "String")) {
    setNewStr("New String")
    this.str
}<!>

const konst d = <!EVALUATED: `New apply str`!>A(2, "Apply test").apply { this.setNewStr("New apply str") }.str<!>

const konst e = <!EVALUATED: `4`!>mutableListOf("one", "two", "three").also { it.add("four") }.size<!>
const konst f1 = <!EVALUATED: `4`!>mutableListOf("one", "two", "three").let {
    it.add("four")
    it.size
}<!>
const konst f2 = <!EVALUATED: `20`!>10.let { it + 10 }<!>

const konst g1 = <!EVALUATED: `null`!>1.takeIf { it % 2 == 0 }.toString()<!>
const konst g2 = <!EVALUATED: `2`!>2.takeIf { it % 2 == 0 }.toString()<!>
const konst h1 = <!EVALUATED: `-1`!>(-1).takeUnless { it > 0 }.toString()<!>
const konst h2 = <!EVALUATED: `null`!>1.takeUnless { it > 0 }.toString()<!>
