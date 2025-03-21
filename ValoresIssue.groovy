import com.atlassian.jira.component.ComponentAccessor
import org.apache.log4j.Logger

def log = Logger.getLogger(getClass())
// ID o clave de la issue
def issueKey = "IMPEXP-67"

// Obtener el servicio de issues
def issueManager = ComponentAccessor.getIssueManager()

// Obtener la issue
def issue = issueManager.getIssueByCurrentKey(issueKey)

if (issue) {
    // Obtener campos estándar
    def keyIssue = issue.getKey()
    def summary = issue.getSummary()
    def description = issue.getDescription()
    def reporter = issue.getReporter()?.getDisplayName()
    def assignee = issue.getAssignee()?.getDisplayName()
    def status = issue.getStatus().getName()

    // Imprimir datos en consola
    log.warn "Clave de la issue: ${keyIssue}"
    log.warn "Resumen: $summary"
    log.warn "Resumen: $summary"
    log.warn "Descripción: $description"
    log.warn "Reportado por: $reporter"
    log.warn "Asignado a: $assignee"
    log.warn "Estado: $status"
    
    // Obtener un campo personalizado (ejemplo con un campo tipo check)
    def customFieldManager = ComponentAccessor.getCustomFieldManager()
    def customField = customFieldManager.getCustomFieldObjectsByName("Opciones_Tareas")?.first()

    if (customField) {
        // Obtener el valor del campo (devuelve una lista de opciones seleccionadas)
        def selectedOptions = issue.getCustomFieldValue(customField)
        if (selectedOptions) {
            def selectedValues = selectedOptions*.value  // Extraer los valores de las opciones seleccionadas
            log.warn "Opciones seleccionadas: ${selectedValues.join(', ')}"
        } else {
            log.warn "No hay opciones seleccionadas en el campo Opciones_Tareas"
        }
    } else {
        log.warn "No se encontró el campo personalizado Opciones_Tareas"
    }
  
} else {
    log.warn "No se encontró la issue con clave: $issueKey"
    log.error "No se encontró la issue con clave: $issueKey"
}


===========================================================
import com.atlassian.jira.component.ComponentAccessor
import org.apache.log4j.Logger

def log = Logger.getLogger(getClass())
// ID o clave de la issue
def issueKey = "IMPEXP-65"

// Obtener el servicio de issues
def issueManager = ComponentAccessor.getIssueManager()

// Obtener la issue
def issue = issueManager.getIssueByCurrentKey(issueKey)

if (issue) {
    // Obtener campos estándar
    def summary = issue.getSummary()
    def description = issue.getDescription()
    def reporter = issue.getReporter()?.getDisplayName()
    def assignee = issue.getAssignee()?.getDisplayName()
    def status = issue.getStatus().getName()

    // Imprimir datos en consola
    log.warn "Resumen: $summary"
    log.warn "Resumen: $summary"
    log.warn "Descripción: $description"
    log.warn "Reportado por: $reporter"
    log.warn "Asignado a: $assignee"
    log.warn "Estado: $status"
    
    // Obtener un campo personalizado (ejemplo con un campo tipo texto)
   // def customFieldManager = ComponentAccessor.getCustomFieldManager()
   // def customField = customFieldManager.getCustomFieldObjectByName("Nombre del Campo Personalizado")
   // def customFieldValue = issue.getCustomFieldValue(customField)

   // println "Valor del Campo Personalizado: $customFieldValue"
} else {
    log.warn "No se encontró la issue con clave: $issueKey"
    log.error "No se encontró la issue con clave: $issueKey"
}
