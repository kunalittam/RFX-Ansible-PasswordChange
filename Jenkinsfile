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
    ])
])
def skipRemainingStages = false
def cluster1=""
pipeline {
    agent{
        node {
            label 'ansible'
            customWorkspace "/application/${env.BUILD_NUMBER}"
        }
    }
    options {
        ansiColor('xterm')
        skipDefaultCheckout(true)
    }

    stages {
        stage('Get variables') {
            steps {
                script {
                    addShortText(border: 0, text: "ENVIRONMENT:-" + ENV_TYPE, background: "azure", color: "black")
                    addShortText(border: 0, text: "CLUSTER_NAME:-" + CLUSTER_NAME, background: "beige", color: "black")                    
                    if (Application.contains("KERNEL")){
                        cluster1=CLUSTER_NAME
                        CLUSTER_NAME="all"
                        JOB_NAME="kernelPasswordUpdate"
                    }
                    else if (Application.contains("MYWORK")){
                        cluster1=CLUSTER_NAME
                        CLUSTER_NAME="all"
                        JOB_NAME="myworkPasswordUpdate"
                    }
                    else{
                        CLUSTER_NAME=Application
                        JOB_NAME="rwsPasswordUpdate"
                    }
                    
                    tags=JOB_NAME
                    playbook="runMaintenance.yml"
                    extra_vars="CLUSTER_NAME=${CLUSTER_NAME}"

                addShortText(border: 0, text: "Application:-" + Application, background: "bisque", color: "black")
                if ( ! params.SNOWTICKET.isEmpty()){
                    addShortText(border: 0, text: "SNOWTICKET:-" + SNOWTICKET, background: "green", color: "black")
                }   
                
                }//script
            }//steps
        }//stage

        stage('Checkout from GitHub') {
            when {
                expression {
                    !skipRemainingStages
                }
            }
            steps {
                checkout([$class: 'GitSCM', 
                branches: [[name: '*/test']], 
                extensions: [[$class: 'RelativeTargetDirectory', 
                relativeTargetDir: "${WORKSPACE}"], 
                [$class: 'CleanBeforeCheckout']], 
                userRemoteConfigs: [[credentialsId: 'kunalpersonal', url: 'https://github.com/kunalittam/RFX-Ansible-PasswordChange.git']]])
            }//steps
        }//stage
        stage ('Get Passwords') {
            when {
                expression {
                    !skipRemainingStages
                }
            }
            steps {
                script {
                    if (! params.PASSWORDS.isEmpty())
                    {
                        writeFile (file: ".passwordtest.yml", text: "${PASSWORDS}")
                        echo "Workspace dir is ${WORKSPACE}"
                    }
                }
            }
        }
        stage ('Ansible Apply') {
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
                    extras: "-e \" ${extra_vars} \" --extra-vars=@.passwordtest.yml ",
                    installation: 'ansible', 
                    inventory: '/application/ansible/inventory/${ENV_TYPE}/${CLUSTER_NAME}/${Application}', 
                    playbook: "${WORKSPACE}/Middleware_PasswordUpdate/projects/Password_Update/${playbook}",
                    tags: "${tags}"
            }            
        }
        stage ('Delete Passwords') {
            when {
                expression {
                    !skipRemainingStages
                }
            }
            steps {
                script {
                    if (! params.PASSWORDS.isEmpty())
                    {
                        sh """
                            set +x
                            pwd
                            rm .passwordtest.yml
                        """
                    }
                }
            }
        }
    }
}