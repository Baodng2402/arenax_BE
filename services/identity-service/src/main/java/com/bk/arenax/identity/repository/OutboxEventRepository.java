package com.bk.arenax.identity.repository;

import com.bk.arenax.identity.domain.entity.OutboxEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {}
