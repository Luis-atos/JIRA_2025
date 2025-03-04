import groovy.json.JsonSlurper
import java.net.HttpURLConnection
import java.net.URL
import org.apache.log4j.Logger
import com.opensymphony.workflow.InvalidInputException

// Inicializar el logger
def log = Logger.getLogger(getClass())

// Función para obtener el ID de un proyecto de GitLab
def getGitLabProjectId(String projectPath, String privateToken) {
    try {
       
    def headers = ["Authorization": "Bearer $privateToken", "Content-Type": "application/json"]
    def url = "https://git.servdev.mdef.es/api/v4/projects?search=${URLEncoder.encode(projectPath.toString(), 'UTF-8')}"
    def response = httpRequest(url, 'GET', headers)
    if (response.status == 200) {
            log.warn("  ${response.status} ")
            def projects = new JsonSlurper().parseText(response.responseBody)
            log.warn(" ------  ${projects}  ")
        // Validar que la respuesta sea una lista
    
        if (projects && !projects.isEmpty()) {
           def projectId = projects[0].id
           def projectName = projects[0].name
           println("El ID del proyecto es: ${projectId} ${projectName} ")
           log.warn(" ------  ${projectId} ${projectName}  ")
           return projectId
          } else {
             println("La lista de proyectos está vacía.")
             log.error "Repositorio '${projectPath}' no encontrado en los resultados. Respuesta completa: ${response.responseBody}"
             throw new InvalidInputException("No se pudo encontrar el repositorio con nombre: ${projectPath}")
             return null
         }       
    } else {
            log.warn("  ERROR  ")
            log.warn("  ${response.status} ")
            return null
            
    }
    } catch (Exception e) {
         log.warn("  catch por error  ")
        println "Excepción: ${e.message}"
        return null
    }
}

// Uso de la función
def projectPath = "experimento_impoexpo-back"  // Ruta del proyecto
def privateToken = "glpat-c81sWsFemUxoRetdDYLk"  // Token de acceso
log.warn("  Entrada -----  ")
def projectId = getGitLabProjectId(projectPath, privateToken)
if (projectId) {
    log.warn("  El ID del proyecto es: ${projectId}  ")
} else {
         log.warn(" No se pudo obtener el ID del proyecto.  ")
}

