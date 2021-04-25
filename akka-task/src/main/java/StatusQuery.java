import akka.actor.typed.ActorRef;

public class StatusQuery implements Command {
    public final int queryId;
    public final int firstSatId;
    public final int range;
    public final int timeout;
    public final ActorRef<Command> replyTo;

    /**
     * @param queryId    query identifier (unique within monitoring station)
     * @param firstSatId id of the first satellite in the range chosen to monitor
     * @param range      the number of consecutive satellites for which the status is to be returned
     * @param timeout    the maximum waiting time for data from a single satellite
     * @param replyTo    actor to reply to
     */
    public StatusQuery(int queryId, int firstSatId, int range, int timeout, ActorRef<Command> replyTo) {
        this.queryId = queryId;
        this.firstSatId = firstSatId;
        this.range = range;
        this.timeout = timeout;
        this.replyTo = replyTo;
    }
}
