import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Dispatcher extends AbstractBehavior<Command> {
    private int nextSatelliteSupervisorId;

    public Dispatcher(ActorContext<Command> context) {
        super(context);
        nextSatelliteSupervisorId = 1;
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Dispatcher::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StatusQuery.class, this::onStatusQuery)
                .build();
    }

    private Behavior<Command> onStatusQuery(StatusQuery statusQuery) {
        ActorRef<Command> sat = getContext().spawn(
                Behaviors.supervise(SatelliteSupervisor.create())
                        .onFailure(Exception.class, SupervisorStrategy.resume()), "satelliteSupervisor" + nextSatelliteSupervisorId++, DispatcherSelector.fromConfig("my-dispatcher"));
        sat.tell(statusQuery);
        return this;
    }
}
