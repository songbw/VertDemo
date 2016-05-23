package com.smartauto;


import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;

/**
 * Created by sbw22 on 2016/4/19.
 */
public class AuthSNServer {


    public static void main(String[] args) throws Exception {
        final MongoClient mongo = MongoClient.createNonShared(Vertx.vertx(), new JsonObject().put("db_name", "chleon").put("host", "115.29.177.82").put("port", 27017));
        JsonObject mySQLClientConfig = new JsonObject().put("host", "182.92.186.153")
                .put("port",3306)
                .put("maxPoolSize",30)
                .put("username", "root")
                .put("password", "root")
                .put("database", "standard")
                .put("charset", "utf8");
        final AsyncSQLClient client = MySQLClient.createShared(Vertx.vertx(), mySQLClientConfig);

        Future<List<JsonObject>> fut1 = Future.future();
        Future future = Future.future() ;
        Future future1 = Future.future() ;
        Vertx.vertx().createHttpServer().requestHandler(req -> {

            if ("GET".equals(req.method().name()) && "/tree".equals(req.uri())) {
                future1.complete(req);
                JsonArray json = new JsonArray();
                mongo.find("BZJTree", new JsonObject(), fut1.completer());
                for (JsonObject o : fut1.result()) {
                    json.add(o);
                }
                req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                req.response().end(json.encode());
            }
            if ("GET".equals(req.method().name()) && "/list".equals(req.path())) {
                String name = req.getParam("name");
                if (name == null || "".equals(name)) {
                    req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
                    req.response().end(new JsonObject().put("status", 2001).put("result", "name is null.").encode());
                }
                String cpString = req.getParam("cp");
                String lsString = req.getParam("ls");
                int cp = 0;
                int ls = 10;
                if (cpString != null && !"".equals(cpString)) {
                    cp = Integer.valueOf(cpString);
                }
                if (lsString != null && !"".equals(lsString)) {
                    ls = Integer.valueOf(lsString);
                }
                final int finalCp = cp;
                final int finalLs = ls;
                client.getConnection(future.completer());

                if (future.failed()) {
                    System.err.println(future.cause().getMessage());
                    return;
                }
                JsonArray json = new JsonArray();
                future.compose(v -> {
                    if (future.succeeded()) {
                        System.out.println("successed");
                        SQLConnection connection = (SQLConnection) future.result();
                        connection.query("select * from standard where name like '%" + name + "%' limit " + finalCp + "," + (finalCp + 1) * finalLs, future1.completer());
                    }
                }, future1);
                if (future1.failed()) {
                    req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
                    JsonObject jsonObject = new JsonObject().put("status", 2000).put("result", "no data");
                    json.add(jsonObject);
                    req.response().end(json.encode());
                } else {
                    ResultSet resultSet = (ResultSet) future1.result();
                    resultSet.getResults().forEach(json::add);

                    req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
                    req.response().end(json.encode());
                }
            }
        }).listen(8080);
    }
}
