package co.edu.escuelaing.app;

import static spark.Spark.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.MongoCollection;

import org.bson.Document;

public class App {

    private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public static void main(String[] args) {
        port(getPort());
        post("/api/backend", (req, res) -> {
            res.type("application/json");
            System.out.println(req.queryParams("value"));
            return insert(req.queryParams("value"));
        });
    }

    private static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 4567;
    }

    private static String findLastTenValues(MongoCollection<Document> collection) {
        int index = (int) collection.countDocuments() - 11;
        System.out.println(index);
        ArrayList<String> res = new ArrayList<>();
        collection.find(Filters.gt("id", index)).forEach((Consumer<Document>) (Document d) -> res.add(d.toJson()));
        return Arrays.toString(res.toArray(new String[res.size()]));
    }

    private static String insert(String a) {
        MongoClient mongoClient = new MongoClient("db");
        MongoDatabase db = mongoClient.getDatabase("logservice");
        MongoCollection<Document> collection = db.getCollection("data");
        Document document = new Document();
        System.out.println((int) collection.countDocuments());
        document.append("id", (int) collection.countDocuments());
        document.append("value", a);
        document.append("date", formatter.format(new Date()));
        collection.insertOne(document);
        String res = findLastTenValues(collection);
        mongoClient.close();
        return res;
    }
}
