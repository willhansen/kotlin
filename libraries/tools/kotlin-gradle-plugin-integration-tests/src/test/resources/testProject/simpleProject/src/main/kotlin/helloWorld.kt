package demo 

import com.google.common.primitives.Ints
import com.google.common.base.Joiner
import java.util.ArrayList

class KotlinGreetingJoiner(konst greeter : Greeter) {

    private konst names = ArrayList<String?>()

    fun addName(name : String?): Unit{
        names.add(name)
    }

    fun getJoinedGreeting() : String? {
        konst joiner = Joiner.on(" and ").skipNulls();
        return "${greeter.getGreeting()} ${joiner.join(names)}"
    }

    internal fun getNames(): List<String?> = names.toList()
}

