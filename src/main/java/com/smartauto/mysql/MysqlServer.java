package com.smartauto.mysql;

import com.smartauto.utils.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;

/**
 * Created by song on 16/6/1.
 */
public class MysqlServer extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(MysqlServer.class);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        String name= "123" ;
        int finalCp = 0;
        int finalLs = 10;
        final JsonObject mySQLClientConfig = new JsonObject().put("host", "182.92.186.153")
                .put("port",3306)
                .put("maxPoolSize",30)
                .put("username", "root")
                .put("password", "root")
                .put("database", "standard")
                .put("charset", "utf8");
        final AsyncSQLClient client = MySQLClient.createShared(Vertx.vertx(), mySQLClientConfig);
        client.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection sqlConnection = res.result() ;
//                sqlConnection.query("select * from standard where name like '%" + name + "%' limit " + finalCp + "," + (finalCp + 1) * finalLs, startFuture.completer());
            }
        });
    }
}
