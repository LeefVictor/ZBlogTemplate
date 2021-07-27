package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.OperationException;
import com.ecwid.consul.v1.agent.model.NewService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

/**
 * Created by nicu on 11.03.2015.
 *
 * @author Stéphane LEROY
 */
public class TtlScheduler {

	private static final Log log = LogFactory.getLog(ConsulDiscoveryClient.class);

	private final Map<String, ScheduledFuture> serviceHeartbeats = new ConcurrentHashMap<>();

	private final TaskScheduler scheduler = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());

	private final HeartbeatProperties heartbeatProperties;

	private final ConsulDiscoveryProperties discoveryProperties;

	private final ConsulClient client;

	private final ReregistrationPredicate reregistrationPredicate;

	private final Map<String, NewService> registeredServices = new ConcurrentHashMap<>();

	public TtlScheduler(HeartbeatProperties heartbeatProperties, ConsulDiscoveryProperties discoveryProperties,
			ConsulClient client, ReregistrationPredicate reregistrationPredicate) {
		this.heartbeatProperties = heartbeatProperties;
		this.discoveryProperties = discoveryProperties;
		this.client = client;
		this.reregistrationPredicate = reregistrationPredicate;
	}

	public void add(final NewService service) {
		add(service.getId());
		this.registeredServices.put(service.getId(), service);
	}

	/**
	 * Add a service to the checks loop.
	 * @param instanceId instance id
	 */
	public void add(String instanceId) {
		ScheduledFuture task = this.scheduler.scheduleAtFixedRate(new ConsulHeartbeatTask(instanceId, this),
				this.heartbeatProperties.computeHeartbeatInterval().toMillis());
		ScheduledFuture previousTask = this.serviceHeartbeats.put(instanceId, task);
		if (previousTask != null) {
			previousTask.cancel(true);
		}
	}

	public void remove(String instanceId) {
		ScheduledFuture task = this.serviceHeartbeats.get(instanceId);
		if (task != null) {
			task.cancel(true);
		}
		this.serviceHeartbeats.remove(instanceId);
		this.registeredServices.remove(instanceId);
	}

	static class ConsulHeartbeatTask implements Runnable {

		private final String serviceId;

		private final String checkId;

		private final TtlScheduler ttlScheduler;

		ConsulHeartbeatTask(String serviceId, TtlScheduler ttlScheduler) {
			this.serviceId = serviceId;
			if (!this.serviceId.startsWith("service:")) {
				this.checkId = "service:" + this.serviceId;
			}
			else {
				this.checkId = this.serviceId;
			}
			this.ttlScheduler = ttlScheduler;
		}

		@Override
		public void run() {
			try {
				this.ttlScheduler.client.agentCheckPass(this.checkId, null, ttlScheduler.discoveryProperties.getAclToken());
				if (log.isDebugEnabled()) {
					log.debug("Sending consul heartbeat for: " + this.checkId);
				}
			}
			catch (OperationException e) {
				if (this.ttlScheduler.heartbeatProperties.isReregisterServiceOnFailure()
						&& this.ttlScheduler.reregistrationPredicate.isEligible(e)) {
					log.warn(e.getMessage());
					NewService registeredService = this.ttlScheduler.registeredServices.get(this.serviceId);
					if (registeredService != null) {
						if (log.isInfoEnabled()) {
							log.info("Re-register " + registeredService);
						}
						this.ttlScheduler.client.agentServiceRegister(registeredService,
								this.ttlScheduler.discoveryProperties.getAclToken());
					}
					else {
						log.warn("The service to re-register is not found.");
					}
				}
				else {
					throw e;
				}
			}
		}

	}

}
