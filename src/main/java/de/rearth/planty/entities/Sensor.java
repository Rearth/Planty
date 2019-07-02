package de.rearth.planty.entities;

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

    @ManyToOne(cascade = CascadeType.DETACH, targetEntity = Plant.class)
    @JoinColumn(name="id")
    private Plant plant;

    @Temporal(TemporalType.TIMESTAMP)
    Date creationTime;

    @Temporal(TemporalType.TIMESTAMP)
    Date lastMessage;

    public boolean isActive() {
        if (getLastMessage() == null) {
            return false;
        }
        //in milliseconds
        long diff = new Date().getTime() - getLastMessage().getTime();
        //convert to minutes
        diff = diff / 1000 / 60;

        //longer than 5 minutes counts as inactive
        return diff < 5;

    }
}
