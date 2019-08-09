package de.rearth.planty.controllers;

import de.rearth.planty.data.Search;
import de.rearth.planty.entities.Plant;
import de.rearth.planty.entities.Sensor;
import de.rearth.planty.data.WaterAnalysis;
import de.rearth.planty.entities.WaterUpdate;
import de.rearth.planty.repositories.PlantRepository;
import de.rearth.planty.repositories.SensorRepository;
import de.rearth.planty.repositories.WaterUpdateRepository;
import de.rearth.planty.repositories.WateringEventRepository;
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
    private final WateringEventRepository wateringEventRepository;
    private final SensorRepository sensorRepository;

    public static WaterUpdateRepository getWaterUpdateRepository() {
        return waterUpdateRepository;
    }

    @Autowired
    public PlantController(StorageService storageService, PlantRepository plantRepository, WaterUpdateRepository updateRepository, WateringEventRepository wateringEventRepository, SensorRepository sensorRepository) {
        this.storageService = storageService;
        this.plantRepository = plantRepository;
        waterUpdateRepository = updateRepository;
        this.wateringEventRepository = wateringEventRepository;
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
    public String createPlantSubmit(@Valid Plant plant, BindingResult result, @RequestParam("file") MultipartFile file, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("message", "unable to add plant " + result + " plant: " + plant.toString());
        } else {
            model.addAttribute("message", " successfully added Plant: " + plant.getName());
            storageService.store(file);

            String fileName = file.getOriginalFilename();
            plant.setFileName(fileName);
            plantRepository.save(plant);


            APIController.analysisDictionary.put(plant.getId(), new WaterAnalysis(waterUpdateRepository.findUpdatesByPlant(plant, 200), plant, wateringEventRepository));
        }

        return showOverview(model);
    }

    @PostMapping("/createSensor")
    public String createSensorSubmit(@Valid Sensor sensor, BindingResult result, @RequestParam("plantid") long plantID, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("message", "unable to add sensor" + result);
        } else {
            sensor.setCreationTime(new Date());
            sensor.setBlocked(false);
            sensor.setPlant(plantRepository.findById(plantID).get());
            sensorRepository.save(sensor);
        }

        return showSensorOverview(model);
    }


    @RequestMapping("/deletePlant/{id}")
    public String deletePlant(@PathVariable("id") long id, Model model) {
        Plant plant = plantRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid plant Id:" + id));
        plantRepository.delete(plant);
        return "redirect:/showPlants";
    }

    @RequestMapping("/deleteSensor/{name}")
    public String deleteSensor(@PathVariable("name") String id, Model model) {
        Sensor sensor = sensorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Sensor Id:" + id));
        sensorRepository.delete(sensor);
        model.addAttribute("message", "Successfully deleted " + sensor.getName());
        return showSensorOverview(model);
    }

    @RequestMapping("/blockSensor/{name}")
    public String blockSensor(@PathVariable("name") String id, Model model) {
        Sensor sensor = sensorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Sensor Id:" + id));
        sensor.setBlocked(true);
        sensorRepository.save(sensor);
        return showSensorOverview(model);
    }

    @RequestMapping("/getConfiguration/{name}")
    public String getSensorConfig(@PathVariable("name") String id, Model model) {
        Sensor sensor = sensorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Sensor Id:" + id));

        model.addAttribute("message", "Name: " + sensor.getName() + " Plant ID: " + sensor.getPlant().getId());
        return showSensorOverview(model);
    }

    @RequestMapping("/unblockSensor/{name}")
    public String unBlockSensor(@PathVariable("name") String id, Model model) {
        Sensor sensor = sensorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Sensor Id:" + id));
        sensor.setBlocked(false);
        sensorRepository.save(sensor);
        return showSensorOverview(model);
    }

    @RequestMapping("/search/")
    public String diplaySearch(@RequestParam("query") String query, Model model) {
        if (StreamSupport.stream(plantRepository.findAll().spliterator(), false).count() > 0) {
            model.addAttribute("plantList", plantRepository.findAll());
        }
        model.addAttribute("query", query);

        Search search = new Search(plantRepository, waterUpdateRepository, sensorRepository, wateringEventRepository, query);

        return "searchResult";
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
            model.addAttribute("message", "No analysis found");
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
