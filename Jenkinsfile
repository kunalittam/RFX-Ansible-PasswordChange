properties([
    parameters([
        string(description: 'Enter the SNOW TICKET Number', name: 'SNOWTICKET', defaultValue: ''),
        [
            $class: 'CascadeChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: 'Select the Cluster',
            referencedParameters: 'SNOWTICKET',
            name: 'ENV_TYPE',
            script: [
                $class: 'GroovyScript',
                fallbackScript: [
                    classpath: [],
                    sandbox: false,
                    script: 'return [\'ERROR\']'
                ],
                script: [
                    classpath: [],
                    sandbox: false,
                    script:  
                    '''
                      import hudson.model.User
                      import hudson.model.Hudson
                      import hudson.security.AuthorizationStrategy
                      import hudson.security.Permission
                      import com.michelin.cio.hudson.plugins.rolestrategy.RoleBasedAuthorizationStrategy
                      import com.michelin.cio.hudson.plugins.rolestrategy.RoleMap
                      if (SNOWTICKET.isEmpty() || SNOWTICKET==""){
                        return ''
                      }
                      else
                      {
                        jobs = []
                        user = User.current()
                        userId = user.getId()

                        def authStrategy = Hudson.instance.getAuthorizationStrategy()
                        def permissions = authStrategy.roleMaps.inject([:]){map, it -> map + it.value.grantedRoles}
                        if (permissions.findAll{ it.value.contains(userId) }.collect{it.key.name}.contains('Middleware_NonProd')||permissions.findAll{ it.value.contains(userId) }.collect{it.key.name}.contains('CLM_NonProd'))
                        {
                          return ["GCP-Sandbox", "GCP-Preprod"]
                        }
                        else if (permissions.findAll{ it.value.contains(userId) }.collect{it.key.name}.contains('admin') || permissions.findAll{ it.value.contains(userId) }.collect{it.key.name}.contains('Middleware_Prod'))
                        {
                          return ["GCP-Sandbox", "GCP-Preprod", "GCP-Production"]
                        }
                        else
                        {
                          return ['ERROR']
                        }
                      }
                    '''
                ]
            ]
        ],
        [
            $class: 'CascadeChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: 'Select the Cluster',
            referencedParameters: 'ENV_TYPE,SNOWTICKET',
            name: 'CLUSTER_NAME',
            script: [
                $class: 'GroovyScript',
                fallbackScript: [
                    classpath: [],
                    sandbox: false,
                    script: 'return [\'ERROR\']'
                ],
                script: [
                    classpath: [],
                    sandbox: false,
                    script:  
                    '''                    
                        import groovy.io.FileType
                        if (SNOWTICKET.isEmpty() || SNOWTICKET==""){
                            return ''
                        }
                        else
                        {
                            def list = []
                            def dir = new File("/application/ansible/inventory/${ENV_TYPE}/")
                            dir.eachFileRecurse (FileType.DIRECTORIES) { file ->
                                list << file.name
                            }
                            return list.sort() - 'group_vars' 
                        }
                    '''
                ]
            ]
        ],
        [
            $class: 'CascadeChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: '',
            referencedParameters: 'CLUSTER_NAME, ENV_TYPE, SNOWTICKET',
            name: 'Application',
            script: [
                $class: 'GroovyScript',
                fallbackScript: [
                    classpath: [],
                    sandbox: false,
                    script: 'return [\'ERROR\']'
                ],
                script: [
                    classpath: [],
                    sandbox: false,
                    script:  
                    '''
                        import groovy.io.FileType
                        import groovy.io.FileType
                        if (SNOWTICKET.isEmpty() || SNOWTICKET==""){
                            return ''
                        }
                        else
                        {                        
                            def list = []
                            def dir = new File("/application/ansible/inventory/${ENV_TYPE}/${CLUSTER_NAME}")
                            dir.eachFile (FileType.FILES) { file ->
                                list << file.name.replaceAll('.yml','');
                            }
                            return list
                        }
                    '''
                ]
            ]
        ],
        text(name: 'PASSWORDS', defaultValue: 'rtmadm: \'rtmadm_pass\'', description: 'Please enter the passowrd in single codes'),
        [
            $class: 'CascadeChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            referencedParameters: 'SNOWTICKET',
            description: "Module of which password to be changed",
            name: 'JOB_NAME',
            script: [
                $class: 'GroovyScript',
                fallbackScript: [
                    classpath: [],
                    sandbox: false,
                    script:
                        'return[\'Error creating options\']'
                ],
                script: [
                    classpath: [],
                    sandbox: false,
                    script: '''
                        if (SNOWTICKET.isEmpty() || SNOWTICKET==""){
                            return ''
                        }
                        else
                        {                        
                            def jobs = [
                                "kernelPasswordUpdate",
                                "myworkPasswordUpdate",
                                "rwsPasswordUpdate",
                            ]
                            return jobs
                        }
                    '''
                ]
            ]
        ],

        [
            $class: 'DynamicReferenceParameter',
            choiceType: 'ET_FORMATTED_HTML',
            description: 'Enter SVN or Artifactory URL in case of artifacts deployment, this option is only required when you selected wasBiweeklyMaintenance or wasBiweeklyMaintenanceSTOP tags.',
            name: 'repo',
            omitValueField: true, 
            referencedParameters: 'JOB_NAME, CLUSTER_NAME',
            script: [
                $class: 'GroovyScript',
                fallbackScript: [
                    classpath: [],
                    sandbox: false,
                    script:
                      'return[\'Error\']'

                ],
                script: [
                    classpath: [],
                    sandbox: false,
                    script:
                    '''
                        if ((JOB_NAME.equals("RWS_Extra_Static_Deployment") ||JOB_NAME.equals("rollingRestartServers")||JOB_NAME.equals("wasBiweeklyMaintenance") ||     JOB_NAME.equals("wasBiweeklyMaintenanceSTOP")) && CLUSTER_NAME != ""){
                            return "<input name='value' type='text' class='setting-input'>"
                        }

                    '''
                ]
            ]
        ],
    ])
])
def skipRemainingStages = false
def cluster1=""
pipeline {
    agent{
        label 'ansible'
    }
    options {
        ansiColor('xterm')
        skipDefaultCheckout(true)
    }

    stages {
        stage('Get variables') {
            agent{
                label 'ansible'
            }
            steps {
                script {
                    addShortText(border: 0, text: "ENVIRONMENT=" + ENV_TYPE, background: "azure", color: "black")
                    addShortText(border: 0, text: "CLUSTER_NAME=" + CLUSTER_NAME, background: "beige", color: "black")                    
                    if (Application.contains("KERNEL") || Application.contains("MYWORK")){
                        cluster1=CLUSTER_NAME
                        CLUSTER_NAME="all"
                    }
                    else{
                        CLUSTER_NAME=Application
                    }
                    
                    tags=JOB_NAME
                    playbook="runMaintenance.yml"

                    if ( ! params.repo.isEmpty()){
                        println repo
                        if (repo.contains('https://') && repo.contains('artifactory')){
                            if (repo.toLowerCase().contains(ENV_TYPE.toLowerCase().substring(4)) && repo.toLowerCase().contains(cluster1.toLowerCase())){
                                extra_vars="CLUSTER_NAME=${CLUSTER_NAME} artifactory_location=${repo}"
                            }
                            else{
                                skipRemainingStages = true
                            }

                        }
                        else if (repo.contains('https://') && repo.contains('svn')){
                            if (repo.toLowerCase().contains(ENVIRONMENT.toLowerCase().substring(4)) && repo.toLowerCase().contains(cluster1.toLowerCase())){
                                extra_vars="CLUSTER_NAME=${CLUSTER_NAME} svn_location=${repo}"
                            }
                            else{
                                skipRemainingStages = true
                            }
                        }
                    }
                    else{
                        extra_vars="CLUSTER_NAME=${CLUSTER_NAME}"
                    }

                addShortText(border: 0, text: "Application=" + Application, background: "bisque", color: "black")
                addShortText(border: 0, text: "tags=" + tags, background: "burlyWood", color: "black")  
                addShortText(border: 0, text: "playbook=" + playbook, background: "brown", color: "white") 
                if ( ! params.SNOWTICKET.isEmpty()){
                    addShortText(border: 0, text: "SNOWTICKET=" + SNOWTICKET, background: "green", color: "black")
                }
                if (! skipRemainingStages){
                    addShortText(border: 0, text: "extra_vars=\"" + extra_vars + "\"", background: "CadetBlue", color: "white")
                }    
                
                }//script
            }//steps
        }//stage
        stage('Abort on Wrong URL') {
            agent{
                label 'ansible'
            }
            when {
                expression {
                    skipRemainingStages
                }
            }
            steps {
                script {
                    currentBuild.result = 'ABORTED'
                    error("Please check environment details and provided URLs of repo...!!!")
                }
            }
        }

        stage('Checkout from GitHub') {
            agent{
                label 'ansible'
            }
            when {
                expression {
                    !skipRemainingStages
                }
            }
            steps {
                checkout([$class: 'GitSCM', 
                branches: [[name: '*/main']], 
                extensions: [[$class: 'RelativeTargetDirectory', 
                relativeTargetDir: "${WORKSPACE}"], 
                [$class: 'CleanBeforeCheckout']], 
                userRemoteConfigs: [[credentialsId: 'kunalpersonal', url: 'https://github.com/kunalittam/RFX-Ansible-PasswordChange.git']]])
            }//steps
        }//stage
        stage ('Get Passwords') {
            agent{
                label 'ansible'
            }
            when {
                expression {
                    !skipRemainingStages
                }
            }
            steps {
                script {
                    if (! params.PASSWORDS.isEmpty())
                    {
                        echo "Passwords are as follows:-\n ${PASSWORDS}"
                        writeFile (file: "passwordtest.yml", text: "${PASSWORDS}")
                        sh "ls -l"
                    }
                }
            }
        }
        stage ('Ansible Apply') {
            agent{
                label 'ansible'
            }
            when {
                expression {
                    !skipRemainingStages
                }
            }
            steps {
                script {
                if ( ! extra_vars.isEmpty()){
                    echo "extra vars:  ${extra_vars}"
                }
                }
                echo "CLUSTER_NAME:  ${CLUSTER_NAME}"
                echo "tags:  ${tags}"
                echo "playbook: ${playbook}"
                echo "SNOWTICKET: ${SNOWTICKET}"
                echo "workspace: ${WORKSPACE}"
                ansiblePlaybook forks: 1000,
                    colorized: true,
                    disableHostKeyChecking: true, 
                    extras: "-e \" ${extra_vars} \" --extra-vars=@passwordtest.yml ",
                    installation: 'ansible', 
                    inventory: '/application/ansible/inventory/${ENV_TYPE}/${CLUSTER_NAME}/${Application}', 
                    playbook: "${WORKSPACE}/Middleware_PasswordUpdate/projects/Password_Update/${playbook}",
                    tags: "${tags}"
            }            
        }
        stage ('Delete Passwords') {
            agent{
                label 'ansible'
            }
            when {
                expression {
                    !skipRemainingStages
                }
            }
            steps {
                script {
                    if (! params.PASSWORDS.isEmpty())
                    {
                        sh "rm passwordtest.yml"
                        sh "ls -l"
                    }
                }
            }
        }
    }
}