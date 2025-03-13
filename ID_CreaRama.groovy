import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.net.HttpURLConnection
import java.net.URL
import org.apache.log4j.Logger
import com.opensymphony.workflow.InvalidInputException
def log = Logger.getLogger(getClass())
def privateToken = "glpat-8e1u3KikpjQzCJ4e_mzr"
def projectPath = "EXPERIMENTO_IMPOEXPO_BACK" // Reemplaza con el nombre real del proyecto




def httpRequest(String url, String method, Map headers, String body = '') {
    def connection = new URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = method
    connection.doOutput = (method in ['POST', 'PUT', 'PATCH']) // Habilitar salida si es necesario

    // Agregar Headers
    headers.each { key, value -> connection.setRequestProperty(key, value) }

    // Enviar el body si existe y si es un método que lo admite
    if (body && connection.doOutput) {
        connection.outputStream.withWriter("UTF-8") { it.write(body) }
    }

    // Obtener respuesta
    def responseCode = connection.responseCode
    if (responseCode in [200, 201, 204]) { // 201 = Created, 204 = No Content
        def responseText = connection.inputStream.text
        return responseText ? new JsonSlurper().parseText(responseText) : null
    } else {
        println "Error: Código $responseCode - ${connection.errorStream?.text}"
        return null
    }
}

def creaRama(idProject,privateToken){
  
    def headers = ["Authorization": "Bearer ${privateToken}", "Content-Type": "application/json"]
    //def encodedBranchName = URLEncoder.encode(branchName, "UTF-8")
    def url = "https://git.servdev.mdef.es/api/v4/projects/${idProject}/repository/branches"
    def requestBody = [
        branch: 'feature_pru',
        ref: 'develop'
    ]
     log.warn("Response: ${url}  ")
      log.warn("headers: ${headers}  ")
       log.warn("Body: ${JsonOutput.toJson(requestBody)}")
    def response = httpRequest(url, 'POST', headers, JsonOutput.toJson(requestBody))
     log.warn("Response: ${response}") 
    if (response.status == 201) {
        return [success: true]
    } else {
        return [success: false, message: "Error al crear la rama: ${response.responseBody}"]
    }

}




// Configuración de Headers
def headers = [
    "Authorization": "Bearer $privateToken",
    "Content-Type": "application/json"
]

// Realizar la solicitud GET
def url = "https://git.servdev.mdef.es/api/v4/projects?search=${URLEncoder.encode(projectPath.toString(), 'UTF-8')}"
def response = httpRequest(url, 'GET', headers)



if (response) {
    log.warn("Proyectos encontrados:") 
    response.each { project ->
       log.warn("ID: ${project.id}, Nombre: ${project.name}, Path: ${project.path_with_namespace}:") 
       creaRama(project.id,privateToken)
    }
   
} else {
    println "No se encontraron proyectos o hubo un error en la solicitud."
}


