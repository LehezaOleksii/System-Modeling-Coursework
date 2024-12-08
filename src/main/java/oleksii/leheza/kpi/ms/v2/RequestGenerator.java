package oleksii.leheza.kpi.ms.v2;

import oleksii.leheza.kpi.ms.v2.enums.RequestType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RequestGenerator extends Element {

    private static Random random = new Random();

    private final Map<String, Double> RequestTypeNameToNextGenerateTime = new HashMap<>();

    public RequestGenerator(String name) {
        super(name);
        nextEventTime = 0;
        for (RequestType requestType : RequestType.values()) {
            RequestTypeNameToNextGenerateTime.put(requestType.name(), 0d);
        }
    }

    public Request generateRequest() {
        RequestType requestType = findCurrentEventType();
        if (requestType != null) {
            double processingRequestTime = generateTime(requestType.getProcessingTimeMean(), requestType.getProcessingTimeVariance());
            RequestTypeNameToNextGenerateTime.put(requestType.name(),  currentTime + generateTime(requestType.getArrivalTimeMean(), requestType.getArrivalTimeVariance()));
            double minNextGenerationTime = findMinNextGenerateTime();
            nextEventTime = minNextGenerationTime;
            return new Request(currentTime, processingRequestTime, requestType.name());
        } else {
            System.out.println("FAILED TO GENERATE REQUEST");
            return null;
        }
    }

    private RequestType findCurrentEventType() {
        for (String key : RequestTypeNameToNextGenerateTime.keySet()) {
            Double value = RequestTypeNameToNextGenerateTime.get(key);
            if (value == currentTime) {
                return RequestType.valueOf(key);
            }
        }
        return null;
    }

    private double findMinNextGenerateTime() {
        double minValue = Double.MAX_VALUE;
        double currentMinGenerationTime;
        for (String key : RequestTypeNameToNextGenerateTime.keySet()) {
            currentMinGenerationTime = RequestTypeNameToNextGenerateTime.get(key);
            if (minValue > currentMinGenerationTime) {
                minValue = currentMinGenerationTime;
            }
        }
        return minValue;
    }

    private double generateTime(double mean, double stdDev) {
        return mean + stdDev * random.nextGaussian();
    }
}
