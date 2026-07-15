package org.kanishk.backend.services;

import org.kanishk.backend.domain.entities.QrCode;
import org.kanishk.backend.domain.entities.Ticket;
import java.util.UUID;

public interface QrCodeService {

  QrCode generateQrCode(Ticket ticket);

  byte[] getQrCodeImageForUserAndTicket(UUID userId, UUID ticketId);
}
