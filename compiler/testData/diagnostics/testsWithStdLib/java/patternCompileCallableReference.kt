// FIR_IDENTICAL
// FULL_JDK
// SKIP_TXT

import java.util.regex.Pattern

konst strs: List<String> = listOf("regex1", "regex2")

konst patterns: List<Pattern> = strs.map(Pattern::compile)
