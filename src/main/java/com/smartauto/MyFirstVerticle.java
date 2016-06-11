package com.smartauto;

import com.smartauto.utils.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.util.Iterator;
import java.util.List;

/**
 * Created by song on 16/6/11.
 */
public class MyFirstVerticle extends AbstractVerticle {

    public static final String COLLECTION = "BZJTree";
    private MongoClient mongo;
    private AsyncSQLClient client ;

    public static void main(String[] args) {
        Runner.runExample(MyFirstVerticle.class);
    }

    @Override
    public void stop() throws Exception {
        mongo.close();
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        mongo = MongoClient.createShared(vertx, new JsonObject().put("db_name", "chleon").put("host", "115.29.177.82").put("port", 27017)) ;
        client = MySQLClient.createShared(vertx, new JsonObject().put("host", "182.92.186.153")
                .put("port",3306)
                .put("maxPoolSize",30)
                .put("username", "root")
                .put("password", "root")
                .put("database", "standard")
                .put("charset", "utf8")) ;
                startWebApp((http) -> completeStartup(http, startFuture));
    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        vertx.createHttpServer().requestHandler(req -> {

            if ("GET".equals(req.method().name()) && "/tree".equals(req.uri())) {

                JsonArray json = new JsonArray();
                mongo.find("BZJTree", new JsonObject(), result -> {

                    for (JsonObject o : result.result()) {
                        json.add(o);
                    }

                    req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                    req.response().end(json.encode());
                });

            }

            if ("GET".equals(req.method().name()) && "/list".equals(req.path())) {
                String name = req.getParam("name");
                System.out.println(name);
                if (name == null || "".equals(name)) {
                    req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
                    req.response().end(new JsonObject().put("status", 2001).put("result", "name is null.").encode());
                } else {
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
//                    JsonArray json = new JsonArray();
                    client.getConnection(res -> {
                        SQLConnection connection = res.result();
                        connection.query("select * from standard where name like '%" + name + "%' limit " + finalCp + "," + (finalCp + 1) * finalLs, result -> {
                            ResultSet resultSet = (ResultSet) result.result();
                            Iterator<JsonArray> iterator = resultSet.getResults().iterator();
                            JsonArray json = new JsonArray();
                            while (iterator.hasNext()) {
                                JsonObject jsonObject = new JsonObject();
                                List list = iterator.next().getList();
                                for (int i = 0; i < 14; i++) {
                                    jsonObject.put(resultSet.getColumnNames().get(i), list.get(i));
                                }
                                json.add(jsonObject);
                            }
                            System.out.println(json.encode());
                            JsonObject results = new JsonObject() ;
                            results.put("list",json) ;
                            req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
                            req.response().end(results.encode());
                        });
                    });
                }
            }

        }).listen(config().getInteger("http.port", 8080), next::handle) ;

    }

    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
        if (http.succeeded()) {
            fut.complete();
        } else {
            fut.fail(http.cause());
        }
    }
}
