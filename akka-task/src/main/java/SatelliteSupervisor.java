import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.SortedMap;
import java.util.TreeMap;

public class SatelliteSupervisor extends AbstractBehavior<Command> {
    private int nextSatelliteId;
    private final SortedMap<Integer, SatelliteAPI.Status> statusMap;
    private int numberOfSatellites;
    private int respondedInRequiredTime;
    private StatusQuery statusQueryCopy;

    public SatelliteSupervisor(ActorContext<Command> context) {
        super(context);
        nextSatelliteId = 1;
        statusMap = new TreeMap<>();
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(SatelliteSupervisor::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StatusQuery.class, this::onStatusQuery)
                .onMessage(SingleSatelliteStatusQueryResponse.class, this::onSingleSatelliteStatusQueryResponse)
                .build();
    }

    public static class SingleSatelliteStatusQuery implements Command {
        public final int satId;
        public final int timeout;
        public final ActorRef<Command> replyTo;

        public SingleSatelliteStatusQuery(int satId, int timeout, ActorRef<Command> replyTo) {
            this.satId = satId;
            this.timeout = timeout;
            this.replyTo = replyTo;
        }
    }

    public static class SingleSatelliteStatusQueryResponse implements Command {
        public final int satId;
        public final long responseTime;
        public final int timeout;
        public final SatelliteAPI.Status status;

        public SingleSatelliteStatusQueryResponse(int satId, long responseTime, int timeout, SatelliteAPI.Status status) {
            this.satId = satId;
            this.responseTime = responseTime;
            this.timeout = timeout;
            this.status = status;
        }
    }

    private Behavior<Command> onStatusQuery(StatusQuery statusQuery) {
        this.respondedInRequiredTime = 0;
        this.numberOfSatellites = statusQuery.range;
        this.nextSatelliteId = statusQuery.firstSatId;

        this.statusQueryCopy = new StatusQuery(
                statusQuery.queryId, statusQuery.firstSatId, statusQuery.range, statusQuery.timeout, statusQuery.replyTo
        );

        for (int satId = statusQuery.firstSatId; satId < (statusQuery.firstSatId + statusQuery.range); satId++) {
            SatelliteSupervisor.SingleSatelliteStatusQuery singleSatelliteStatusQuery =
                    new SatelliteSupervisor.SingleSatelliteStatusQuery(
                            nextSatelliteId, statusQuery.timeout, getContext().getSelf()
                    );

            ActorRef<Command> satellite = getContext().spawn(
                    Behaviors.supervise(Satellite.create())
                            .onFailure(Exception.class, SupervisorStrategy.restart()), "satellite" + nextSatelliteId++, DispatcherSelector.fromConfig("my-dispatcher"));

            satellite.tell(singleSatelliteStatusQuery);
        }
        return this;
    }

    private Behavior<Command> onSingleSatelliteStatusQueryResponse(SingleSatelliteStatusQueryResponse response) {
        numberOfSatellites--;
        if (response.responseTime <= response.timeout) {
            if (response.status != SatelliteAPI.Status.OK) {
                statusMap.put(response.satId, response.status);
            }
            respondedInRequiredTime++;
        }
        if (numberOfSatellites == 0) {
            int respondedInRequiredTimePercentage = respondedInRequiredTime * 100 / statusQueryCopy.range;
            StatusQueryResponse statusQueryResponse = new StatusQueryResponse(statusQueryCopy.queryId, statusMap, respondedInRequiredTimePercentage);
            this.statusQueryCopy.replyTo.tell(statusQueryResponse);
            return Behaviors.stopped();
        }
        return this;
    }
}
