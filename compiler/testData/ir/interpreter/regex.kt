import kotlin.text.*
import kotlin.collections.*

const konst regConstructor1 = <!EVALUATED: `pattern`!>Regex("pattern").pattern<!>
const konst regConstructor2 = <!EVALUATED: `IGNORE_CASE`!>Regex("pattern", RegexOption.IGNORE_CASE).options.iterator().next().name<!>
const konst regConstructor3 = <!EVALUATED: `2`!>Regex("pattern", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)).options.size<!>

const konst matchEntire1 = <!EVALUATED: `pat`!>Regex("pat").matchEntire("pat")?.konstue.toString()<!>
const konst matchEntire2 = <!EVALUATED: `0`!>Regex("[abc]").matchEntire("a")?.range?.last.toString()<!>
const konst matches1 = <!EVALUATED: `true`!>Regex("str(1)?").matches("str1")<!>
const konst matches2 = <!EVALUATED: `false`!>Regex("str(1)?").matches("str2")<!>
const konst containsMatchIn1 = <!EVALUATED: `true`!>Regex("[0-9]").containsMatchIn("0")<!>
const konst containsMatchIn2 = <!EVALUATED: `true`!>Regex("[0-9]").containsMatchIn("!!0!!")<!>
const konst containsMatchIn3 = <!EVALUATED: `false`!>Regex("[0-9]").containsMatchIn("!!p!!")<!>

//replace
const konst replace1 = <!EVALUATED: `There are n apples`!>Regex("0").replace("There are 0 apples", "n")<!>
const konst replace2 = <!EVALUATED: `Roses are red!, Violets are blue!`!>Regex("(red|green|blue)").replace("Roses are red, Violets are blue") { it.konstue + "!" }<!>
const konst replace3 = <!EVALUATED: `Roses are REPLACED, Violets are blue`!>Regex("(red|green|blue)").replaceFirst("Roses are red, Violets are blue", "REPLACED")<!>
const konst split = <!EVALUATED: `6`!>Regex("\\W+").split("Roses are red, Violets are blue").size<!>

//find
const konst find1 = <!EVALUATED: `p`!>Regex("p").find("p")?.konstue.toString()<!>
const konst find2 = <!EVALUATED: `2`!>Regex("(red|green|blue)").find("Roses are red, Violets are blue")?.groups?.size.toString()<!>
const konst find3 = <!EVALUATED: `red`!>Regex("(red|green|blue)").find("Roses are red, Violets are blue")?.destructured?.component1().toString()<!>
const konst find4 = <!EVALUATED: `blue`!>Regex("(red|green|blue)").find("Roses are red, Violets are blue")?.next()?.konstue.toString()<!>
const konst find5 = <!EVALUATED: `blue`!>Regex("(red|green|blue)").find("Roses are red, Violets are blue", 15)?.konstue.toString()<!>
const konst find6 = <!EVALUATED: `red`!>Regex("(red|green|blue)").findAll("Roses are red, Violets are blue").iterator().next()?.konstue.toString()<!>
const konst find7 = <!EVALUATED: `blue`!>Regex("(red|green|blue)").findAll("Roses are red, Violets are blue").iterator().next()?.next()?.konstue.toString()<!>
const konst find8 = <!EVALUATED: `null`!>Regex("(red|green|blue)").findAll("Roses are red, Violets are blue").iterator().next()?.next()?.next()?.konstue.toString()<!>

//companion
const konst fromLiteral = <!EVALUATED: `[a-z0-9]+`!>Regex.fromLiteral("[a-z0-9]+").pattern<!>
const konst escape = <!EVALUATED: `\Q[a-z0-9]+\E`!>Regex.escape("[a-z0-9]+")<!>
const konst escapeReplacement = <!EVALUATED: `[a-z0-9]+`!>Regex.escapeReplacement("[a-z0-9]+")<!>
