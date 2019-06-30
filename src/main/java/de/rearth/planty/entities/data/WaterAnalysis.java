package de.rearth.planty.entities.data;

import de.rearth.planty.repositories.WateringEventRepository;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Getter
@Setter
public class WaterAnalysis {

    private static final int updateSeconds = 5;
    private static final int minDataRequired = 40;


    private List<WateringEvent> events;
    private Date nextAdditionExpected;
    private float AvgLossRate; //per x seconds
    private String currentState;
    @Transient
    private String lastEventTime;
    @Transient
    private String formattedNextWaterTime;

    public WaterAnalysis(List<WaterUpdate> updates, Plant plant, WateringEventRepository wateringEventRepository) {

        if (updates.size() < minDataRequired) {
            this.currentState = "not enough data";
            return;
        }

        this.events = findEvents(updates, plant);
        this.AvgLossRate = findAvgLossRate(updates, updates.size() - 2);
        this.nextAdditionExpected = findNextAddition(updates, AvgLossRate, plant);


        //store all new elements in db, then query db for all existing elements
        for (WateringEvent event : events) {
            if (!wateringEventRepository.existsById(event.startTime)) {
                wateringEventRepository.save(event);
            }
        }

        events = wateringEventRepository.findallByPlant(plant);

        if (events.size() >= 1) {
            this.currentState = findCurrentState(updates, plant, nextAdditionExpected, events.get(events.size() - 1).endTime);
        } else {
            this.currentState = "not enough data";
        }


    }

    public String getLastEventTime() {
        try {
            Date date = events.get(events.size() - 1).endTime;
            return formatDate(date);
        } catch (Exception ex) {
            return "no data found";
        }
    }

    public String getFormattedNextWaterTime() {
        return formatDate(this.getNextAdditionExpected());
    }

    private String formatDate(Date date) {
        try {
            return new SimpleDateFormat("EEE, HH:mm").format(date);
        } catch (NullPointerException ex) {
            return "no data found";
        }
    }

    //method is used to find all WateringEvents in the given list
    private static List<WateringEvent> findEvents(List<WaterUpdate> updates, Plant plant) {

        List<WateringEvent> events = new ArrayList<>();

        //find all watered elements (where waterLevel rises)
        //from oldest to newest
        float oldLevel = updates.get(0).getWaterLevel();
        boolean rising = false;
        boolean ignoreStart = false;
        WaterUpdate startingAt = null;

        for (int i = 0; i < updates.size(); i++) {
            WaterUpdate elem = updates.get(i);
            float newLevel = elem.getWaterLevel();

            if (newLevel > oldLevel) {
                if (i < 2) {
                    ignoreStart = true;
                }
                //water is rising

                if (!rising) {
                    //first rising event
                    startingAt = elem;
                    rising = true;
                }
                //water isnt rising
            } else {
                if (rising) {
                    //end of rising event
                    rising = false;
                    WateringEvent evt = new WateringEvent();
                    evt.startTime = startingAt.getMsgTimestamp();
                    evt.endTime = elem.getMsgTimestamp();
                    evt.addedAmount = elem.getWaterLevel() - startingAt.getWaterLevel();
                    evt.plant = plant;

                    //discard events with too little change, could just be measurement mistakes
                    if (evt.addedAmount < 0.1 || ignoreStart) {
                        continue;
                    }

                    events.add(evt);
                }
            }

            oldLevel = newLevel;
        }

        return events;
    }

    //finds the average water loss rate in the last n updates
    private static float findAvgLossRate(List<WaterUpdate> updates, int count) {
        float sum = 0;
        float finalCount = count;
        for (int i = updates.size() - count; i < updates.size(); i++) {
            float res = updates.get(i - 1).getWaterLevel() - updates.get(i).getWaterLevel();
            if (res > -0.001) {
                //only used negative values, to ignore events while the plant is being watered
                sum += res;
            } else {
                finalCount--;
            }
        }

        return sum / finalCount;
    }

    //sensors send data every x seconds, so the loss rate if for that timespan
    private static Date findNextAddition(List<WaterUpdate> updates, float avgLossRate, Plant plant) {
        float curLevel = updates.get(updates.size() - 1).getWaterLevel();
        float minLevel = plant.getWaterRequirement();

        Calendar now = Calendar.getInstance();
        float lossPerSecond = avgLossRate / updateSeconds;
        //minLevel = curLevel - lossPerSecond * x
        //x = (curLevel - minLevel) / lossPerSecond
        float secondsToMin = (curLevel - minLevel) / lossPerSecond;

        now.add(Calendar.SECOND, (int) secondsToMin);
        return now.getTime();
    }

    private static String findCurrentState(List<WaterUpdate> updates, Plant plant, Date nextEvent, Date lastWaterEvent) {

        //add 0.1f since waterRequirement is the minimum amount of water the plant should have
        float targetLevel = plant.getWaterRequirement();
        float level = updates.get(updates.size() - 1).getWaterLevel();

        int daysToNextWater = daysBetween(new Date(), nextEvent);
        int daysToLastWater = daysBetween(lastWaterEvent, new Date());

         if (level > targetLevel + 0.3f && daysToLastWater > 2) {
            return "Too much water";
        } else if (level > targetLevel && daysToNextWater >= 1) {
            return "Perfectly watered";
        } else if (level > targetLevel) {
            return "Should be watered soon";
        } else if (level > targetLevel - 0.1) {
            return "Needs to be watered";
        } else if (level < targetLevel) {
            return "Desperately needs water";
        }

        return "unknown error";
    }

    private static int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    @Data
    @Entity
    public static class WateringEvent {
        @Id
        private Date startTime;
        private Date endTime;
        private float addedAmount;

        //just for database
        @ManyToOne(cascade = CascadeType.ALL, targetEntity = Plant.class)
        @JoinColumn(name="id")
        private Plant plant;

    }
}
