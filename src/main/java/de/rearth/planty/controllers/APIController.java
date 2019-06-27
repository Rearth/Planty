package de.rearth.planty.controllers;

import de.rearth.planty.entities.data.Plant;
import de.rearth.planty.entities.data.Sensor;
import de.rearth.planty.entities.data.WaterAnalysis;
import de.rearth.planty.entities.data.WaterUpdate;
import de.rearth.planty.repositories.PlantRepository;
import de.rearth.planty.repositories.SensorRepository;
import de.rearth.planty.repositories.WaterUpdateRepository;
import de.rearth.planty.repositories.WateringEventRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class APIController {

    private final AtomicLong counter = new AtomicLong();

    private final PlantRepository plantRepository;
    private final WaterUpdateRepository waterUpdateRepository;
    private final WateringEventRepository wateringEventRepository;
    private final SensorRepository sensorRepository;

    public static final Map<Long, WaterAnalysis> analysisDictionary = new HashMap<>();

    @Autowired
    public APIController(PlantRepository plantRepository, WaterUpdateRepository waterUpdateRepository, WateringEventRepository wateringEventRepository, SensorRepository sensorRepository) {
        this.plantRepository = plantRepository;
        this.waterUpdateRepository = waterUpdateRepository;
        this.wateringEventRepository = wateringEventRepository;
        this.sensorRepository = sensorRepository;

        //initialize dict with available data
        plantRepository.findAll().forEach(plant -> {
            analysisDictionary.put(plant.getId(), new WaterAnalysis(waterUpdateRepository.findUpdatesByPlant(plant, 200), plant, wateringEventRepository));
        });

    }

    @RequestMapping(value = "/getImage/{imageId}")
    @ResponseBody
    public byte[] getImage(@PathVariable String imageId, HttpServletRequest request) {
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("upload-dir/" + imageId));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }


    @RequestMapping(value = "/api/waterevent")
    public @ResponseBody response uploadEvent(@RequestParam(value="sensorName") String sensorName, @RequestParam(value="plantID") long plantID, @RequestParam(value="waterLevel") float waterLevel) {

        try {

            Date now = new Date();
            Plant plant = plantRepository.findById(plantID).get();

            Sensor sensor = sensorRepository.findById(sensorName).get();
            sensor.setLastMessage(now);

            if (sensor.isBlocked()) {
                return new response(counter.incrementAndGet(), false, null, "sensor blocked");
            }

            WaterUpdate update = new WaterUpdate();
            update.setSensorOrigin(sensor);
            update.setPlant(plant);
            update.setMsgTimestamp(new Date());

            if (waterLevel <= 0) {
                waterLevel = 0.001f;
            }
            update.setWaterLevel(waterLevel);

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