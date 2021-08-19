package com.example.starter;

import com.example.starter.dao.MySqlVerticle;
import com.example.starter.pet.PetPath;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    Router global = Router.router(vertx);

    //集群session
    //SessionStore store = ClusteredSessionStore.create(vertx);
    //本地session
    SessionStore store = LocalSessionStore.create(vertx);

    global.route()
      .handler(SessionHandler.create(store).setCookieless(false).setSessionCookieName("vertx-web-session-id")); //cookieless为cookie会话，即不设置cookie

    global.route().handler(LoggerHandler.create(LoggerFormat.DEFAULT));

    RouterBuilder.create(vertx, "api.yaml", h -> {
      if (h.failed()) {
        System.out.println("解析文档失败");
        h.cause().printStackTrace();
      }
      RouterBuilder routerBuilder = h.result();
      routeConf(routerBuilder);
      Router router = routerBuilder.createRouter();
      //设置一个前缀路由
      global.mountSubRouter("/v1", router);
      vertx.createHttpServer().requestHandler(global).listen(8888, http -> {
        if (http.succeeded()) {
          startPromise.complete();
          System.out.println("HTTP server started on port 8888");
        } else {
          startPromise.fail(http.cause());
        }
      });
    });
  }

  private static RouterBuilder routeConf(RouterBuilder routerBuilder){
    //register pet api
    for (PetPath value : PetPath.values()) {
      routerBuilder.operation(value.name()).handler(value.handler());
    }

    return routerBuilder;
  }

  public static void main(String[] args) {
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
    vertx.deployVerticle(new MySqlVerticle());
  }

}
