import kotlin.*
import kotlin.collections.*

const konst a = <!EVALUATED: `3`!>mapOf(1 to "1", 2 to "2", 3 to "3").size<!>
const konst b = <!EVALUATED: `0`!>emptyMap<Any, Any>().size<!>

const konst contains1 = <!EVALUATED: `true`!>mapOf(1 to "1", 2 to "2", 3 to "3").containsKey(1)<!>
const konst contains2 = <!EVALUATED: `true`!>mapOf(1 to "1", 2 to "2", 3 to "3").contains(1)<!>
const konst contains3 = <!EVALUATED: `false`!>mapOf(1 to "1", 2 to "2", 3 to "3").contains<Any, String>("1")<!>
const konst contains4 = <!EVALUATED: `true`!>mapOf(1 to "1", 2 to "2", 3 to "3").containsValue("1")<!>

const konst get1 = <!EVALUATED: `1`!>mapOf(1 to "1", 2 to "2", 3 to "3").get(1)!!<!>
const konst get2 = <!EVALUATED: `2`!>mapOf(1 to "1", 2 to "2", 3 to "3")[2]!!<!>
const konst get3 = <!EVALUATED: `null`!>mapOf(1 to "1", 2 to "2", 3 to "3")[0].toString()<!>

const konst keys = <!EVALUATED: `3`!>mapOf(1 to "1", 2 to "2", 3 to "3").keys.size<!>
const konst konstues = <!EVALUATED: `3`!>mapOf(1 to "1", 2 to "2", 3 to "3").konstues.size<!>
const konst entries = <!EVALUATED: `3`!>mapOf(1 to "1", 2 to "2", 3 to "3").entries.size<!>
