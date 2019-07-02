package de.rearth.planty.repositories;

import de.rearth.planty.entities.WateringEvent;
import de.rearth.planty.entities.Plant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface WateringEventRepository extends CrudRepository<WateringEvent, Date> {

    @Query("SELECT u FROM WateringEvent u WHERE u.plant=?1")
    List<WateringEvent> findallByPlant(Plant plant);

}