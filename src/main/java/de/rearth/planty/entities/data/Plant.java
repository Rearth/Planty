package de.rearth.planty.entities.data;

import de.rearth.planty.controllers.PlantController;
import de.rearth.planty.repositories.WaterUpdateRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.List;

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
    @Transient
    private float waterLevel;

    public float getWaterLevel() {

        WaterUpdateRepository updateRepository = PlantController.getWaterUpdateRepository();

        if (updateRepository == null || this.name == null) {
            return 0f;
        }

        List<WaterUpdate> updates = updateRepository.findUpdatesByPlant(this, 1);
        //Collections.sort(updates);

        try {
            return updates.get(updates.size() - 1).getWaterLevel();
        } catch (NullPointerException | IndexOutOfBoundsException ex) {
            return 0f;
        }
    }

    @Transient
    private float waterPercentage;

    public float getWaterPercentage() {
        return getWaterLevel() / getWaterRequirement();
    }
}
