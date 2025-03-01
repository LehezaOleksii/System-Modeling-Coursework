package oleksii.leheza.kpi.ms.v3;

import oleksii.leheza.kpi.ms.v3.enums.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Server {

    private double currentTime;
    private double nextTime;
    private List<Element> elements;
    private int requestNum;
    private int processedRequests;
    private ServerState serverState;

    private double lastFirstQueueTime;
    private List<Integer> firstQueueSizeList = new ArrayList<>();
    private List<Double> firstQueueSizePriodList = new ArrayList<>();
    private double lastSecondQueueTime;
    private List<Integer> secondQueueSizeList = new ArrayList<>();
    private List<Double> secondQueueSizePriodList = new ArrayList<>();

    private double workingTime;

    private double startTimeTracking;

    private Queue<Request> firstProcessQueue;
    private Queue<Request> secondProcessQueue;

    private List<Process> firstprocesses = new ArrayList<>();
    private List<Process> secondProcesses = new ArrayList<>();

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
                    firstprocesses.add(process);
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
                if (serverLoadingState != ServerLoadingState.MAIN_WORKING_LOADING) {
                    if (firstProcessQueue.size() >= 2 && secondProcessQueue.size() >= 1) {
                        boolean statisticFlag = true;
                        for (Element e : elements) {
                            if (e instanceof Process p) {
                                if (p.isBusy()) {
                                    statisticFlag = false;
                                }
                            }
                        }
                        for (Element e : elements) {
                            if (e instanceof Process p) {
                                p.setServerLoadingState(ServerLoadingState.MAIN_WORKING_LOADING);
                                p.setStartMainLoadingSystemTime(currentTime);
                            }
                        }
                        serverLoadingState = ServerLoadingState.MAIN_WORKING_LOADING;
                        startTimeTracking = currentTime;
                        lastFirstQueueTime = currentTime;
                        lastSecondQueueTime = currentTime;
                    }
                } else {
                    firstQueueSizeList.add(firstProcessQueue.size() - 2);
                    firstQueueSizePriodList.add(currentTime - lastFirstQueueTime);
                    secondQueueSizeList.add(secondProcessQueue.size() - 1);
                    secondQueueSizePriodList.add(currentTime - lastSecondQueueTime);
                    lastFirstQueueTime = currentTime;
                    lastSecondQueueTime = currentTime;
                }
                if (process.isBusy()) {
                    Request releasedRequest = process.getCurrentRequest();
                    process.releaseRequest();
                    processedRequests += 1;
                    if (serverLoadingState == ServerLoadingState.MAIN_WORKING_LOADING) {
                        workingTime += releasedRequest.getProcessingTime();
                    }
                } else {
                    process.startProcessingFromQueue();
                }
            }
        }
    }

    private void chooseProcessType(Request request) {
        int firstBusyProcesses = 0;
        for (Process process : firstprocesses) {
            if (process.isBusy()) {
                firstBusyProcesses++;
            }
        }
        if (firstBusyProcesses + firstProcessQueue.size() >= 1 || request.getRequestType().equals(RequestType.A.name()) || request.getRequestType().equals(RequestType.B.name())) {
            if (serverState != ServerState.PROCESS_FIRST_REQUEST_TYPE) {
                System.out.println("------------------------------\n" + "Switch Model from process C request type to A and B request type\n" + "------------------------------\n");
                for (Process process : secondProcesses) {
                    process.setProcessState(ProcessState.DISABLE_TO_GET_REQUEST);
                }
                for (Process process : firstprocesses) {
                    process.setProcessState(ProcessState.ENABLE_TO_GET_REQUEST);
                }
                serverState = ServerState.PROCESS_FIRST_REQUEST_TYPE;
                double maxSecondProcessFinishTime = findMaxProcessTimeForAllowedProcessRequestType(AllowedProcessRequestType.SECOND_TYPE);
                for (Process process : secondProcesses) {
                    process.setNextEventTime(maxSecondProcessFinishTime);
                }
            }
        } else {
            if (serverState != ServerState.PROCESS_SECOND_REQUEST_TYPE) {
                System.out.println("------------------------------\n" + "Switch Model from process A and B request type to C request type\n" + "------------------------------\n");
                for (Process process : firstprocesses) {
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
        for (Process process : firstprocesses) {
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
        for (Process process : firstprocesses) {
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
            for (Process process : firstprocesses) {
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
        if (maxProcessTime == Double.MAX_VALUE) {
            maxProcessTime = currentTime;
        }
        return maxProcessTime;
    }

    public double getWorkingTime() {
        return workingTime;
    }

    public void printServerStatistic() {
        double allFirstQueueSizeSum = 0;
        for (Integer firstQueueLength : firstQueueSizeList) {
            for (Double firstQueuePeriod : firstQueueSizePriodList) {
                allFirstQueueSizeSum += firstQueueLength * firstQueuePeriod;
            }
        }
        double allSecondQueueSizeSum = 0;
        for (Integer secondQueueLength : secondQueueSizeList) {
            for (Double secondQueuePeriod : secondQueueSizePriodList) {
                allSecondQueueSizeSum += secondQueueLength * secondQueuePeriod;
            }
        }
        double firstMean = (-1) * (allFirstQueueSizeSum) / (firstQueueSizeList.size() * (currentTime - startTimeTracking));
        double secondMean = (allSecondQueueSizeSum) / (firstQueueSizeList.size() * (currentTime - startTimeTracking));
        System.out.println("----------Server statistic----------");
        System.out.println("All generated requests :" + requestNum + "\n" + "Processed requests: " + processedRequests + "\n" + "First queue mean size: " + firstMean + "\n" + "Second queue mean size: " + secondMean + "\n" + "First queue standard deviation: " + calculateStandardDeviation(firstQueueSizePriodList, firstMean) + "\n" + "Second queue standard deviation: " + calculateStandardDeviation(secondQueueSizePriodList, secondMean) + "\n");
    }

    public double calculateStandardDeviation(List<Double> intervals, double mean) {
        double sum = 0.0;
        for (double interval : intervals) {
            sum += Math.pow(interval - mean, 2);
        }
        return Math.sqrt(sum / intervals.size());
    }
}
