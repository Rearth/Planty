package de.rearth.planty.entities.data;

import de.rearth.planty.controllers.PlantController;
import de.rearth.planty.repositories.WaterUpdateRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank
    private String name;

    @NotBlank
    private String plantKind;

    //waterRequirement is the minimum amount of water the plant should have
    private float waterRequirement;

    //name of the picture on the storage system
    private String fileName;

    //description of the plant, whether or not it has enough water
    private String waterLevel;


    public String getWaterLevel() {

        WaterUpdateRepository updateRepository = PlantController.getWaterUpdateRepository();

        if (updateRepository == null) {
            return "missing db connection instance";
        }

        List<WaterUpdate> updates = updateRepository.findUpdatesByPlant(this, 1);
        Collections.sort(updates);
        try {
            return String.valueOf(updates.get(0).getWaterLevel());
        } catch (NullPointerException | IndexOutOfBoundsException ex) {
            return "no data found";
        }
    }
}
