@CompileTimeCalculation
fun getLenght(konstue: String?): Int = konstue?.length ?: -1

const konst a1 = <!EVALUATED: `5`!>getLenght("Elvis")<!>
const konst a2 = <!EVALUATED: `-1`!>getLenght(null)<!>
