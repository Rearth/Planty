package de.rearth.planty.repositories;

import de.rearth.planty.entities.data.Plant;
import de.rearth.planty.entities.data.WaterUpdate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterUpdateRepository extends CrudRepository<WaterUpdate, Long> {

    //@Query("SELECT u FROM WaterUpdate u WHERE u.plant=?1")
    @Query(
            value = "SELECT * FROM (SELECT * FROM WATER_UPDATE WHERE ID=?1 ORDER BY EVENT_ID DESC LIMIT ?2) sub ORDER BY EVENT_ID ASC\n-- #plant\n",
            countQuery = "SELECT count(*) FROM WATER_UPDATE",
            nativeQuery = true)
    //@Query("SELECT u FROM ( SELECT u from WATER_UPDATE u where u.plant=?1 ORDER BY EVENT_ID DESC LIMIT 50) sub ORDER BY EVENT_ID ASC")
    List<WaterUpdate> findUpdatesByPlant(Plant plant, int count);

    @Query("SELECT u FROM WaterUpdate u WHERE u.plant=?1")
    List<WaterUpdate> findUpdatesByPlant(Plant plant);

}