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

    HttpsClient(log, token, String host) {
        this.token = token
        this.log = log
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
        if(hostCertificate != null){
            String certFilePath = "C:\\certificates\\" + hostCertificate.trim()    
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
                this.sslContext = sslContext
            } catch (Exception e) {
                debug "Failed to initialize SSL context: ${e.message}"
                this.sslContext = null
            }
        } else{
            this.sslContext = null
        }
        
       
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    doGet(String url) {
        try {
            //debug "requesting -\nGET ${url}"
            if(sslContext != null){
                URL requestUrl = new URL(url)
                HttpsURLConnection connection = (HttpsURLConnection) requestUrl.openConnection()
                connection.setSSLSocketFactory(sslContext.socketFactory)
                connection.setRequestProperty("Authorization", "Bearer ${token}")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestMethod("GET")

                int responseCode = connection.getResponseCode()
                //debug "Response Code: ${responseCode}"
                def response = connection.inputStream.text
            } else{
                def response = new HttpRequest().get(url)
                    .tokenAuthentication(token).acceptJson().acceptJson().send().bodyText()
            }
            
            //debug "response -\n${response}"
            return new JsonSlurperClassic().parseText(response)
        } catch (Exception e) {
            log e
        }
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    doPost(String url, String data = null) {
        try {
            //debug "requesting -\nPOST ${url}"
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
                def response = connection.inputStream.text
            } else{
                def response = new HttpRequest().post(url)
                    .tokenAuthentication(token).acceptJson().contentTypeJson().body(
                    data == null ? "" : JsonOutput.toJson(data)
            ).send().bodyText()
            }
            
            //debug "response -\n${response}"
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
                def response = connection.inputStream.text
            } else{
                def response = new HttpRequest().put(url)
                    .tokenAuthentication(token).acceptJson().contentTypeJson()
                    .body(JsonOutput.toJson(data))
                    .send().bodyText()
            }
            //debug "response -\n${response}"
            return new JsonSlurperClassic().parseText(response)
        } catch (Exception e) {
            log e
        }
    }
}