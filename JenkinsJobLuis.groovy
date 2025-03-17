import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import com.atlassian.jira.component.ComponentAccessor
import com.opensymphony.workflow.InvalidInputException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.apache.commons.codec.binary.Base64

// Obtener información de la issue
def issueKey = "IMPEXP-67"

// Obtener el servicio de issues
def issueManager = ComponentAccessor.getIssueManager()

// Obtener la issue
def issue = issueManager.getIssueByCurrentKey(issueKey)

def keyIssue = issue.getKey()
def issueSummary = issue.getSummary()
def reporter = issue.getReporter()?.getDisplayName()
def status = issue.getStatus().getName()

// Configuración de Jenkins
//def jenkinsUrl = "http://srvcceacml77:8080"
def jenkinsUrl = "http://srvcceacml120d:8080"
def jobName = "EXPERIMENTO_IMPOEXPO"
def jenkinsUser = "lmunma1"                                // Usuario de Jenkins
def jenkinsToken = "119dd35bd85e15e75140ede838bad87c1f" 


// Codificar credenciales en Base64
def authString = "${jenkinsUser}:${jenkinsToken}".bytes.encodeBase64().toString()

// Parámetros para pasar a Jenkins

def params = [
    ISSUE_KEY: keyIssue,
    ISSUE_SUMMARY: issueSummary,
    REPORTER: reporter,
    STATUS: status
]


// Construir la URL de la API para disparar el job
def apiUrl = "${jenkinsUrl}/job/${jobName}/job/develop/buildWithParameters"
//def apiUrl = "${jenkinsUrl}/job/${jobName}/job/develop/build"
def queryParams = params.collect { k, v -> "${k}=${URLEncoder.encode(v.toString(), 'UTF-8')}" }.join("&")

def finalUrl = "${apiUrl}?${queryParams}"

// Configurar la petición HTTP
def connection = new URL(finalUrl).openConnection() as HttpURLConnection
connection.setRequestProperty("Authorization", "Basic ${authString}")
connection.setRequestMethod("POST")
connection.setRequestProperty("Content-Type", "application/json")
connection.setDoOutput(true)

// Ejecutar la petición y capturar la respuesta
def responseCode = connection.responseCode
log.warn "Código de respuesta: $responseCode"

if (responseCode == 200 || responseCode == 201) {
    def responseStream = connection.inputStream.text
    log.warn "Respuesta de Jenkins: $responseStream"
    return "Job ejecutado correctamente en Jenkins: $responseStream"
} else {
    def errorStream = connection.errorStream?.text ?: "No hay respuesta de error"
    log.error "Error al ejecutar job en Jenkins - Código: $responseCode, Respuesta: $errorStream"
    throw new InvalidInputException("Error al ejecutar job en Jenkins: Código $responseCode")
}

el pipeline debe tener los parametros :

def call(Map params) {
//pipeline {
environment {
        JIRA_SITE = "JENKINS_JIRA"
        JIRA_PROJECT_KEY = "IMPEXP"  
        JIRA_ISSUE_TYPE = "Error"
    }
    
 
def pathWS=''
def userID =""
def versionInput=""
def environment_deploy=""
def deployTarget=""
def versionOK=""
def taskUrl =""
def statusSonar="PASSED"
def libsfunctionsArtefactos
def libsfunctionsZipConsoleOut
def checkQG=""
def agentLabels='linux'
if (currentBuild.getBuildCauses().toString().contains('BranchIndexingCause')) { currentBuild.result = 'ABORTED'; return; }
def name_job = env.JOB_NAME.split("/")[0]
if (name_job.matches("SINFRADEF_(.*)")){
   agentLabels='linux'
}else if ((env.BRANCH_NAME.matches("feature(.*)"))){
   agentLabels='Jenkins_CI'
}
pipeline{
   agent { label agentLabels}
   
   options {
    timeout(time: 2, unit: 'HOURS')   // timeout on whole pipeline job
   }
   tools{
	    maven "apache-maven-3.8.6"
	   jdk "openjdk1.8"
	 }
   parameters {
        string(name: 'ISSUE_KEY', defaultValue: '', description: 'Clave de la Issue')
        string(name: 'ISSUE_SUMMARY', defaultValue: '', description: 'Resumen de la Issue')
        string(name: 'REPORTER', defaultValue: '', description: 'Reportero')
        string(name: 'STATUS', defaultValue: '', description: 'Estado de la Issue')
    }
         
   stages{  
         stage('Download params'){
         steps{
         //   logstash {
            script{
                      echo """*************** Parametros de entrada *************************
                                Tecnologia: ${params.Tecnologia}
                                Proyecto: ${params.Proyecto}
                                Rama: ${params.Rama}                     
                                """

                     e



=========================================================================
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import com.atlassian.jira.component.ComponentAccessor
import com.opensymphony.workflow.InvalidInputException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.apache.commons.codec.binary.Base64

// Obtener información de la issue
def issueKey = "IMPEXP-67"

// Obtener el servicio de issues
def issueManager = ComponentAccessor.getIssueManager()

// Obtener la issue
def issue = issueManager.getIssueByCurrentKey(issueKey)

def keyIssue = issue.getKey()
def issueSummary = issue.getSummary()
def reporter = issue.getReporter()?.getDisplayName()
def status = issue.getStatus().getName()

// Configuración de Jenkins
def jenkinsUrl = "http://srvcceacml77:8080"
def jobName = "MENSADEF_BACK"
def jenkinsUser = "lmunma1"                                // Usuario de Jenkins
def jenkinsToken = "11a09035c1a4dc27413d47981e99fc16c4" 


// Codificar credenciales en Base64
def authString = "${jenkinsUser}:${jenkinsToken}".bytes.encodeBase64().toString()

// Parámetros para pasar a Jenkins
def params = [
    ISSUE_KEY: keyIssue,
    ISSUE_SUMMARY: issueSummary,
    REPORTER: reporter,
    STATUS: status
]

// Construir la URL de la API para disparar el job
def apiUrl = "${jenkinsUrl}/job/${jobName}/job/develop/build"
def queryParams = params.collect { k, v -> "${k}=${URLEncoder.encode(v.toString(), 'UTF-8')}" }.join("&")

def finalUrl = "${apiUrl}?${queryParams}"

// Configurar la petición HTTP
def connection = new URL(finalUrl).openConnection() as HttpURLConnection
connection.setRequestProperty("Authorization", "Basic ${authString}")
connection.setRequestMethod("POST")
connection.setRequestProperty("Content-Type", "application/json")
connection.setDoOutput(true)

// Ejecutar la petición y capturar la respuesta
def responseCode = connection.responseCode
log.warn "Código de respuesta: $responseCode"

if (responseCode == 200 || responseCode == 201) {
    def responseStream = connection.inputStream.text
    log.warn "Respuesta de Jenkins: $responseStream"
    return "Job ejecutado correctamente en Jenkins: $responseStream"
} else {
    def errorStream = connection.errorStream?.text ?: "No hay respuesta de error"
    log.error "Error al ejecutar job en Jenkins - Código: $responseCode, Respuesta: $errorStream"
    throw new InvalidInputException("Error al ejecutar job en Jenkins: Código $responseCode")
}
