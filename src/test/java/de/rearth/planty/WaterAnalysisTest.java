package de.rearth.planty;

import de.rearth.planty.data.WaterAnalysis;
import de.rearth.planty.entities.Plant;
import de.rearth.planty.entities.Sensor;
import de.rearth.planty.entities.WaterUpdate;
import de.rearth.planty.entities.WateringEvent;
import de.rearth.planty.repositories.WateringEventRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@RunWith(SpringRunner.class)
@DataJpaTest
public class WaterAnalysisTest {
 
    @Autowired
    private TestEntityManager entityManager;
 
    @Autowired
    private WateringEventRepository waterRepo;

    //required data
    Date dateA;
    Plant plant;
    Sensor sensor;
    ArrayList<WaterUpdate> updates = new ArrayList<>();
    WateringEvent evt;

    public void initTest() {
        // given
        dateA = new Date();
        plant = new Plant();
        plant.setFileName("test");
        plant.setName("test");
        plant.setPlantKind("test");
        plant.setWaterRequirement(0.3f);
        sensor = new Sensor();
        sensor.setName("test");
        sensor.setCreationTime(new Date());
        sensor.setPlant(plant);

        float waterLevel = 0.5f;
        //add 30 elements, water rising between 15 and 20
        for (int i = 0; i < 50; i++) {
            WaterUpdate elem = new WaterUpdate();
            elem.setMsgTimestamp(addSecondsToDate(5, dateA));
            elem.setWaterLevel(waterLevel -= 0.02f);
            elem.setSensorOrigin(sensor);
            elem.setPlant(plant);
            if (i >= 15 && i < 20) {
                elem.setWaterLevel(waterLevel += 0.08f);
            }
            updates.add(elem);
        }

        //add one already found element to the database
        evt = new WateringEvent();
        evt.setStartTime(addSecondsToDate(-120, dateA));
        evt.setEndTime(addSecondsToDate(-100, dateA));
        evt.setAddedAmount(0.2f);
        evt.setPlant(plant);
        entityManager.persist(evt);
        entityManager.flush();
    }

    @Test
    public void testAnalysis_IsSizeCorrect() {
        initTest();
        WaterAnalysis analysis = new WaterAnalysis(updates, plant, waterRepo);

        // then
        assertThat(analysis.getEvents().size()).isEqualTo(2);
    }

    @Test
    public void testAnalysis_IsAddingRightAmount() {
        initTest();
        WaterAnalysis analysis = new WaterAnalysis(updates, plant, waterRepo);

        // then
        assertThat(analysis.getEvents().get(1).getAddedAmount()).isCloseTo(0.06f * 5, within(0.001f));

    }

    @Test
    public void testAnalysis_IsRightState() {
        initTest();
        WaterAnalysis analysis = new WaterAnalysis(updates, plant, waterRepo);

        // then
        assertThat(analysis.getCurrentState()).isEqualTo("Desperately needs water");
    }

    @Test
    public void testAnalysis_FailsWithInsufficientData() {
        initTest();
        updates.clear();
        WaterAnalysis analysis = new WaterAnalysis(updates, plant, waterRepo);

        // then
        assertThat(analysis.getCurrentState()).isEqualTo("not enough data");
    }

    private static Date addSecondsToDate(int seconds, Date beforeTime){
        final long ONE_SECOND_IN_MILLIS = 1000;//millisecs

        long curTimeInMs = beforeTime.getTime();
        Date afterAddingMins = new Date(curTimeInMs + (seconds * ONE_SECOND_IN_MILLIS));
        return afterAddingMins;
    }
 
}