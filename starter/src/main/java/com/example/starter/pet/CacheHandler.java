package com.example.starter.pet;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface CacheHandler {



  Handler<RoutingContext> cacheHandler();

}
