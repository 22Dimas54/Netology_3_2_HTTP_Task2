import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static final String REMOTE_SERVICE_URI = "https://api.nasa.gov/planetary/apod?api_key=77881U2bqY9XCtdiNU9hmx1n7fmFnd1tAuJgdFSO";
    public static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        HttpGet request = new HttpGet(REMOTE_SERVICE_URI);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build();
             CloseableHttpResponse response = httpClient.execute(request);) {
            Arrays.stream(response.getAllHeaders()).forEach(System.out::println);
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            List<ApiNASA> apiNASAS = mapper.readValue(response.getEntity().getContent(), new TypeReference<>() {
            });
            for (ApiNASA apiNASA : apiNASAS) {
                HttpGet requestHdurl = new HttpGet(apiNASA.getHdurl());
                requestHdurl.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
                try (CloseableHttpResponse responseHdurl = httpClient.execute(requestHdurl)) {
                    HttpEntity entity = responseHdurl.getEntity();
                    if (entity != null) {
                        File file = new File(Paths.get(new URI(apiNASA.getHdurl()).getPath()).getFileName().toString());
                        try (FileOutputStream outstream = new FileOutputStream(file)) {
                            entity.writeTo(outstream);
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
