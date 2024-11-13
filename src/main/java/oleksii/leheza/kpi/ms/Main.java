package oleksii.leheza.kpi.ms;

import oleksii.leheza.kpi.ms.enums.AllowedProcessRequestType;
import oleksii.leheza.kpi.ms.enums.ProcessState;
import oleksii.leheza.kpi.ms.enums.ServerLoadingState;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Main {

    public static void main(String[] args) {

        ServerLoadingState serverLoadingState = ServerLoadingState.START_WORKING_LOADING;
        Queue<Request> firstProcessQueue = new LinkedList<>();
        Queue<Request> secondProcessQueue = new LinkedList<>();
        Process p1 = new Process("Process 1.1", firstProcessQueue, AllowedProcessRequestType.FIRST_TYPE, ProcessState.ENABLE_TO_GET_REQUEST, serverLoadingState);
        Process p2 = new Process("Process 1.2", firstProcessQueue, AllowedProcessRequestType.FIRST_TYPE, ProcessState.ENABLE_TO_GET_REQUEST, serverLoadingState);
        Process p3 = new Process("Process 2.1", secondProcessQueue, AllowedProcessRequestType.SECOND_TYPE, ProcessState.DISABLE_TO_GET_REQUEST, serverLoadingState);

        RequestGenerator generator = new RequestGenerator("Generator");

        List<Element> elements = new ArrayList<>();
        elements.add(p1);
        elements.add(p2);
        elements.add(p3);
        elements.add(generator);

        Server server = new Server(elements, firstProcessQueue, secondProcessQueue, serverLoadingState);
        server.simulate(10000);

        server.printServerStatistic();

        for (Element e : elements) {
            if (e instanceof Process process) {
                process.printStatistic();
            }
        }

        System.out.println("-------System statistic------");
        double firstSystemLoadingDevicesSum = 0;
        for (Element e : elements) {
            if (e instanceof Process process && process.getAllowedProcessRequestType() == AllowedProcessRequestType.FIRST_TYPE) {
                firstSystemLoadingDevicesSum += process.getLoadingDevice();
            }
        }
        double secondSystemLoadingDevicesSum = 0;
        for (Element e : elements) {
            if (e instanceof Process process && process.getAllowedProcessRequestType() == AllowedProcessRequestType.SECOND_TYPE) {
                secondSystemLoadingDevicesSum += process.getLoadingDevice();
            }
        }
        List<Process> firstProcesses = new ArrayList<>();
        List<Process> secondProcesses = new ArrayList<>();
        for (Element element : elements) {
            if (element instanceof Process process) {
                if (process.getAllowedProcessRequestType().equals(AllowedProcessRequestType.FIRST_TYPE)) {
                    firstProcesses.add(process);
                } else {
                    secondProcesses.add(process);
                }
            }
        }
        System.out.printf("Total system loading: %.3f%%%n", ((firstSystemLoadingDevicesSum / firstProcesses.size()) + (secondSystemLoadingDevicesSum / secondProcesses.size())) / 2);
        System.out.printf("Efficient system loading: %.3f%%%n", (firstSystemLoadingDevicesSum / firstProcesses.size()) + (secondSystemLoadingDevicesSum / secondProcesses.size()));
    }
}
