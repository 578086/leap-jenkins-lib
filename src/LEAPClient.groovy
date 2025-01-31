#!/usr/bin/groovy

class LEAPClient extends HttpsClient {
    private String exec_api
    def log
    
    LEAPClient(log, host, token) {
        super(log, token, host)
        this.log = log
        this.exec_api = host.endsWith('execution') ? host : host + '/execution'
    }

    private getRunSuiteUrl(project, suite) {
      
        return "${exec_api}/suites/execute?name=${suite}&projectName=${project}"
    }

    private suiteUrl() {
        return "${exec_api}/suites"
    }

    private getExecutionStatusUrl(id) {
        return getExecutionUrl(id) + "/suite-report"
    }

    private getSearchSuitesUrl(project) {
        return "${exec_api}/suites/search?projectName=${project}"
    }
    private getExecutionUrl(id){
        return "${exec_api}/executions/${id}"
    }

    def findSuite(suite, project) {
        def suites = super.doGet(getSearchSuitesUrl(project))
        return suites != null ? suites.find { (it.name == suite) } : suites
    }

    def updateSuiteTests(tests, suiteName, project) {
        def suite = findSuite(suiteName, project)
        if (suite != null) {
            tests = tests.split(",")
            log "test to update ${tests}"
            suite.task.tasks = tests.collect { test ->
                [name: test, type: 'test']
            }
            log "updating suite {suite}"
            return super.doPut(suiteUrl() , suite)
        }
        throw new Error("unable to find suite ${project}/${suiteName}")
    }

    def runSuite(String suite, String project) {
        return super.doPost(getRunSuiteUrl(project, suite))
    }

    def getExecutionStatus(String id) {
        return super.doGet(getExecutionStatusUrl(id))
    }
    def getExecution(String id){
        return super.doGet(getExecutionUrl(id))
    }
}
