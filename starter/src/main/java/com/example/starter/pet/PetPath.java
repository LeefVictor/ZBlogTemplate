package com.example.starter.pet;

import com.example.starter.pet.service.PetService;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import java.util.List;

public enum PetPath implements CacheHandler {
  //如果有多个handler要处理， 且值需要传递， 那么可以用 routingcontext.put() 和 get()两个方法，

  getPet() {
    @Override
    public Handler<RoutingContext> handler() {
      return r -> {
        Integer id = Integer.parseInt(r.queryParam("id").get(0));
        System.out.println("id is " + id);

        Promise<Pet> promise = Promise.promise();
        promise.future().onSuccess(pet->{
          //已经在文档那里指定了contentType等属性， 就不需要自己再put了
          r.response().end(Json.encodePrettily(pet));
        });

        PetService petService = new PetService();
        petService.getById(id, promise);


      };
    }
  },

  getPetWithPath() {
    @Override
    public Handler<RoutingContext> handler() {
      return r -> {
        Integer id = Integer.parseInt(r.pathParam("id"));
        System.out.println("id is " + id);
        //已经在文档那里指定了contentType等属性， 就不需要自己再put了
        r.response().end("{\"id\":" + id + ", \"name\":\"B\"}");
      };
    }
  },

  createPets() {
    @Override
    public Handler<RoutingContext> handler() {
      return r -> {
        Pet pet = Json.decodeValue(r.getBody(), Pet.class);
        Future<Boolean> exist = Future.future(h->h.complete(Boolean.TRUE));
        Future<String> renameName = Future.future(h->h.complete(pet.getName()+"-rrrr"));

        Promise<Pet> respon = Promise.promise();
        respon.future().onSuccess(p->{
          //已经在文档那里指定了contentType等属性， 就不需要自己再put了
          System.out.println(r.session().id());
          r.response().end(Json.encodePrettily(pet));
        });

        Promise<Pet> promise = Promise.promise();
        promise.future().onComplete(p->{
          respon.complete(p.result());
        });

        CompositeFuture.all(exist, renameName).onComplete(h->{
          List list = h.result().list();
          if ((Boolean)list.get(0)) {
            PetService petService = new PetService();
            petService.createPet(pet.getName(), promise);
          }
        });
      };
    }
  },
  petWithForm() {
    @Override
    public Handler<RoutingContext> handler() {
      return r -> {
        int id = Integer.parseInt(r.request().getFormAttribute("id"));
        //r.request().getHeader()
        System.out.println(id);
        //已经在文档那里指定了contentType等属性， 就不需要自己再put了
        r.response().end("{\"id\":" + id + ", \"name\":\"C\"}");
      };
    }
  };


  public abstract Handler<RoutingContext> handler();

  @Override
  public Handler<RoutingContext> cacheHandler() {
    return r->{
      Object obj = null;//from redis
      if (obj != null) {
        System.out.println("use cache");
        r.response().end(Json.encodePrettily(obj));
      } else {
        System.out.println("no cache data, next handler");
        r.next();
      }
    };
  }
}
