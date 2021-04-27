import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Date;

public class Satellite extends AbstractBehavior<Command> {

    public Satellite(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Satellite::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(SatelliteSupervisor.SingleSatelliteStatusQuery.class, this::onSingleSatelliteStatusQuery)
                .build();
    }

    private Behavior<Command> onSingleSatelliteStatusQuery(SatelliteSupervisor.SingleSatelliteStatusQuery singleSatelliteStatusQuery) {
        long startTime, endTime, responseTime;

        startTime = new Date().getTime();
        SatelliteAPI.Status status = SatelliteAPI.getStatus(singleSatelliteStatusQuery.satId);
        endTime = new Date().getTime();
        responseTime = endTime - startTime;

        SatelliteSupervisor.SingleSatelliteStatusQueryResponse singleSatelliteStatusQueryResponse
                = new SatelliteSupervisor.SingleSatelliteStatusQueryResponse(singleSatelliteStatusQuery.satId, responseTime, singleSatelliteStatusQuery.timeout, status);

        singleSatelliteStatusQuery.replyTo.tell(singleSatelliteStatusQueryResponse);

        return Behaviors.stopped();
    }
}
