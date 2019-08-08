package de.rearth.planty.data;

import de.rearth.planty.repositories.PlantRepository;
import de.rearth.planty.repositories.SensorRepository;
import de.rearth.planty.repositories.WaterUpdateRepository;
import de.rearth.planty.repositories.WateringEventRepository;

public class Search {

    public enum searchType {
        Date, Text, Floating, Decimal
    }

    //Search waterEvents by date, number(wateringAmount, id)
    //Search waterUpdates by date, number(id)
    //Search plantRepository by text(name, kind, sensorName), number(id, waterAmount), date(creationDate)
    //Search SensorRepository by text(name, plantName, activeState, BlockedState/Name), date(creationDate, lastMessage)

    public Search(PlantRepository plantRepository, WaterUpdateRepository waterUpdateRepository, SensorRepository sensorRepository, WateringEventRepository wateringRepository, String query) {
        //data repositories
        PlantRepository plantRepository1 = plantRepository;
        WaterUpdateRepository waterUpdateRepository1 = waterUpdateRepository;
        SensorRepository sensorRepository1 = sensorRepository;
        WateringEventRepository wateringRepository1 = wateringRepository;
        String query1 = query;

        searchType searchType = findSearchType(query);
    }

    private searchType findSearchType(String query) {
        //check for int
        try {
            Integer.valueOf(query);
            return searchType.Decimal;
        } catch(NumberFormatException ignored) {}

        try {
            Float.valueOf(query);
            return searchType.Floating;
        } catch(NumberFormatException ignored) {}


        return null;
    }
}
