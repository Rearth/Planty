package de.rearth.planty.entities.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.util.Date;

//represents a plant being watered at a specific moment
@Entity
@Getter
@Setter
@NoArgsConstructor
public class WaterUpdate implements Comparable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long eventId;

    //@NotBlank
    @ManyToOne(cascade = CascadeType.ALL, targetEntity = Sensor.class)
    @JoinColumn(name="name")
    private Sensor sensorOrigin;

    //range 0-1
    private float waterLevel;

    @Temporal(TemporalType.TIMESTAMP)
    Date msgTimestamp;

    @ManyToOne(cascade = CascadeType.ALL, targetEntity = Plant.class)
    @JoinColumn(name="id")
    private Plant plant;

    @Override
    public int compareTo(Object o) {
        return ((WaterUpdate) o).getMsgTimestamp().compareTo(msgTimestamp);
    }
}
