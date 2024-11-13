package oleksii.leheza.kpi.ms;

import oleksii.leheza.kpi.ms.enums.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Server {

    private static final double PERCENT_SERVER_OFFLOADING = 20;

    private double currentTime;
    private double nextTime;
    private List<Element> elements;
    private int requestNum;
    private int processedRequests;
    private ServerState serverState;

    private double workingTime;
    private int processedRequestsWorking;

    private Queue<Request> firstProcessQueue;
    private Queue<Request> secondProcessQueue;

    private List<Process> firstProcesses = new ArrayList<>();
    private List<Process> secondProcesses = new ArrayList<>();

    private int nextSecondRequestsProcessingNum;
    private double switchProcessValue = 0.01;

    private ServerLoadingState serverLoadingState;

    public Server(List<Element> elements, Queue<Request> firstProcessQueue, Queue<Request> secondProcessQueue, ServerLoadingState serverLoadingState) {
        this.elements = elements;
        this.firstProcessQueue = firstProcessQueue;
        this.secondProcessQueue = secondProcessQueue;
        serverState = ServerState.PROCESS_FIRST_REQUEST_TYPE;
        this.serverLoadingState = serverLoadingState;
        for (Element element : elements) {
            if (element instanceof Process process) {
                if (process.getAllowedProcessRequestType().equals(AllowedProcessRequestType.FIRST_TYPE)) {
                    firstProcesses.add(process);
                } else {
                    secondProcesses.add(process);
                }
            }
        }
    }

    public void simulate(double modelTime) {
        Element currentElement = null;
        while (currentTime < modelTime) {
            nextTime = Double.MAX_VALUE;
            for (Element e : elements) {
                if (e.getNextEventTime() < nextTime) {
                    nextTime = e.getNextEventTime();
                    currentElement = e;
                }
            }
            currentTime = nextTime;
            for (Element e : elements) {
                e.setCurrentTime(currentTime);
            }
            if (currentElement instanceof RequestGenerator requestGenerator) {
                Request request = requestGenerator.generateRequest();
                if (request != null) {
                    requestNum += 1;
                    chooseProcessType(request);
                    assignRequestToProcess(request);
                }
            } else if (currentElement instanceof Process process) {
                optimize(process);
                if (serverLoadingState != ServerLoadingState.MAIN_WORKING_LOADING) {
                    if (firstProcessQueue.size() >= 10 && secondProcessQueue.size() >= 10) {
                        boolean statisticFlag = true;
                        for (Element e : elements) {
                            if (e instanceof Process p) {
                                if (p.isBusy()) {
                                    statisticFlag = false;
                                }
                            }
                        }
                        if (statisticFlag) {
                            for (Element e : elements) {
                                if (e instanceof Process p) {
                                    p.setServerLoadingState(ServerLoadingState.MAIN_WORKING_LOADING);
                                    p.setStartMainLoadingSystemTime(currentTime);
                                }
                            }
                            serverLoadingState = ServerLoadingState.MAIN_WORKING_LOADING;
                        }
                    }
                }
                if (process.isBusy()) {
                    Request releasedRequest = firstProcessQueue.peek();
                    process.releaseRequest();
                    processedRequests += 1;
                    if (process.getAllowedProcessRequestType() == AllowedProcessRequestType.SECOND_TYPE) {
                        if (nextSecondRequestsProcessingNum > 0) {
                            nextSecondRequestsProcessingNum--;
                        }
                    }
                    if (serverLoadingState == ServerLoadingState.MAIN_WORKING_LOADING) {
                        workingTime += releasedRequest.getProcessingTime();
                        processedRequestsWorking++;
                    }
                } else {
                    process.startProcessingFromQueue();
                }
            }
        }
    }

    private void chooseProcessType(Request request) {
        int firstBusyProcesses = 0;
        for (Process process : firstProcesses) {
            if (process.isBusy()) {
                firstBusyProcesses++;
            }
        }
        if (firstBusyProcesses + firstProcessQueue.size() >= 2 && nextSecondRequestsProcessingNum == 0) {
            if (serverState != ServerState.PROCESS_FIRST_REQUEST_TYPE) {
                System.out.println("------------------------------\n" +
                        "Switch Model from process C request type to A and B request type\n" +
                        "------------------------------\n");
                for (Process process : secondProcesses) {
                    process.setProcessState(ProcessState.DISABLE_TO_GET_REQUEST);
                }
                for (Process process : firstProcesses) {
                    process.setProcessState(ProcessState.ENABLE_TO_GET_REQUEST);
                }
                serverState = ServerState.PROCESS_FIRST_REQUEST_TYPE;
                double maxSecondProcessFinishTime = findMaxProcessTimeForAllowedProcessRequestType(AllowedProcessRequestType.SECOND_TYPE);
                for (Process process : firstProcesses) {
                    process.setNextEventTime(maxSecondProcessFinishTime);
                }
            }
        } else {
            if (request.getRequestType().equals(RequestType.C.name())) {
                if (serverState != ServerState.PROCESS_SECOND_REQUEST_TYPE) {
                    System.out.println("------------------------------\n" +
                            "Switch Model from process A and B request type to C request type\n" +
                            "------------------------------\n");
                    for (Process process : firstProcesses) {
                        process.setProcessState(ProcessState.DISABLE_TO_GET_REQUEST);
                    }
                    for (Process process : secondProcesses) {
                        process.setProcessState(ProcessState.ENABLE_TO_GET_REQUEST);
                    }
                    serverState = ServerState.PROCESS_SECOND_REQUEST_TYPE;
                    double maxFirstProcessFinishTime = findMaxProcessTimeForAllowedProcessRequestType(AllowedProcessRequestType.FIRST_TYPE);
                    for (Process process : secondProcesses) {
                        process.setNextEventTime(maxFirstProcessFinishTime);
                    }
                }
            }
        }
    }

    public void optimize(Process currentProcess) {
        if (currentProcess.getAllowedProcessRequestType() == AllowedProcessRequestType.FIRST_TYPE) {
            int firstBusyProcesses = 0;
            for (Process process : firstProcesses) {
                if (process.isBusy()) {
                    firstBusyProcesses++;
                }
            }
            if (firstBusyProcesses == 2) {
                if (!secondProcessQueue.isEmpty()) {
                    Process anotherBusyProcess = null;
                    for (Process process : firstProcesses) {
                        if (process != currentProcess) {
                            anotherBusyProcess = process;
                            break;
                        }
                    }
                    Request anotherProcessRequest = anotherBusyProcess.getCurrentRequest();
                    if (1 - ((currentTime - anotherProcessRequest.getStartProcessingTime()) / anotherProcessRequest.getProcessingTime()) <= switchProcessValue) {
                        if (serverState != ServerState.PROCESS_SECOND_REQUEST_TYPE) {
                            for (Process process : firstProcesses) {
                                process.setProcessState(ProcessState.DISABLE_TO_GET_REQUEST);
                            }
                            for (Process process : secondProcesses) {
                                process.setProcessState(ProcessState.ENABLE_TO_GET_REQUEST);
                            }
                            nextSecondRequestsProcessingNum = (int) (secondProcessQueue.size() * PERCENT_SERVER_OFFLOADING / 100);
                            System.out.println("------------------------------\n" +
                                    "Optimize switch model from process A and B request type to C request type\n" +
                                    "------------------------------\n");
                            serverState = ServerState.PROCESS_SECOND_REQUEST_TYPE;
                            double maxFirstProcessFinishTime = findMaxProcessTimeForAllowedProcessRequestType(AllowedProcessRequestType.FIRST_TYPE);
                            for (Process process : secondProcesses) {
                                process.setNextEventTime(maxFirstProcessFinishTime);
                            }
                        }
                    }
                }
            }
        }
    }

    private void assignRequestToProcess(Request request) {
        if (request.getRequestType().equals(RequestType.A.name()) || request.getRequestType().equals(RequestType.B.name())) {
            assignRequestToFirstProcessType(request);
        } else {
            assignRequestToSecondProcessType(request);
        }
    }

    private void assignRequestToFirstProcessType(Request request) {
        for (Process process : secondProcesses) {
            if (process.isBusy()) {
                firstProcessQueue.add(request);
                System.out.println("Request ID " + request.getId() + " type " + request.getRequestType() + " assigned to queue");
                return;
            }
        }
        boolean assignedRequest = false;
        for (Process process : firstProcesses) {
            if (!process.isBusy()) {
                process.processRequest(request);
                assignedRequest = true;
                break;
            }
        }
        if (!assignedRequest) {
            firstProcessQueue.add(request);
            System.out.println("Request ID " + request.getId() + " type " + request.getRequestType() + " assigned to queue");
        }
    }

    private void assignRequestToSecondProcessType(Request request) {
        for (Process process : firstProcesses) {
            if (process.isBusy()) {
                secondProcessQueue.add(request);
                System.out.println("Request ID " + request.getId() + " type " + request.getRequestType() + " assigned to queue");
                return;
            }
        }
        boolean assignedRequest = false;
        for (Process process : secondProcesses) {
            if (!process.isBusy()) {
                process.processRequest(request);
                assignedRequest = true;
                break;
            }
        }
        if (!assignedRequest) {
            secondProcessQueue.add(request);
            System.out.println("Request ID " + request.getId() + " type " + request.getRequestType() + " assigned to queue");
        }
    }

    private double findMaxProcessTimeForAllowedProcessRequestType(AllowedProcessRequestType allowedProcessRequestType) {
        double maxProcessTime = 0;
        if (AllowedProcessRequestType.FIRST_TYPE == allowedProcessRequestType) {
            double currentProcessTime;
            for (Process process : firstProcesses) {
                currentProcessTime = process.getNextEventTime();
                if (currentProcessTime > maxProcessTime) {
                    maxProcessTime = currentProcessTime;
                }
            }
        } else {
            double currentProcessTime;
            for (Process process : secondProcesses) {
                currentProcessTime = process.getNextEventTime();
                if (currentProcessTime > maxProcessTime) {
                    maxProcessTime = currentProcessTime;
                }
            }
        }
        return maxProcessTime;
    }

    public double getWorkingTime() {
        return workingTime;
    }

    public void printServerStatistic() {
        System.out.println("----------Server statistic----------");
        System.out.println("All generated requests :" + requestNum + "\n" + "Processed requests: " + processedRequests);
    }
}
