package org.springframework.cloud.gateway.filter;

import org.springframework.context.ApplicationEvent;


/**
 * 路由删除事件
 */
public class RouteDelEvent extends ApplicationEvent {

	private String routeId;

	public RouteDelEvent(Object source, String routeId) {
		super(source);
		this.routeId = routeId;
	}

	public String getRouteId() {
		return routeId;
	}

}