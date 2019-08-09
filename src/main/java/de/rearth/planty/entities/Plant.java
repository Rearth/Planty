package de.rearth.planty.entities;

import de.rearth.planty.controllers.PlantController;
import de.rearth.planty.repositories.WaterUpdateRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    @NotBlank
    public String name;

    @NotBlank
    public String plantKind;

    //waterRequirement is the minimum amount of water the plant should have
    public float waterRequirement;

    //name of the picture on the storage system
    public String fileName;

    //description of the plant, whether or not it has enough water
    @Transient
    public float waterLevel;

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
    public float waterPercentage;

    @Override
    public String toString() {
        return "Plant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", plantKind='" + plantKind + '\'' +
                ", waterRequirement=" + waterRequirement +
                ", fileName='" + fileName + '\'' +
                ", waterLevel=" + waterLevel +
                ", waterPercentage=" + waterPercentage +
                '}';
    }

    // --Commented out by Inspection START (09.08.19 04:48):
//    public float getWaterPercentage() {
//        return getWaterLevel() / getWaterRequirement();
//    }
// --Commented out by Inspection STOP (09.08.19 04:48)
}
