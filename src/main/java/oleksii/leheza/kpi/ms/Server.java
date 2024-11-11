package oleksii.leheza.kpi.ms;

import oleksii.leheza.kpi.ms.enums.AllowedProcessRequestType;
import oleksii.leheza.kpi.ms.enums.ProcessState;
import oleksii.leheza.kpi.ms.enums.RequestType;
import oleksii.leheza.kpi.ms.enums.ServerState;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Server {

    private static final double PERCENT_SERVER_OFFLOADING = 70;

    private double currentTime;
    private double nextTime;
    private List<Element> elements;
    private int requestNum;
    private int processedRequests;

    private Queue<Request> firstProcessQueue;
    private Queue<Request> secondProcessQueue;

    private List<Process> firstProcesses = new ArrayList<>();
    private List<Process> secondProcesses = new ArrayList<>();

    private int nextSecondRequestsProcessingNum;

    private double switchProcessValue = 0.1;

    private ServerState serverState = ServerState.PROCESS_FIRST_REQUEST_TYPE;


    public Server(List<Element> elements, Queue<Request> firstProcessQueue, Queue<Request> secondProcessQueue) {
        this.elements = elements;
        this.firstProcessQueue = firstProcessQueue;
        this.secondProcessQueue = secondProcessQueue;
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
                process.releaseRequest();
                processedRequests += 1;
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
            }
        } else {
            if (serverState != ServerState.PROCESS_SECOND_REQUEST_TYPE) {
                if (request.getRequestType().equals(RequestType.C.name())) {
                    System.out.println("------------------------------\n" +
                            "Switch Model from process A and B request type to C request type\n" +
                            "------------------------------\n");
                    for (Process process : firstProcesses) {
                        process.setProcessState(ProcessState.DISABLE_TO_GET_REQUEST);
                    }
                    for (Process process : secondProcesses) {
                        process.setProcessState(ProcessState.ENABLE_TO_GET_REQUEST);
                    }
                }
                serverState = ServerState.PROCESS_SECOND_REQUEST_TYPE;
            }
            if (nextSecondRequestsProcessingNum > 0) {
                nextSecondRequestsProcessingNum--;
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
        }
    }

    private void assignRequestToSecondProcessType(Request request) {
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
        }
    }

    public void printServerStatistic() {
        System.out.println("----Statistic----\n" + "All requests :" + requestNum + "\n" + "Requests received for processing\n: " + processedRequests + "\n");
    }
}
