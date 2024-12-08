package oleksii.leheza.kpi.ms.v1;

import oleksii.leheza.kpi.ms.v1.enums.AllowedProcessRequestType;
import oleksii.leheza.kpi.ms.v1.enums.ProcessState;
import oleksii.leheza.kpi.ms.v1.enums.RequestType;
import oleksii.leheza.kpi.ms.v1.enums.ServerLoadingState;

import java.util.Queue;

public class Process extends Element {

    private Queue<Request> requestQueue;
    private int processedRequests;
    private AllowedProcessRequestType allowedProcessRequestType;
    private ProcessState processState;
    private Request currentRequest;
    private boolean isBusy;

    private ServerLoadingState serverLoadingState;
    private int processedRequestsStatistic;

    private double startMainLoadingSystemTime;

    private double processingTime;

    public Process(String name, Queue<Request> requestQueue, AllowedProcessRequestType allowedProcessRequestType, ProcessState processState, ServerLoadingState serverLoadingState) {
        super(name);
        this.requestQueue = requestQueue;
        this.allowedProcessRequestType = allowedProcessRequestType;
        this.processState = processState;
        this.serverLoadingState = serverLoadingState;
    }

    public void processRequest(Request request) {
        if (allowedProcessRequestType.getAllowedRequestTypes().contains(RequestType.valueOf(request.getRequestType()))) {
            if (isBusy || processState == ProcessState.DISABLE_TO_GET_REQUEST) {
                if (requestQueue != null) {
                    requestQueue.add(request);
                    System.out.println("Request ID " + request.getId() + " type " + request.getRequestType() + " assigned to queue");
                }
            } else {
                isBusy = true;
                currentRequest = request;
                currentRequest.setStartProcessingTime(currentTime);
                nextEventTime = currentTime + request.getProcessingTime();
                System.out.println("Request ID " + request.getId() + " type " + request.getRequestType() + " start processing in the " + name);
            }
        } else {
            System.out.println("Request ID " + request.getId() + " is not allowed to process");
        }
        System.out.println("QUEUE " + name + " " + requestQueue.size());
    }

    public void releaseRequest() {
        processedRequests++;
        if (serverLoadingState == ServerLoadingState.MAIN_WORKING_LOADING) {
            processedRequestsStatistic++;
            processingTime += currentRequest.getProcessingTime();
        }
        isBusy = false;
        System.out.println("Request ID " + currentRequest.getId() + " finished processing in the " + name);
        if (!requestQueue.isEmpty() && processState == ProcessState.ENABLE_TO_GET_REQUEST) {
            Request nextRequest = requestQueue.poll();
            processRequest(nextRequest);
        } else {
            nextEventTime = Double.MAX_VALUE;
            currentRequest = null;
            System.out.println(name + " is free");
        }
    }

    public void startProcessingFromQueue() {
        if (!requestQueue.isEmpty() && processState == ProcessState.ENABLE_TO_GET_REQUEST) {
            Request nextRequest = requestQueue.poll();
            processRequest(nextRequest);
        } else {
            nextEventTime = Double.MAX_VALUE;
            currentRequest = null;
            System.out.println(name + " is free");
        }
    }

    public void printStatistic() {
        System.out.println("-------" + name + " statistics" + "-------");
        System.out.println("Processed requests: " + processedRequests);
        System.out.println("Processed requests in main system state: " + processedRequestsStatistic);
        System.out.println("Queue size: " + requestQueue.size());
        System.out.printf("Loading device : %.3f%%%n", getLoadingDevice());
    }

    public double getLoadingDevice() {
        return (processingTime / (currentTime - startMainLoadingSystemTime)) * 100;
    }

    public AllowedProcessRequestType getAllowedProcessRequestType() {
        return allowedProcessRequestType;
    }

    public Request getCurrentRequest() {
        return currentRequest;
    }

    public void setProcessState(ProcessState processState) {
        this.processState = processState;
    }

    public Queue<Request> getRequestQueue() {
        return requestQueue;
    }

    public ProcessState getProcessState() {
        return processState;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public int getProcessedRequests() {
        return processedRequests;
    }

    public double getProcessingTime() {
        return processingTime;
    }

    public void setServerLoadingState(ServerLoadingState serverLoadingState) {
        this.serverLoadingState = serverLoadingState;
    }

    public void setStartMainLoadingSystemTime(double startMainLoadingSystemTime) {
        this.startMainLoadingSystemTime = startMainLoadingSystemTime;
    }
}
