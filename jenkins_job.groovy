import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import com.atlassian.jira.component.ComponentAccessor
import com.opensymphony.workflow.InvalidInputException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Obtener información de la issue
def jiraIssue = ComponentAccessor.issueManager.getIssueObject(issue.id)
def issueKey = jiraIssue.key
def issueSummary = jiraIssue.summary
def reporter = jiraIssue.reporter?.displayName ?: "Unknown"
def status = jiraIssue.status.name
def jobName = "Desconocido"

// Extraer el nombre del job desde el resumen de la issue
// def matcher = (issueSummary =~ /#(.*?)#/)
// if (matcher.find()) {
//     jobName = matcher.group(1) // Extrae el primer grupo capturado
//     log.info "? El nombre del job extraído es: ${jobName}"
// } else {
//     log.error "? No se pudo extraer el nombre del job del resumen: ${issueSummary}"
//     throw new InvalidInputException("No se pudo determinar el nombre del job de Jenkins. Verifique el resumen de la issue.")
// }

jobName = getRepositoryName(jiraIssue)

def getRepositoryName(issue) {
    // Lógica para obtener el nombre del repositorio de un campo personalizado o del resumen
    // Por ejemplo, leer un campo personalizado 'Repositorio' o extraerlo del summary
    def repoField = issue.getCustomFieldValue(13500) ?: issue.summary
    return repoField?.toString().trim()
}

// Configuración de Jenkins
def jenkinsUrl = "http://srvcceacml77:8080/"        // URL de Jenkins
def jenkinsPath = "job/${jobName}/job/"             // Nombre del Job en Jenkins
def branch = "develop"                              // Nombre de la rama
def user = "jfajram"                                // Usuario de Jenkins
def token = "118cc81dfb0b44f8be4b448cbc47c2de1c"    // Token API de Jenkins

// Parámetros para pasar a Jenkins
def params = [
    ISSUE_KEY: issueKey,
    ISSUE_SUMMARY: issueSummary,
    REPORTER: reporter,
    STATUS: status
]

// Construir la URL con parámetros codificados
def buildUrl = "${jenkinsUrl}/${jenkinsPath}/${branch}/buildWithParameters"
def queryParams = params.collect { k, v -> "${k}=${URLEncoder.encode(v.toString(), 'UTF-8')}" }.join("&")
def fullUrl = "${buildUrl}?${queryParams}"

try {
    // Abrir la conexión a Jenkins
    def connection = new URL(fullUrl).openConnection() as HttpURLConnection
    connection.setRequestProperty("Authorization", "Basic " + "${user}:${token}".bytes.encodeBase64().toString())
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)

    // Verificar la respuesta de Jenkins
    def responseCode = connection.responseCode
    def responseMessage = connection.responseMessage

    if (responseCode == 201 || responseCode == 200) {
        log.info "? Jenkins job triggered successfully for issue ${issueKey}."
    } else {
        log.error "? Fallo al disparar el job en Jenkins. Código de respuesta: ${responseCode}. Mensaje: ${responseMessage}"
        throw new InvalidInputException("Error al iniciar el despliegue en Jenkins. Respuesta: ${responseMessage}")
    }
} catch (Exception e) {
    log.error "? Error conectando con Jenkins: ${e.message}"
    throw new InvalidInputException("No se pudo conectar con Jenkins. Verifique la configuración.")
}
