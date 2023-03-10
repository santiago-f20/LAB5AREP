package co.edu.escuelaing.app;

import static spark.Spark.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class App {

    private static ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>() {
        {
            add("1");
            add("2");
            add("3");
        }
    };

    public static void main(String[] args) {
        staticFiles.location("/public");
        port(getPort());
        post("/log", (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.type("application/json");
            return balancer(req.queryParams("value"));
        });
    }

    static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 4567;
    }

    public static String balancer(String value) {
        String temp = queue.poll();
        queue.add(temp);
        return doPost(value, temp);
    }

    public static String doPost(String value, String server) {
        String linea = "";
        try {
            String data = "value=" + value;
            String url = "http://logservice" + server + ":3500" + server + "/api/backend?" + data;
            System.out.println(url.toString());
            HttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = client.execute(httpPost);
            linea = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } catch (MalformedURLException me) {
            System.err.println("MalformedURLException: " + me);
        } catch (IOException ioe) {
            System.err.println("IOException:  " + ioe);
        }
        return linea;
    }
}
