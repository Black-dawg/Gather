package org.kanishk.backend.repositories;

import org.kanishk.backend.domain.entities.QrCode;
import org.kanishk.backend.domain.entities.QrCodeStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {
  Optional<QrCode> findByTicketIdAndTicketPurchaserId(UUID ticketId, UUID ticketPurchaseId);
  Optional<QrCode> findByIdAndStatus(UUID id, QrCodeStatusEnum status);
}
