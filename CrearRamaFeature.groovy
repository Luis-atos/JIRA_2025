====================================
CREAR RAMA FEATURE EN GITLAB 
=================================

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.net.HttpURLConnection
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.component.ComponentAccessor

// Función principal que verificará si se puede crear la rama y permitir la transición
def validateBranchBeforeTransition(issue) {
    // Obtener información de la issue
    if (!issue) {
        log.error "No se pudo obtener la incidencia"
        return false
    }

    def issueKey = issue.key
    def issueSummary = issue.summary

    // Obtener el nombre del repositorio de un campo personalizado o del resumen de la incidencia
    String repoName = getRepositoryName(issue)
    if (!repoName) {
        log.error "No se pudo obtener el nombre del repositorio"
        return false
    }

    def branchName = getBranchName(issueKey)

    // Verificar si existe la rama en GitLab
    def existingBranch = checkIfBranchExists(repoName, branchName)
    
    if (existingBranch.exists) {
        log.warn "La rama $branchName ya existe. Permitiendo la transición."
        return true
    } else {
        log.warn "La rama $branchName no existe. Creándola..."
        
        def creationResult = createBranch(repoName, branchName)
        if (creationResult.success) {
            log.warn "Rama $branchName creada exitosamente. Permitiendo la transición."
            return true
        } else {
            log.error "Error al crear la rama $branchName: ${creationResult.message}"
            return false
        }
    }
}

def getRepositoryName(issue) {
    // Lógica para obtener el nombre del repositorio de un campo personalizado o del resumen
    // Por ejemplo, leer un campo personalizado 'Repositorio' o extraerlo del summary
    def repoField = issue.getCustomFieldValue(13500) ?: issue.summary
    return repoField?.toString().trim()
}

def getBranchName(issueKey) {
    // Nueva lógica para formar el nombre de la rama basado en la clave de la incidencia
    "feature/${issueKey}"
}

def checkIfBranchExists(repoName, branchName) {
    def token = getToken()  // Implementación para obtener el token de API
    def headers = ["Authorization": "Bearer $token", "Content-Type": "application/json"]

    log.warn("getProjectId ==. ${getProjectId(repoName)}")
    
    def url = "https://git.servdev.mdef.es/api/v4/projects/${getProjectId(repoName)}"
    def response = httpRequest(url, 'GET', headers)
    
    if (response.status == 200) {
        def project = new JsonSlurper().parseText(response.responseBody)
        for (def branch : project.branches) {
            if (branch.name == branchName) {
                return [exists: true]
            }
        }
    }
    
    return [exists: false]
}

def createBranch(repoName, branchName) {
    def token = getToken()  // Implementación para obtener el token de API
    def headers = ["Authorization": "Bearer $token", "Content-Type": "application/json"]
    
    def projectId = getProjectId(repoName)
    if (!projectId) {
        return [success: false, message: "No se pudo determinar el ID del proyecto."]
    }
    
    def url = "https://git.servdev.mdef.es/api/v4/projects/${projectId}/repository/branches"
    def requestBody = [
        branch: branchName,
        ref: 'develop'
    ]
    
    def response = httpRequest(url, 'POST', headers, new JsonOutput().toJsonString(requestBody))
    
    if (response.status == 201) {
        return [success: true]
    } else {
        return [success: false, message: "Error al crear la rama: ${response.responseBody}"]
    }
}

def getToken() {
    // Implementación para obtener el token de API desde una variable de entorno o seguro
    return System.getenv('GITLAB_API_TOKEN') ?: 'glpat-59ji9iQfSodLhPy478Tt'
}

def getProjectId(repoName) {
    log.warn "repoName ===: ${repoName}"
    def token = getToken()
    def headers = ["Authorization": "Bearer $token", "Content-Type": "application/json"]
    
    def url = "https://gitlab.com/api/v4/projects?search=$repoName"
    def response = httpRequest(url, 'GET', headers)
    
    if (response.status == 200) {
        def projects = new JsonSlurper().parseText(response.responseBody)
        for (def project : projects) {
            if (project.path_with_namespace.toLowerCase() == repoName.toLowerCase()) {
                return project.id
            }
        }
    }
    
    null
}

def httpRequest(url, method, headers, body = '') {
    def connection = new URL(url).openConnection()
    connection.requestMethod = method
    
    headers.each { key, value ->
        connection.setRequestProperty(key, value)
    }
    
    if (body) {
        connection.doOutput = true
        connection.outputStream.write(body.getBytes())
    }
    
    def response
    try {
        response = connection.inputStream.text
        connection.responseCode
    } catch (e) {
        log.error "Error realizando la solicitud HTTP: $e"
        throw e
    } finally {
        connection.disconnect()
    }
    
    [status: connection.responseCode, responseBody: response]
}


try {
    // Invocar la función de validación antes de permitir una transición
    def jiraIssue = ComponentAccessor.issueManager.getIssueObject(issue.id)
    validateBranchBeforeTransition(jiraIssue)
    log.warn("La rama existe o fue creada con éxito.")
} catch (e) {
    log.error("No se pudo validar la rama en GitLab.")
}
