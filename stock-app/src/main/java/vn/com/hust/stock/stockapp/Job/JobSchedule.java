package vn.com.hust.stock.stockapp.Job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Inflater;

@Component
public class JobSchedule {


    private  RestTemplate restTemplate;

    private  ObjectMapper objectMapper;

    @Autowired
    public JobSchedule( ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        restTemplate= new RestTemplate();
        restTemplate.setRequestFactory(createRequestFactory());
        sendRequest();
    }



    private static HttpComponentsClientHttpRequestFactory createRequestFactory() {
        try {
            SSLContextBuilder sslContext = new SSLContextBuilder();
            sslContext.loadTrustMaterial(null, new TrustAllStrategy());
            CloseableHttpClient client = HttpClients.custom().setSSLContext(sslContext.build()).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(client);
            return requestFactory;
        } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException var3) {
            throw new IllegalStateException("Couldn't create HTTP Request factory ignore SSL cert validity: ", var3);
        }
    }




    public ResponseEntity<String> sendRequest() {

        ResponseEntity<byte[]> response
                = restTemplate.getForEntity("https://cophieu68.vn/export/metastock_all.php", byte[].class);
        byte[] data = decompressByteArray(response.getBody());
        System.out.println(openFileToString(data));
       return null;
    }
    public String openFileToString(byte[] _bytes)
    {
        String file_string = "";

        for(int i = 0; i < _bytes.length; i++)
        {
            file_string += (char)_bytes[i];
        }

        return file_string;
    }

    public byte[] decompressByteArray(byte[] bytes){

        ByteArrayOutputStream baos = null;
        Inflater iflr = new Inflater();
        iflr.setInput(bytes);
        baos = new ByteArrayOutputStream();
        byte[] tmp = new byte[4*1024];
        try{
            while(!iflr.finished()){
                int size = iflr.inflate(tmp);
                baos.write(tmp, 0, size);
            }
        } catch (Exception ex){

        } finally {
            try{
                if(baos != null) baos.close();
            } catch(Exception ex){}
        }

        return baos.toByteArray();
    }
}
