package com.example.starter.pet.service;

import static com.example.starter.dao.MySqlVerticle.getPool;

import com.example.starter.pet.Pet;
import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.Tuple;

public class PetService {

  public void getById(int id, Promise<Pet> promise) {
    //find by db

    //直接查询
/*    getPool().preparedQuery("select * from pet where id=?").execute(Tuple.of(id)).onSuccess(rows -> {
      Pet pet = row2Pet(rows.iterator().next());
      promise.complete(pet);
    }).onFailure(ex -> {
          promise.fail(ex.getMessage());
          ex.printStackTrace();
    });*/

    //流方式查询处理
    getPool().getConnection().compose(conn -> {
      Promise<Pet> queryPromise = Promise.promise();
      conn.prepare("select * from pet where id = ?").map(preparedStatement -> preparedStatement.createStream(50, Tuple.of(id))//每次获取50行
      ).onComplete(ar -> {
        if (ar.succeeded()) {
          RowStream<Row> stream = ar.result();
          stream
            .exceptionHandler(queryPromise::fail)
            .handler(row -> {
              Pet pet = row2Pet(row);
              queryPromise.complete(pet);
            });
        } else {
          queryPromise.fail(ar.cause());
        }
      }).onFailure(ex -> {
        queryPromise.fail(ex.getMessage());
        ex.printStackTrace();
      });

      return queryPromise.future().onComplete(v -> {
        // close the connection
        conn.close();
        promise.complete(v.result());
      }).onFailure(ex -> {
        promise.fail(ex.getMessage());
        ex.printStackTrace();
      });
    });


  }

  public void createPet(String name, Promise<Pet> promise) {
    getPool().withTransaction(conn -> conn.preparedQuery("insert into pet (name) value (?)")
      .execute(Tuple.of(name))
      .compose(rows -> {
        long lastInsertId = rows.property(MySQLClient.LAST_INSERTED_ID);
        return conn.preparedQuery("select * from pet where id=?").execute(Tuple.of(lastInsertId));
      })
      .onSuccess(rows -> {
        Pet pet = row2Pet(rows.iterator().next());
        promise.complete(pet);
      })
      .onFailure(ex -> {
        promise.fail(ex.getMessage());
        ex.printStackTrace();
      }));
  }

  private Pet row2Pet(Row row) {
    Pet pet = new Pet();
    pet.setId(row.get(Integer.class, "id"));
    pet.setName(row.get(String.class, "name"));
    return pet;
  }

}
