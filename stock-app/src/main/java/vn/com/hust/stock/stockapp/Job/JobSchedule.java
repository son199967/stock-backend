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

import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.*;


@Component
public class JobSchedule {


    private  RestTemplate restTemplate;

    private  ObjectMapper objectMapper;

    @Autowired
    public JobSchedule( ObjectMapper objectMapper) throws IOException {
        this.objectMapper = objectMapper;
        restTemplate= new RestTemplate();
        restTemplate.setRequestFactory(createRequestFactory());
//        sendRequest();
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

    public ResponseEntity<String> sendRequest() throws IOException {

//        ResponseEntity<String> response
//                = restTemplate.getForEntity("https://cophieu68.vn/export/metastock_all.php", String.class);
//        this.writeFile(response.getBody(),"D:\\data.zip");
        unzipAbc("D:\\data.zip","D:\\data2");

//        convert(response.getBody());
       return null;
    }
    public void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdirs();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    public  void unzipAbc( String source,       String destination) throws IOException {

        ZipFile zipFile = new ZipFile(source);

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            InputStream stream = zipFile.getInputStream(entry);
            System.out.println(stream);
        }
    }
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[40096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }


    public byte[] convert(String orig) throws IOException {

        // Compress it
        ByteArrayOutputStream baostream = new ByteArrayOutputStream();
        OutputStream outStream = new GZIPOutputStream(baostream);
        outStream.write(orig.getBytes("UTF-8"));
        outStream.close();
        byte[] compressedBytes = baostream.toByteArray(); // toString not always possible

        // Uncompress it
        InputStream inStream = new GZIPInputStream(
                new ByteArrayInputStream(compressedBytes));
        ByteArrayOutputStream baoStream2 = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = inStream.read(buffer)) > 0) {
            baoStream2.write(buffer, 0, len);
        }
        String uncompressedStr = baoStream2.toString("UTF-8");

        System.out.println("orig: " + orig);
        System.out.println("unc:  " + uncompressedStr);
       return null;
    }
    public void writeFile(String data,String path){
        try {
            FileWriter fw = new FileWriter(path);
            fw.write(data);
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("Success...");
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
