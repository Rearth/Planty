package de.rearth.planty.entities.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Sensor {
    //transient values
    @Transient
    private boolean active;

    //db values
    private boolean blocked;

    @Id
    @NotBlank
    private String name;

    @ManyToOne(cascade = CascadeType.ALL, targetEntity = Plant.class)
    @JoinColumn(name="id")
    private Plant plant;

    @Temporal(TemporalType.TIMESTAMP)
    Date creationTime;

    @Temporal(TemporalType.TIMESTAMP)
    Date lastMessage;


}
