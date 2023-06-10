// FILE: J.java
import java.util.*;

public class J {
    public static String s = null;
    public static Map<String, String> m = null;
}

// FILE: k.kt

konst testImplicitExclExcl1: String = J.s
konst testImplicitExclExcl2: String? = J.s

konst testImplicitExclExcl3: String = <!TYPE_MISMATCH!>J.m[""]<!>
konst testImplicitExclExcl4: String? = J.m[""]

konst testExclExcl1: String = J.s!!
konst testExclExcl2: String? = J.s!!

konst testExclExcl3: String = J.m[""]!!
konst testExclExcl4: String? = J.m[""]!!

konst testSafeCall1: String = <!TYPE_MISMATCH!>J.s?.let { it }<!>
konst testSafeCall2: String? = J.s?.let { it }

konst testSafeCall3: String = <!TYPE_MISMATCH!>J.m[""]?.let { it }<!>
konst testSafeCall4: String? = J.m[""]?.let { it.toString() }

konst testIf1: String = if (true) J.s else J.s
konst testIf2: String? = if (true) J.s else J.s

konst testIf3: String = if (true) <!TYPE_MISMATCH!>J.m[""]<!> else <!TYPE_MISMATCH!>J.m[""]<!>
konst testIf4: String? = if (true) J.m[""] else J.m[""]

konst testWhen1: String = when { else -> J.s }
konst testWhen2: String? = when { else -> J.s }

konst testWhen3: String = when { else -> <!TYPE_MISMATCH!>J.m[""]<!> }
konst testWhen4: String? = when { else -> J.m[""] }
