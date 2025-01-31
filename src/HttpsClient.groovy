#!/usr/bin/groovy
import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic
import javax.net.ssl.*
import java.security.KeyStore
import java.io.FileInputStream
@Grab("org.jodd:jodd-http:5.1.4")
import jodd.http.HttpRequest

class HttpsClient {
    String token
    String log
    SSLContext sslContext
    String hostCertificate = null

    HttpsClient(log, token, String host) {
        this.token = token
        this.log = log
        if (host.toLowerCase().startsWith("https")) {
            if (host.toLowerCase().contains("143")) {
                this.hostCertificate = "143.p12"
            } else if (host.toLowerCase().contains("131")) {
                this.hostCertificate = "131.p12"
            } else if (host.toLowerCase().contains("23")) {
                this.hostCertificate = "23.p12"
            }
        }
    }

    createSSLContext(){
        String certFilePath = "C:\\certificates\\" + this.hostCertificate.trim()    
            try {
                String p12Password = "cctp"
                KeyStore keyStore = KeyStore.getInstance("PKCS12")
                FileInputStream keyStoreFile = new FileInputStream(certFilePath)
                keyStore.load(keyStoreFile, p12Password.toCharArray())
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(keyStore)

                SSLContext sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustManagerFactory.getTrustManagers(), null)

                // Set the default SSL context
                SSLContext.setDefault(sslContext)

                //debug "SSL context initialized successfully."
                return sslContext
            } catch (Exception e) {
                debug "Failed to initialize SSL context: ${e.message}"
                return null
            }
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    doGet(String url) {
        try {
            def response
            //debug "requesting -\nGET ${url}"
            sslContext = this.createSSLContext()
            if(sslContext != null){
                URL requestUrl = new URL(url)
                HttpsURLConnection connection = (HttpsURLConnection) requestUrl.openConnection()
                connection.setSSLSocketFactory(sslContext.socketFactory)
                connection.setRequestProperty("Authorization", "Bearer ${token}")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestMethod("GET")

                int responseCode = connection.getResponseCode()
                //debug "Response Code: ${responseCode}"
                response = connection.inputStream.text
            } else{
                response = new HttpRequest().get(url)
                    .tokenAuthentication(token).acceptJson().acceptJson().send().bodyText()
            }
            
            println "response -\n${response}"
            return new JsonSlurperClassic().parseText(response)
        } catch (Exception e) {
            return e
        }
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    doPost(String url, String data = null) {
        def response
        try {
            //debug "requesting -\nPOST ${url}"
            sslContext = this.createSSLContext()
            if(sslContext != null){
                URL requestUrl = new URL(url)
                HttpsURLConnection connection = (HttpsURLConnection) requestUrl.openConnection()
                connection.setSSLSocketFactory(sslContext.socketFactory)
                connection.setRequestProperty("Authorization", "Bearer ${token}")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestMethod("POST")
                connection.setDoOutput(true)

                if (data != null) {
                    connection.outputStream.withWriter('UTF-8') { writer ->
                        writer << JsonOutput.toJson(data)
                    }
                }

                int responseCode = connection.getResponseCode()
                //debug "Response Code: ${responseCode}"
                //println response.responseCode
                response = connection.inputStream.text
            } else{
                response = new HttpRequest().post(url)
                    .tokenAuthentication(token).acceptJson().contentTypeJson().body(
                    data == null ? "" : JsonOutput.toJson(data)
            ).send().bodyText()
            }
            
            println "response -\n${response}"
            return new JsonSlurperClassic().parseText(response)
        } catch (Exception e) {
            //log "url:${url}, exception:${e}"
            return e
        }
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    doPut(String url, def data = null) {
        try {
            //debug "requesting -\nPUT ${url}"
            def response
            sslContext = this.createSSLContext()
            if(sslContext != null){
                URL requestUrl = new URL(url)
                HttpsURLConnection connection = (HttpsURLConnection) requestUrl.openConnection()
                connection.setSSLSocketFactory(sslContext.socketFactory)
                connection.setRequestProperty("Authorization", "Bearer ${token}")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestMethod("PUT")
                connection.setDoOutput(true)

                if (data != null) {
                    connection.outputStream.withWriter('UTF-8') { writer ->
                        writer << JsonOutput.toJson(data)
                    }
                }

                int responseCode = connection.getResponseCode()
                //debug "Response Code: ${responseCode}"
                response = connection.inputStream.text
            } else{
                response = new HttpRequest().put(url)
                    .tokenAuthentication(token).acceptJson().contentTypeJson()
                    .body(JsonOutput.toJson(data))
                    .send().bodyText()
            }
            println "response -\n${response}"
            return new JsonSlurperClassic().parseText(response)
        } catch (Exception e) {
            return e
        }
    }
}