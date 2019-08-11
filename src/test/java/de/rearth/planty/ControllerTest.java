package de.rearth.planty;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.rearth.planty.controllers.PlantController;
import de.rearth.planty.entities.Plant;
import de.rearth.planty.entities.Sensor;
import de.rearth.planty.repositories.PlantRepository;
import de.rearth.planty.repositories.SensorRepository;
import de.rearth.planty.repositories.WaterUpdateRepository;
import de.rearth.planty.repositories.WateringEventRepository;
import de.rearth.planty.storage.StorageService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static java.util.Optional.ofNullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PlantController.class)
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StorageService storageService;

    @MockBean
    private PlantRepository plantRepository;

    @MockBean
    private WateringEventRepository wateringEventRepository;

    @MockBean
    private WaterUpdateRepository waterUpdateRepository;

    @MockBean
    private SensorRepository sensorRepository;

    @Test
    public void testPlantController_IsAddingNewPlant() throws Exception {
        Plant plant = new Plant();
        plant.setName("test");
        plant.setPlantKind("test");
        plant.setWaterRequirement(0.3f);

        MockMultipartFile firstFile = new MockMultipartFile("file", "filename.png", "text/plain", "empty image".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/createPlant")
                .file(firstFile)
                .flashAttr("plant", plant)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(plantRepository, times(1)).save(plant);
    }

    @Test
    public void testPlantController_IsNotSavingWhenInvalidValues() throws Exception {
        Plant plant = new Plant();
        plant.setPlantKind("test");
        plant.setWaterRequirement(0.3f);
        //plant name has not been set

        MockMultipartFile firstFile = new MockMultipartFile("file", "filename.png", "text/plain", "empty image".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/createPlant")
                .file(firstFile)
                .flashAttr("plant", plant)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(plantRepository, times(0)).save(plant);
    }

    @Test
    public void testAPIController_IsCreatingSensor() throws Exception {
        Plant plant = new Plant();
        plant.setPlantKind("test");
        plant.setWaterRequirement(0.3f);
        plant.setName("test");
        plant.setId(1L);

        Sensor sensor = new Sensor();
        sensor.setName("testSensor");
        sensor.setPlant(plant);

        Mockito.when(plantRepository.findById(plant.getId())).thenReturn(ofNullable(plant));

        mockMvc.perform(post("/createSensor")
                .flashAttr("sensor", sensor)
                .param("plantid", String.valueOf(plant.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(sensorRepository, times(1)).save(sensor);
    }

    @Test
    public void testAPIController_IsNotCreatingSensorWithMissingModelData() throws Exception {
        Plant plant = new Plant();
        plant.setPlantKind("test");
        plant.setWaterRequirement(0.3f);
        plant.setName("test");
        plant.setId(1L);

        Sensor sensor = new Sensor();
        //sensor name left blank
        sensor.setPlant(plant);

        mockMvc.perform(post("/createSensor")
                .flashAttr("sensor", sensor)
                .param("plantid", String.valueOf(plant.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", Matchers.startsWith("unable to add")));


    }

}
