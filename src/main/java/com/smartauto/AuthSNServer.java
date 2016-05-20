package com.smartauto;


import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

/**
 * Created by sbw22 on 2016/4/19.
 */
public class AuthSNServer {


    public static void main(String[] args) {
        final MongoClient mongo = MongoClient.createNonShared(Vertx.vertx(), new JsonObject().put("db_name", "chleon").put("host", "115.29.177.82").put("port", 27017));
        JsonObject mySQLClientConfig = new JsonObject().put("host", "182.92.186.153")
                .put("port",3306)
                .put("maxPoolSize",30)
                .put("username", "root")
                .put("password", "root")
                .put("database", "standard")
                .put("charset", "utf8");
        final AsyncSQLClient client = MySQLClient.createShared(Vertx.vertx(), mySQLClientConfig);
        Vertx.vertx().createHttpServer().requestHandler(req -> {
            if ("GET".equals(req.method().name()) && "/tree".equals(req.uri())) {
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
            }
            System.out.println(req.path());
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
                client.getConnection(conn -> {
                    if (conn.failed()) {
                        System.err.println(conn.cause().getMessage());
                        return;
                    }
                    final SQLConnection connection = conn.result();
                    connection.query("select * from standard where name like '%" + name + "%' limit " + finalCp + "," + (finalCp + 1) * finalLs, resultSetAsyncResult -> {
                        final JsonArray json = new JsonArray();
                        if (resultSetAsyncResult.failed()) {
                            req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
                            JsonObject jsonObject = new JsonObject().put("status", 2000).put("result", "no data");
                            json.add(jsonObject);
                            req.response().end(json.encode());
                        }
                        ResultSet resultSet = resultSetAsyncResult.result();


                        for (JsonObject o : resultSet.getRows()) {
                            json.add(o);
                        }
                        String end = json.encode() ;
                        req.response().setChunked(false);
                        req.response().putHeader(HttpHeaders.CONTENT_LENGTH,end.length()+"") ;
                        req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
                        req.response().write(json.encode());

                    });
                });
            }

        }).listen(8080);
    }
}
