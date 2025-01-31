#!/usr/bin/env groovy
String id;
def call(suite, project,
         host = env.LEAP_HOST, token = env.LEAP_TOKEN,
         passPercentVar = 'PASSED_PERCENTAGE', resultsSourceFile = 'results.properties') {
    def parseError = { res ->
        def msg = res == null ? null : res.message ?: res
        return msg != null ? "\nMSG: ${msg}" : ''
    }
    def info = { String msg -> println "(${project}/${suite}): ${msg}" }
    def debug = { String msg ->
        if (env.DEBUG == 'true' || env.debug == 'true') {
            info msg
        }
    }
    def setPassed = { value ->
        value  = (int) Float.valueOf(value)
        env[passPercentVar] = value
        writeFile file: resultsSourceFile, text: "${passPercentVar}=${value}"
    }

    setPassed 0
    info "starting execution..."

    def executionApi = new LEAPClient(debug, host, token)
    info "custom mesg :  $executionApi"      
    def exec = executionApi.runSuite(suite, project)
    debug exec 
    info "custom mesg :  $exec"      
    info "custom mesg :  $exec['suiteId'] " 
    def status, completed = false, retry = 5
    if (exec != null && exec['suiteId'] != null) {
        info "[leap.execution.id=${exec.id}]"
        while (!completed) {
            sleep 5
            status = executionApi.getExecutionStatus(exec.id)
            if (status && status.jobs) {
                completed = status.completed
                info "completion(${completed}) - ${status.finishedPercentage}%"
            } else {
                completed = --retry == 0
            }
        }
    } else {
        error "unable to trigger suite - ${suite}" + parseError(exec)
    }
    if (status == null || !status.jobs) {
        error "unable to read status for execution - ${exe.name}" + parseError(status)
    } else {
        info "pass percentage - ${status.passPercentage?status.passPercentage+'%':'n/a'}"
        setPassed status.passPercentage ?: 0
        sleep 15
        def execution = executionApi.getExecution(exec.id);
        if(execution !=null && exec['suiteId'] != null){
            id = execution.id
            info "[leap.execution.id=${execution.id}]"
            info "[leap.execution.reportId=${execution.reportId}]"
            info "[leap.execution.reportProjectId=${execution.reportProjectId}]"
        }
    }
    println(id)
    return id
}
