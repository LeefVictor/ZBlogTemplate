package com.example.starter.dao;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;

public class MySqlVerticle extends AbstractVerticle {

  private static MySQLPool pool;

  public static MySQLPool getPool() {
    return pool;
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("xxx")
      .setDatabase("test_db")
      .setUser("xxx")
      .setPassword("xxxx");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);
    // Create the pooled client
    pool = MySQLPool.pool(vertx, connectOptions, poolOptions);
  }
}
