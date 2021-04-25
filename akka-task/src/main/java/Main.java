import akka.actor.typed.*;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Random;

public class Main {

    public static void main(String[] args) {
        Config config = ConfigFactory.load();
        ActorSystem.create(Main.create(), "system", config.getConfig("configuration"));
    }

    public static Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
                    ActorRef<Command> dispatcher = context.spawn(
                            Behaviors.supervise(Dispatcher.create())
                                    .onFailure(Exception.class, SupervisorStrategy.resume()), "dispatcher", DispatcherSelector.fromConfig("my-dispatcher"));

                    ActorRef<Command> monitoringStation1 = context.spawn(
                            Behaviors.supervise(MonitoringStation.create())
                                    .onFailure(Exception.class, SupervisorStrategy.resume()), "monitoringStation1", DispatcherSelector.fromConfig("my-dispatcher"));
                    ActorRef<Command> monitoringStation2 = context.spawn(
                            Behaviors.supervise(MonitoringStation.create())
                                    .onFailure(Exception.class, SupervisorStrategy.resume()), "monitoringStation2", DispatcherSelector.fromConfig("my-dispatcher"));
                    ActorRef<Command> monitoringStation3 = context.spawn(
                            Behaviors.supervise(MonitoringStation.create())
                                    .onFailure(Exception.class, SupervisorStrategy.resume()), "monitoringStation3", DispatcherSelector.fromConfig("my-dispatcher"));

                    Thread.sleep(2000);

                    //test case parameters
                    Random rand = new Random();
                    int firstSatId = 100 + rand.nextInt(50);
                    int range = 50;
                    int timeout = 300;

                    MonitoringStation.Init init = new MonitoringStation.Init(dispatcher, firstSatId, range, timeout);

                    monitoringStation1.tell(init);
                    monitoringStation1.tell(init);
                    monitoringStation2.tell(init);
                    monitoringStation2.tell(init);
                    monitoringStation3.tell(init);
                    monitoringStation3.tell(init);

                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }
}
