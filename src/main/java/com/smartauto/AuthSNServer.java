package com.smartauto;


import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * Created by sbw22 on 2016/4/19.
 */
public class AuthSNServer {

    public static void main(String[] args) {
        final MongoClient mongo = MongoClient.createNonShared(Vertx.vertx(), new JsonObject().put("db_name", "chleon").put("host", "115.29.177.82").put("port", 27017));
        Vertx.vertx().createHttpServer().requestHandler(req -> {
            mongo.find("BZJTree", new JsonObject(), lookup -> {
                // error handling
                final JsonArray json = new JsonArray();
                if (lookup.failed()) {
                    req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                    req.response().end(json.encode());
                }

                for (JsonObject o : lookup.result()) {
                    json.add(o);
                }

                req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                req.response().end(json.encode());
            });
        }).listen(8080);
    }
}
