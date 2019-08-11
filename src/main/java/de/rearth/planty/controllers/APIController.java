package de.rearth.planty.controllers;

import de.rearth.planty.entities.Plant;
import de.rearth.planty.entities.Sensor;
import de.rearth.planty.data.WaterAnalysis;
import de.rearth.planty.entities.WaterUpdate;
import de.rearth.planty.repositories.PlantRepository;
import de.rearth.planty.repositories.SensorRepository;
import de.rearth.planty.repositories.WaterUpdateRepository;
import de.rearth.planty.repositories.WateringEventRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

//used to recieve data from a real sensor on a plant
@Controller
public class APIController {

    //increment counter used to assign IDs to incoming events
    private final AtomicLong counter = new AtomicLong();

    //Connections to the DB
    private final PlantRepository plantRepository;
    private final WaterUpdateRepository waterUpdateRepository;
    private final WateringEventRepository wateringEventRepository;
    private final SensorRepository sensorRepository;

    //Storing all analysis in a dictionary, available to all other classes since its public and static
    public static final Map<Long, WaterAnalysis> analysisDictionary = new HashMap<>();

    @Autowired
    public APIController(PlantRepository plantRepository, WaterUpdateRepository waterUpdateRepository, WateringEventRepository wateringEventRepository, SensorRepository sensorRepository) {
        this.plantRepository = plantRepository;
        this.waterUpdateRepository = waterUpdateRepository;
        this.wateringEventRepository = wateringEventRepository;
        this.sensorRepository = sensorRepository;

        //initialize dict with available data
        //on startup the application analyzes stored data in the DB to have some data available for the user
        plantRepository.findAll().forEach(plant -> analysisDictionary.put(plant.getId(), new WaterAnalysis(waterUpdateRepository.findUpdatesByPlant(plant, 200), plant, wateringEventRepository)));
    }

    //GET- and POST requests are handled here
    @RequestMapping(value = "/api/waterevent")
    public @ResponseBody response uploadEvent(@RequestParam(value="sensorName") String sensorName, @RequestParam(value="plantID") long plantID, @RequestParam(value="waterLevel") float waterLevel) {

        try {
            //Find the corresponding sensor in the DB and create a new WaterUpdate event
            Date now = new Date();
            Plant plant = plantRepository.findById(plantID).get();

            Sensor sensor = sensorRepository.findById(sensorName).get();
            sensor.setLastMessage(now);
            sensorRepository.save(sensor);

            if (sensor.isBlocked() || sensor.getPlant().getId() != plantID) {
                return new response(counter.incrementAndGet(), false, null, "sensor blocked or plant id doesnt match sensor");
            }

            WaterUpdate update = new WaterUpdate();
            update.setSensorOrigin(sensor);
            update.setPlant(plant);
            update.setMsgTimestamp(new Date());

            if (waterLevel <= 0) {
                waterLevel = 0.001f;
            }
            update.setWaterLevel(waterLevel);

            //store new update in DB, and analyze new Data
            waterUpdateRepository.save(update);
            analysisDictionary.put(plant.getId(), new WaterAnalysis(waterUpdateRepository.findUpdatesByPlant(plant, 100), plant, wateringEventRepository));


            return new response(counter.incrementAndGet(), true, update, "");
        } catch (NoSuchElementException ex) {
            return new response(counter.incrementAndGet(), false, null, "Element not found: " + ex.getMessage());
        }
    }


    @Data
    private class response {
        private final long id;
        private final boolean success;
        private final WaterUpdate update;
        private final String errorMessage;
    }
}