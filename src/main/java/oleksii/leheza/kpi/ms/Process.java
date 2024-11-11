package oleksii.leheza.kpi.ms;

import oleksii.leheza.kpi.ms.enums.AllowedProcessRequestType;
import oleksii.leheza.kpi.ms.enums.ProcessState;
import oleksii.leheza.kpi.ms.enums.RequestType;

import java.util.Queue;

public class Process extends Element {

    private Queue<Request> requestQueue;
    private int processedRequests;/////////////////
    private AllowedProcessRequestType allowedProcessRequestType;
    private ProcessState processState;
    private Request currentRequest;
    private boolean isBusy;

    public Process(String name, Queue<Request> requestQueue, AllowedProcessRequestType allowedProcessRequestType, ProcessState processState) {
        super(name);
        this.requestQueue = requestQueue;
        this.allowedProcessRequestType = allowedProcessRequestType;
        this.processState = processState;
    }

    public void processRequest(Request request) {
        if (allowedProcessRequestType.getAllowedRequestTypes().contains(RequestType.valueOf(request.getRequestType()))) {
            if (isBusy || processState == ProcessState.DISABLE_TO_GET_REQUEST) { //TODO add waiting
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

    public Queue<Request> getRequestQueue() {
        return requestQueue;
    }

    public void printStatistic() {
        System.out.println("----------" + name + " statistics" + "----------");
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

    public ProcessState getProcessState() {
        return processState;
    }

    public boolean isBusy() {
        return isBusy;
    }
}
