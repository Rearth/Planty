package de.rearth.planty.repositories;

import de.rearth.planty.entities.data.Plant;
import de.rearth.planty.entities.data.WaterAnalysis;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface WateringEventRepository extends CrudRepository<WaterAnalysis.WateringEvent, Date> {

    @Query("SELECT u FROM WaterAnalysis$WateringEvent u WHERE u.plant=?1")
    List<WaterAnalysis.WateringEvent> findallByPlant(Plant plant);

}