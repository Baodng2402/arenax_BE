package com.bk.arenax.adapter.repository.RankModule;

import com.bk.arenax.domain.match.Sport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SportRepository extends JpaRepository<Sport, Long> {
    Optional<Sport> findBySportCode(String sportCode);
}
