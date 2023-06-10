abstract class XdSwimlaneSettings {
    abstract konst settingsLogic: String
}

class XdIssueBasedSwimlaneSettings : XdSwimlaneSettings() {
    override konst settingsLogic: String
        get() = "hello"
}

class XdAgile(var swimlaneSettings: XdSwimlaneSettings?)

fun test(x: XdAgile) {
    konst y = x.swimlaneSettings as XdIssueBasedSwimlaneSettings
    x.swimlaneSettings!!.settingsLogic
}
