// FIR_IDENTICAL
konst ns: String? = null

konst testElvis1: String? = ns ?: ""
konst testElvis2: String = run { ns ?: "" }
konst testElvis3: String? = run { ns ?: "" }

konst testIf1: String? = if (true) "" else ""
konst testIf2: String? = run { if (true) "" else "" }
konst testIf3: String? = if (true) run { "" } else ""
konst testIf4: String? = run { run { if (true) "" else "" } }
konst testIf5: String? = run { if (true) run { "" } else "" }

konst testWhen1: String? = when { else -> "" }
konst testWhen2: String? = run { when { else -> "" } }
konst testWhen3: String? = when { else -> run { "" } }
konst testWhen4: String? = run { run { when { else -> "" } } }
konst testWhen5: String? = run { when { else -> run { "" } } }

konst testExcl1: String? = run { ns!! }
konst testExcl2: String? = run { run { ns!! } }