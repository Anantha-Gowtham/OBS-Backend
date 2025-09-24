package com.obs.repository;

import com.obs.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KycRequestRepository extends JpaRepository<KycRequest, Long> {
    List<KycRequest> findByStatus(KycStatus status);
}
