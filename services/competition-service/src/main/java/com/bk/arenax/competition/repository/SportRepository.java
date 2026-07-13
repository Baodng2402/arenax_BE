package com.bk.arenax.competition.repository;

import com.bk.arenax.competition.domain.entity.Sport;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SportRepository extends JpaRepository<Sport, UUID> {
}
