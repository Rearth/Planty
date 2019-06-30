package de.rearth.planty.controllers;

import de.rearth.planty.entities.data.Plant;
import de.rearth.planty.entities.data.Sensor;
import de.rearth.planty.entities.data.WaterAnalysis;
import de.rearth.planty.entities.data.WaterUpdate;
import de.rearth.planty.repositories.PlantRepository;
import de.rearth.planty.repositories.SensorRepository;
import de.rearth.planty.repositories.WaterUpdateRepository;
import de.rearth.planty.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.StreamSupport;

@Controller
public class PlantController {

    private final StorageService storageService;
    private final PlantRepository plantRepository;
    private static WaterUpdateRepository waterUpdateRepository;
    private final SensorRepository sensorRepository;

    public static WaterUpdateRepository getWaterUpdateRepository() {
        return waterUpdateRepository;
    }

    @Autowired
    public PlantController(StorageService storageService, PlantRepository plantRepository, WaterUpdateRepository updateRepository, SensorRepository sensorRepository) {
        this.storageService = storageService;
        this.plantRepository = plantRepository;
        waterUpdateRepository = updateRepository;
        this.sensorRepository = sensorRepository;
    }

    @GetMapping("/showPlants")
    public String showOverview(Model model) {

        model.addAttribute("plant", new Plant());

        if (StreamSupport.stream(plantRepository.findAll().spliterator(), false).count() > 0) {
            model.addAttribute("plantList", plantRepository.findAll());
        }

        return "index";
    }


    @GetMapping("/")
    public String showLandingPage(Model model) {
        return showOverview(model);
    }

    @GetMapping("/createPlant")
    public String showNewPlantForm(Model model) {

        model.addAttribute("plant", new Plant());

        if (StreamSupport.stream(plantRepository.findAll().spliterator(), false).count() > 0) {
            model.addAttribute("plantList", plantRepository.findAll());
        }

        return "create-plant";
    }

    @PostMapping("/createPlant")
    public String createPlantSubmit(@Valid Plant plant, BindingResult result, @RequestParam("file") MultipartFile file,  Model model) {

        if (result.hasErrors()) {
            model.addAttribute("message", "unable to add plant" + result);
        } else {
            model.addAttribute("message", " successfully added Plant: " + plant.getName());
            storageService.store(file);

            String fileName = file.getOriginalFilename();
            plant.setFileName(fileName);
            plantRepository.save(plant);
        }

        return showOverview(model);
    }

    @PostMapping("/createSensor")
    public String createSensorSubmit(@Valid Sensor sensor, BindingResult result, @RequestParam("plantid") long plantID, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("sensorCreation", "unable to add sensor" + result);
        } else {
            model.addAttribute("sensorCreation", " successfully added sensor: " + sensor.getName());
            sensor.setCreationTime(new Date());
            sensor.setBlocked(false);
            sensor.setPlant(plantRepository.findById(plantID).get());
            sensorRepository.save(sensor);
        }

        return "#";
    }


    @RequestMapping("/deletePlant/{id}")
    public String deletePlant(@PathVariable("id") long id, Model model) {
        Plant plant = plantRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid plant Id:" + id));
        plantRepository.delete(plant);
        return "redirect:/showPlants";
    }

    @RequestMapping("/blockSensor/{name}")
    public String blockSensor(@PathVariable("name") String id, Model model) {
        Sensor sensor = sensorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Sensor Id:" + id));
        sensor.setBlocked(true);
        sensorRepository.save(sensor);
        return "redirect:/showSensors";
    }

    @RequestMapping("/unblockSensor/{name}")
    public String unBlockSensor(@PathVariable("name") String id, Model model) {
        Sensor sensor = sensorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Sensor Id:" + id));
        sensor.setBlocked(false);
        sensorRepository.save(sensor);
        return "redirect:/showSensors";
    }

    @GetMapping("/showSensors")
    public String showSensorOverview(Model model) {

        List<Sensor> sensors = new ArrayList<>();
        sensorRepository.findAll().forEach(sensors::add);
        model.addAttribute("sensors", sensors);

        if (StreamSupport.stream(plantRepository.findAll().spliterator(), false).count() > 0) {
            model.addAttribute("plantList", plantRepository.findAll());
        }

        model.addAttribute("sensorcreator", new Sensor());

        return "sensor-overview";
    }

    @GetMapping("/displayPlant/{id}")
    public String showPlantDetails(@PathVariable("id") long id, Model model) {
        Plant plant = plantRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid plant Id:" + id));

        List<WaterUpdate> updates = waterUpdateRepository.findUpdatesByPlant(plant, 10000);

        model.addAttribute("plant", plant);

        if (updates.size() > 500) {
            model.addAttribute("updates", updates.subList(updates.size() - 500, updates.size() - 1));
        } else {
            model.addAttribute("updates", updates);
        }

        WaterAnalysis analysis = APIController.analysisDictionary.get(plant.getId());
        if (analysis == null) {
            System.out.println("analysis is null");
        }
        model.addAttribute("analysis", analysis);

        Sensor modelDummy = new Sensor();
        model.addAttribute("sensor, ", modelDummy);

        if (StreamSupport.stream(plantRepository.findAll().spliterator(), false).count() > 0) {
            model.addAttribute("plantList", plantRepository.findAll());
        }
        return "plant-details";
    }
}
