package de.rearth.planty.entities;

import de.rearth.planty.entities.Plant;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

//represents the event of water being added to a plant
@Data
@Entity
public class WateringEvent {
    @Id
    private Date startTime;
    private Date endTime;
    private float addedAmount;

    //just for database
    @ManyToOne(cascade = CascadeType.ALL, targetEntity = Plant.class)
    @JoinColumn(name="id")
    private Plant plant;
}
