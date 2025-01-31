#!/usr/bin/groovy

class RBotClient extends HttpsClient {
    private String host

    RBotClient(log, host, token) {
        String hostCertificate = null 

        if (host.toLowerCase().startsWith("https")) {
            if (host.toLowerCase().contains("143")) {
                hostCertificate = "143.p12"
            } else if (host.toLowerCase().contains("131")) {
                hostCertificate = "131.p12"
            } else if (host.toLowerCase().contains("23")) {
                hostCertificate = "23.p12"
            }
        }
        super(log, token, hostCertificate)
        this.host = host
    }

    private getRecommendedTestsUrl(String projectId, String modelId) {
        return "${host}/systemRecommendedTcs?projectid=${projectId}&modelid=${modelId}"
    }

    def getRecommendedTest(projectId, modelId) {
        return super.doGet(getRecommendedTestsUrl(projectId, modelId))
    }

    def isEmpty(list) {
        list == []
    }

    def resolvePattern(pattern, list, sep = ' ') {
        list.collect { map -> pattern.replaceAll(/\{(\w+)\}/) { match, key -> map[key ?: match[1]] } }.join(sep)
    }
}
