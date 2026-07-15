package org.kanishk.backend.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.kanishk.backend.domain.entities.*;
import org.kanishk.backend.exceptions.QrCodeNotFoundException;
import org.kanishk.backend.exceptions.TicketNotFoundException;
import org.kanishk.backend.repositories.QrCodeRepository;
import org.kanishk.backend.repositories.TicketRepository;
import org.kanishk.backend.repositories.TicketValidationRepository;
import org.kanishk.backend.services.TicketValidationService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketValidationServiceImpl implements TicketValidationService {

  private final QrCodeRepository qrCodeRepository;
  private final TicketValidationRepository ticketValidationRepository;
  private final TicketRepository ticketRepository;

  @Override
  public TicketValidation validateTicketByQrCode(UUID qrCodeId) {
    QrCode qrCode = qrCodeRepository.findByIdAndStatus(qrCodeId, QrCodeStatusEnum.ACTIVE)
        .orElseThrow(() -> new QrCodeNotFoundException(
            String.format(
                "QR Code with ID %s was not found", qrCodeId
            )
        ));

    Ticket ticket = qrCode.getTicket();

    return validateTicket(ticket, TicketValidationMethod.QR_SCAN);
  }

  private TicketValidation validateTicket(Ticket ticket,
      TicketValidationMethod ticketValidationMethod) {
    TicketValidation ticketValidation = new TicketValidation();
    ticketValidation.setTicket(ticket);
    ticketValidation.setValidationMethod(ticketValidationMethod);

    TicketValidationStatusEnum ticketValidationStatus = ticket.getValidations().stream()
        .filter(v -> TicketValidationStatusEnum.VALID.equals(v.getStatus()))
        .findFirst()
        .map(v -> TicketValidationStatusEnum.INVALID)
        .orElse(TicketValidationStatusEnum.VALID);

    ticketValidation.setStatus(ticketValidationStatus);

    return ticketValidationRepository.save(ticketValidation);
  }

  @Override
  public TicketValidation validateTicketManually(UUID ticketId) {
    Ticket ticket = ticketRepository.findById(ticketId)
        .orElseThrow(TicketNotFoundException::new);
    return validateTicket(ticket, TicketValidationMethod.MANUAL);
  }
}
