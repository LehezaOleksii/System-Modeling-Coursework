package oleksii.leheza.kpi.ms;

import oleksii.leheza.kpi.ms.enums.RequestType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RequestGenerator extends Element {

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
            double processingRequestTime = generateExponentialTime(requestType.getProcessingTimeMean(), requestType.getProcessingTimeVariance());
            RequestTypeNameToNextGenerateTime.put(requestType.name(), currentTime + processingRequestTime);
            double minNextGenerationTime = findMinNextGenerateTime();
            nextEventTime = minNextGenerationTime;
            return new Request(currentTime, processingRequestTime, requestType.name());
        } else {
            System.out.println("FAILED TO GENERATE REQUEST");//TODO delete
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

    private double generateExponentialTime(int mean, int variance) {
        double gaussian = ThreadLocalRandom.current().nextGaussian();
        return Math.round(mean + gaussian * variance);//TODO question
    }
}
