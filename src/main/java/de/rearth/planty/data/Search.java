package de.rearth.planty.data;

import de.rearth.planty.repositories.PlantRepository;
import de.rearth.planty.repositories.SensorRepository;
import de.rearth.planty.repositories.WaterUpdateRepository;
import de.rearth.planty.repositories.WateringEventRepository;

public class Search {

    //data repositories
    private final PlantRepository plantRepository;
    private final WaterUpdateRepository waterUpdateRepository;
    private final SensorRepository sensorRepository;
    private final WateringEventRepository wateringRepository;

    private final String query;

    public enum searchType {
        Date, Text, Floating, Decimal
    }

    //Search waterEvents by date, number(wateringAmount, id)
    //Search waterUpdates by date, number(id)
    //Search plantRepository by text(name, kind, sensorName), number(id, waterAmount), date(creationDate)
    //Search SensorRepository by text(name, plantName, activeState, BlockedState/Name), date(creationDate, lastMessage)

    public Search(PlantRepository plantRepository, WaterUpdateRepository waterUpdateRepository, SensorRepository sensorRepository, WateringEventRepository wateringRepository, String query) {
        this.plantRepository = plantRepository;
        this.waterUpdateRepository = waterUpdateRepository;
        this.sensorRepository = sensorRepository;
        this.wateringRepository = wateringRepository;
        this.query = query;

        searchType searchType = findSearchType(query);
    }

    private searchType findSearchType(String query) {
        //check for int
        try {
            Integer.valueOf(query);
            return searchType.Decimal;
        } catch(NumberFormatException ex) {};

        try {
            Float.valueOf(query);
            return searchType.Floating;
        } catch(NumberFormatException ex) {};


        return null;
    }
}
