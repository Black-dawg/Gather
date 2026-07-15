package org.kanishk.backend.services;

import org.kanishk.backend.domain.entities.TicketValidation;
import java.util.UUID;

public interface TicketValidationService {
  TicketValidation validateTicketByQrCode(UUID qrCodeId);
  TicketValidation validateTicketManually(UUID ticketId);
}
