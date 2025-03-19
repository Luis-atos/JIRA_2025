pipeline {
    agent { label 'Jenkins_PRE' }
    environment {
        //Tomamos el nombre de la aplicación y el entorno basándonos en el nombre de la tarea jenkins
        APP_NAME=sh(script: "echo ${env.JOB_NAME} | awk -F '_' '{print \$3}'", returnStdout: true).trim()
        ENTORNO=sh(script: "echo ${env.JOB_NAME} | awk -F '_' '{print \$2}'", returnStdout: true).trim()
        
        //Definimos nombres y rutas de los scripts a ejecutar durante el despliegue
		SCRIPT_DESCARGA_NAME="descarga_artefactos_'${APP_NAME}'.sh"
		SCRIPT_DESCARGA="${env.WORKSPACE}/../${SCRIPT_DESCARGA_NAME}"
		SCRIPT_SUBIDA_NAME="subir_a_nexus_${APP_NAME}.sh"
		SCRIPT_SUBIDA="${env.WORKSPACE}/../${SCRIPT_SUBIDA_NAME}"
		SCRIPT_DESPLIEGUE_NAME="despliegue_artefactos_${APP_NAME}.sh"
		SCRIPT_DESPLIEGUE="${env.WORKSPACE}/../${SCRIPT_DESPLIEGUE_NAME}"
		
		//Archivo de texto con el nombre del zip que contiene toda la aplicación, para subirla a Nexus
		ARTEFACTOS_RESUMEN="Artefactos_${APP_NAME}.txt"
		
		//Servidor nexus donde están los artefactos de origen
		NEXUS_SERVER_ORIGIN="https://nexus.servdev.mdef.es"
		
		//Servidor nexus donde se subirá el zip con la aplicación
		NEXUS_SERVER_PRE="https://nexus.servdev.mdef.es"
		 
        //Ruta de origen de cada artefacto. Se inicializan tras solicitar las versiones de back-end y front-end, porque la ruta contiene la versión
        URL_NEXUS_BACK=""
        URL_NEXUS_FRONT=""
        
		//Variables donde se guardará el valor introducido en el paso de la pipeline de solicitud de versiones
		VERSION_FRONT=""
		VERSION_BACK=""
		
		//Servidor coordinador de JBOSS y grupo de despliegue
		//JBOSS_SERVER='SRVCCEAAJBL26P'
		//GRUPO_DESPLIEGUE='Grupo-REPEREST'
		
		//Array simulado con los servidores, usando una coma como delimitador, sin espacios. Se convertirá en array en el script de despliegue
		//BACK_SERVERS="SRVCCEAAJBL27P,SRVCCEAAJBL28P"
		//FRONT_SERVERS="SRVCCEAWAPL15P,SRVCCEAWAPL16P"
		
        //Usuario linux estándar para jenkins en cada servidor de preproducción
		//USER_PRE='jenkinspre'
		
    }
	
    stages {
        stage('Solicitar versiones de back-end y front-end') {
            steps {
                script {
                    try{
                        echo "***** Solicitando versión del back-end *****"
                        def version_back = input(
                            message: 'Por favor, introduzca la versión del back',
                            parameters: [string(name: 'Versión', defaultValue: '1.15.0.0', description: 'Versión del back-end')]
                        )
                        
                        if ("${version_back}" == "0.0.0.0") {
                            error("Abortando pipeline: La versión del back no puede ser 0.0.0.0")
                        }else{
                            VERSION_BACK="${version_back}"
                            echo "El número de versión ingresado es: ${version_back}"
                        }
                        
                        echo "***** Solicitando versión del front-end *****"
                        def version_front = input(
                            message: 'Por favor, introduzca la versión del front',
                            parameters: [string(name: 'Versión', defaultValue: '2.4.0.0', description: 'Versión del front-end')]
                        )
                        
                        if ("${version_front}" == "0.0.0.0") {
                            error("Abortando pipeline: La versión del front no puede ser 0.0.0.0")
                        }else{
                            VERSION_FRONT="${version_front}"
                            echo "El número de versión ingresado es: ${version_front}"
                        }
                        
                    }catch(err){
					   echo " ***** ERROR: LA CARGA DE VERSIONES HA FALLADO ****** "
						currentBuild.result = 'ABORTED'
						throw new Exception("ERROR: LA CARGA DE VERSIONES HA FALLADO")
				    }
                }
            }
        }
        
        stage('Recuperación de variables') {
            steps {
                script {
                    try{
                    def propertiesPath = "${WORKSPACE}/${APP_NAME}.properties"

                    if (fileExists(propertiesPath)) {
                        echo "El archivo ${propertiesPath} existe."
                        
                        def props = readProperties file: propertiesPath
                        
                        // Imprimir una propiedad específica
                        TECNOLOGIA = props['TECNOLOGIA']
                        if (TECNOLOGIA) {
                            echo "La tecnología es: ${TECNOLOGIA}"
                        } else {
                            echo "No se encontró la propiedad 'TECNOLOGIA' en el archivo."
                            currentBuild.result = 'ABORTED'
                        }
                    
                        switch ("${TECNOLOGIA}") {
                            case "'JBOSS'":
                                JBOSS_SERVER = props['JBOSS_SERVER']
                                if (JBOSS_SERVER) {
                                    echo "El coordinador JBOSS es: ${JBOSS_SERVER}"
                                } else {
                                    echo "No se encontró la propiedad 'JBOSS_SERVER' en el archivo."
                                    currentBuild.result = 'ABORTED'
                                }
                                
                                GRUPO_DESPLIEGUE = props['GRUPO_DESPLIEGUE']
                                if (GRUPO_DESPLIEGUE) {
                                    echo "El GRUPO_DESPLIEGUE es: ${GRUPO_DESPLIEGUE}"
                                } else {
                                    echo "No se encontró la propiedad 'GRUPO_DESPLIEGUE' en el archivo."
                                    currentBuild.result = 'ABORTED'
                                }
                                
                                BACK_SERVERS = props['BACK_SERVERS']
                                if (BACK_SERVERS) {
                                    echo "Los servidores back-end son: ${BACK_SERVERS}"
                                } else {
                                    echo "No se encontró la propiedad 'BACK_SERVERS' en el archivo."
                                    currentBuild.result = 'ABORTED'
                                }
                                
                                FRONT_SERVERS = props['FRONT_SERVERS']
                                if (FRONT_SERVERS) {
                                    echo "Los servidores front-end son: ${FRONT_SERVERS}"
                                } else {
                                    echo "No se encontró la propiedad 'FRONT_SERVERS' en el archivo."
                                    currentBuild.result = 'ABORTED'
                                }
                                
                                USER_PRE = props['USER_PRE']
                                if (USER_PRE) {
                                    echo "El usuario de PRE es: ${USER_PRE}"
                                } else {
                                    echo "No se encontró la propiedad 'USER_PRE' en el archivo."
                                    currentBuild.result = 'ABORTED'
                                }
                            
                                break
                            case "TOMCAT":
                                println("El valor es B")
                                break
                            case "C":
                                println("El valor es C")
                                break
                            default:
                                println("No se encontró el valor")
                                currentBuild.result = 'ABORTED'
                        }
                            
                        } else {
                            echo "El archivo ${propertiesPath} no existe."
                            currentBuild.result = 'ABORTED'
                        }
                    }catch (err){
                        echo "Error al leer el fichero"
                        currentBuild.result = 'ABORTED'
    					throw new Exception("Error al leer el fichero")
                    }
                }
            }
        }
        
        stage('Creación dinámica de URLs de origen') {
            steps {
                script {
                    try{
                        //URL_NEXUS_BACK="${NEXUS_SERVER_ORIGIN}/repository/releases/PruebaTest/PruebaTest/${APP_NAME}_BACK/${VERSION_BACK}/${APP_NAME}_BACK_${VERSION_BACK}.zip"
                        URL_NEXUS_BACK="${NEXUS_SERVER_ORIGIN}/repository/releases/PruebaTest/PruebaTest/${APP_NAME}_BACK/${VERSION_BACK}/reperest.zip"
                        echo "Origen del back-end definido en:"
                        echo "${URL_NEXUS_BACK}"
                        
                        URL_NEXUS_FRONT="${NEXUS_SERVER_ORIGIN}/repository/${APP_NAME}_FRONT/${APP_NAME}_FRONT_${VERSION_FRONT}.zip"
                        //URL_NEXUS_FRONT="${NEXUS_SERVER_ORIGIN}/repository/${APP_NAME}_FRONT/${VERSION_FRONT}.zip"
                        echo "Origen del front-end definido en:"
                        echo "${URL_NEXUS_FRONT}"
                        
                    }catch(err){
					   echo " ***** ERROR: LA GENERACIÓN DINÁMICA DE URLs HA FALLADO ****** "
						currentBuild.result = 'ABORTED'
						throw new Exception("ERROR: LA GENERACIÓN DINÁMICA DE URLs HA FALLADO")
				    }
                }
            }
        }
        
        
		stage('Descarga de artefactos') {
            steps {
                script{
                    echo "Descarga"
                    try{
                        withCredentials([usernamePassword(credentialsId: 'jenkins120_nexus', passwordVariable: 'clave', usernameVariable: 'usuario')])
						{
							output = sh(script: "${SCRIPT_DESCARGA} ${usuario} ${clave} '${APP_NAME}' '${ENTORNO}' '${VERSION_BACK}' '${VERSION_FRONT}' '${ARTEFACTOS_RESUMEN}' '${URL_NEXUS_BACK}' '${URL_NEXUS_FRONT}'", returnStdout: true)
							
						}
					}catch(err){
					   echo " ***** ERROR: LA DESCARGA DEL BACK HA FALLADO ****** "
					   echo " ***** Revise el archivo ${env.WORKSPACE}/log.txt para más información"
						currentBuild.result = 'ABORTED'
						throw new Exception("ERROR: LA DESCARGA DEL BACK HA FALLADO")
				    }
                }
            }
        }

		stage('Subida a Nexus') {
            steps {
                script{
                    try{
    				    echo 'Subiendo a NEXUS la aplicación...'
    				    withCredentials([usernamePassword(credentialsId: 'jenkins120_nexus', passwordVariable: 'clave', usernameVariable: 'usuario')])
						{
							switch ("${TECNOLOGIA}") {
                            case "'JBOSS'":
                                output = sh(script: "${SCRIPT_SUBIDA} ${usuario} ${clave} ${APP_NAME} ${ENTORNO} '${ARTEFACTOS_RESUMEN}'", returnStdout: true)
							
                                break
                            case "B":
                                println("El valor es B")
                                break
                            case "C":
                                println("El valor es C")
                                break
                            default:
                                println("No se encontró el valor")
                                currentBuild.result = 'ABORTED'
                        }
							
							
						}
    				}catch(err){
    				echo " ***** ERROR: SUBIDA A NEXUS FALLIDA ****** "
    					currentBuild.result = 'ABORTED'
    					throw new Exception("ERROR: SUBIDA A NEXUS FALLIDA")
    				}
                }
            }
        }
		
		stage('Despliegue de aplicación') {
            steps {
                script{
                    try{
                    
					echo 'Iniciando despliegue de la aplicación...'
    				withCredentials([usernamePassword(credentialsId: 'jenkins120_nexus', passwordVariable: 'clave', usernameVariable: 'usuario')])
						{
    						switch ("${TECNOLOGIA}") {
                                case "'JBOSS'":
                                    //output = sh(script: "${SCRIPT_DESPLIEGUE} ${usuario} ${clave} '${APP_NAME}' '${ENTORNO}' '${TECNOLOGIA}' '${BACK_SERVERS}' '${FRONT_SERVERS}' '${JBOSS_SERVER}' '${GRUPO_DESPLIEGUE}'", returnStdout: true)
                                    break
                                case "B":
                                    println("El valor es B")
                                    break
                                case "C":
                                    println("El valor es C")
                                    break
                                default:
                                    println("No se encontró el valor")
                                    currentBuild.result = 'ABORTED'
    							
    						}
						}
    				}catch(err){
    				echo " ***** ERROR: DESPLIEGUE FALLIDO ****** "
    					currentBuild.result = 'ABORTED'
    					throw new Exception("ERROR: DESPLIEGUE FALLIDO")
    				}
                }
            }
        }
    }
}
