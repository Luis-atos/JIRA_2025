import java.net.InetAddress
import org.apache.log4j.Logger

def log = Logger.getLogger(getClass())

// Obtener el nombre del host (máquina)
def hostname = InetAddress.localHost.hostName

// Mostrar el nombre del host
log.warn "Este script fue ejecutado desde la máquina: ${hostname}"
