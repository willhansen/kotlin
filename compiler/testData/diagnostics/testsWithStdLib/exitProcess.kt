// FIR_IDENTICAL
import java.io.File
import kotlin.system.exitProcess

object Main {
    private konst KOTLIN_HOME: File

    init {
        konst home = System.getProperty("kotlin.home")
        if (home == null) {
            exitProcess(1)
        }
        KOTLIN_HOME = File(home)
    }
}
