import java.util.Map;

public class StatusQueryResponse implements Command {
    public final int queryId;
    public final Map<Integer, SatelliteAPI.Status> statusMap;
    public final int respondedInRequiredTimePercentage;

    /**
     * @param queryId                           identifier of the query to which the response relates
     * @param statusMap                         status for satellites that returned an error
     *                                          i.e. a status other than OK (we do not send those that returned OK)
     * @param respondedInRequiredTimePercentage percentage of satellites that replied in the required time
     */
    public StatusQueryResponse(int queryId, Map<Integer, SatelliteAPI.Status> statusMap, int respondedInRequiredTimePercentage) {
        this.queryId = queryId;
        this.statusMap = statusMap;
        this.respondedInRequiredTimePercentage = respondedInRequiredTimePercentage;
    }
}
