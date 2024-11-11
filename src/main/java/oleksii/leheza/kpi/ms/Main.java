package oleksii.leheza.kpi.ms;

import oleksii.leheza.kpi.ms.enums.AllowedProcessRequestType;
import oleksii.leheza.kpi.ms.enums.ProcessState;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Main {

    public static void main(String[] args) {

        Queue<Request> firstProcessQueue = new LinkedList<>();
        Queue<Request> secondProcessQueue = new LinkedList<>();
        Process p1 = new Process("Process 1.1", firstProcessQueue, AllowedProcessRequestType.FIRST_TYPE, ProcessState.ENABLE_TO_GET_REQUEST);
        Process p2 = new Process("Process 1.2", firstProcessQueue, AllowedProcessRequestType.FIRST_TYPE, ProcessState.ENABLE_TO_GET_REQUEST);
        Process p3 = new Process("Process 2.1", secondProcessQueue, AllowedProcessRequestType.SECOND_TYPE, ProcessState.DISABLE_TO_GET_REQUEST);

        RequestGenerator generator = new RequestGenerator("Generator");

        List<Element> elements = new ArrayList<>();
        elements.add(p1);
        elements.add(p2);
        elements.add(p3);
        elements.add(generator);

        Server server = new Server(elements, firstProcessQueue, secondProcessQueue);
        server.simulate(10000);

        server.printServerStatistic();

        for (Element e : elements) {
            if (e instanceof Process process) {
                process.printStatistic();
            }
        }
    }
}
