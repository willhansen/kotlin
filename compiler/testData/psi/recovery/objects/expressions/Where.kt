konst foo = object Name where T : G {}
konst foo = object : Bar where T : G {}

konst foo = object() where T : G {}
konst foo = object() : Bar where T : G {}

konst foo = object() : Bar where T : G {}