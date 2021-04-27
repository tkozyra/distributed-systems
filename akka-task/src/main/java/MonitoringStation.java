import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MonitoringStation extends AbstractBehavior<Command> {

    private int nextQueryId;
    private final Map<Integer, Long> times;

    public MonitoringStation(ActorContext<Command> context) {
        super(context);
        this.nextQueryId = 1;
        times = new HashMap<>();
    }

    public static class Init implements Command {
        final ActorRef<Command> dispatcher;
        public final int firstSatId;
        public final int range;
        public final int timeout;

        public Init(ActorRef<Command> dispatcher, int firstSatId, int range, int timeout) {
            this.dispatcher = dispatcher;
            this.firstSatId = firstSatId;
            this.range = range;
            this.timeout = timeout;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(MonitoringStation::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StatusQueryResponse.class, this::onStatusQueryResponse)
                .onMessage(Init.class, this::onInit)
                .build();
    }

    private Behavior<Command> onInit(Init init) {
        times.put(nextQueryId, new Date().getTime());
        StatusQuery statusQuery = new StatusQuery(nextQueryId++, init.firstSatId, init.range, init.timeout, getContext().getSelf());
        init.dispatcher.tell(statusQuery);
        return this;
    }

    private Behavior<Command> onStatusQueryResponse(StatusQueryResponse statusQueryResponse) {
        Long responseTime = new Date().getTime() - this.times.remove(statusQueryResponse.queryId);
        StringBuilder stringBuilder = new StringBuilder();
        System.out.println("--------------STATUS QUERY RESPONSE-------------");
        stringBuilder.append("Monitoring station name: ")
                .append(getContext().getSelf().path().name())
                .append("\nResponse time: ")
                .append(responseTime)
                .append("ms")
                .append("\nSuccess rate: ")
                .append(statusQueryResponse.respondedInRequiredTimePercentage)
                .append("%")
                .append("\nNumber of errors: ")
                .append(statusQueryResponse.statusMap.size())
                .append("\nList of errors: ");

        for (Integer key : statusQueryResponse.statusMap.keySet()) {
            stringBuilder.append("\n\tSatellite ID: ")
                    .append(key)
                    .append("\tError: ")
                    .append(statusQueryResponse.statusMap.get(key));
        }

        System.out.println(stringBuilder.toString());
        System.out.println("------------------------------------------------\n");

        return this;
    }
}
