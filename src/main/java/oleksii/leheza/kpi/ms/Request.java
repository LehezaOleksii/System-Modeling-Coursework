package oleksii.leheza.kpi.ms;

public class Request {

    private static int generalId = 1;

    private int id;
    private double processingTime;
    private String requestType;
    private double createdTime;
    private double startProcessingTime;
    private double waitingTime;

    public Request(double createdTime, double processingTime, String requestType) {
        id = generalId;
        generalId += 1;
        this.createdTime = createdTime;
        this.processingTime = processingTime;
        this.requestType = requestType;
        System.out.println("Generated Request ID: " + id);
    }

    public double getProcessingTime() {
        return processingTime;
    }

    public int getId() {
        return id;
    }

    public String getRequestType() {
        return requestType;
    }

    public double getCreatedTime() {
        return createdTime;
    }

    public double getWaitingTime() {
        return waitingTime;
    }

    public double getStartProcessingTime() {
        return startProcessingTime;
    }

    public void setStartProcessingTime(double startProcessingTime) {
        this.startProcessingTime = startProcessingTime;
    }
}
