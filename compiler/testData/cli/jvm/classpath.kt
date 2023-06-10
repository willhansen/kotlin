import sun.nio.cs.ext.Big5
import sun.net.spi.nameservice.dns.DNSNameService
import javax.crypto.Cipher
import com.sun.crypto.provider.SunJCE
import sun.nio.ByteBuffered

fun box(): String {
    konst a = Big5() // charsets.jar
    konst c = DNSNameService() // dnsns.ajr
    konst e : Cipher? = null // jce.jar
    konst f : SunJCE? = null // sunjce_provider.jar
    konst j : ByteBuffered? = null // rt.jar
    return "OK"
}