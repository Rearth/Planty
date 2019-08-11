package de.rearth.planty.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

//represents a plant's water level at a specific moment
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

    @Transient
    String formattedTimestamp;

    public String getFormattedTimestamp() {
        return new SimpleDateFormat("EEE HH:mm").format(getMsgTimestamp());
    }
}
