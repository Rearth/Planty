package de.rearth.planty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rearth.planty.controllers.PlantController;
import de.rearth.planty.entities.Plant;
import de.rearth.planty.repositories.PlantRepository;
import de.rearth.planty.repositories.SensorRepository;
import de.rearth.planty.repositories.WaterUpdateRepository;
import de.rearth.planty.repositories.WateringEventRepository;
import de.rearth.planty.storage.StorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.validation.BindingResult;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PlantController.class)
public class PlantControllerTest {

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

}
