package org.kanishk.backend.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.kanishk.backend.domain.entities.Ticket;
import org.kanishk.backend.domain.entities.TicketStatusEnum;
import org.kanishk.backend.domain.entities.TicketType;
import org.kanishk.backend.domain.entities.User;
import org.kanishk.backend.exceptions.TicketTypeNotFoundException;
import org.kanishk.backend.exceptions.TicketsSoldOutException;
import org.kanishk.backend.exceptions.UserNotFoundException;
import org.kanishk.backend.repositories.TicketRepository;
import org.kanishk.backend.repositories.TicketTypeRepository;
import org.kanishk.backend.repositories.UserRepository;
import org.kanishk.backend.services.QrCodeService;
import org.kanishk.backend.services.TicketTypeService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

  private final UserRepository userRepository;
  private final TicketTypeRepository ticketTypeRepository;
  private final TicketRepository ticketRepository;
  private final QrCodeService qrCodeService;

  @Override
  @Transactional
  public Ticket purchaseTicket(UUID userId, UUID ticketTypeId) {
    User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
        String.format("User with ID %s was not found", userId)
    ));

    TicketType ticketType = ticketTypeRepository.findByIdWithLock(ticketTypeId)
        .orElseThrow(() -> new TicketTypeNotFoundException(
            String.format("Ticket type with ID %s was not found", ticketTypeId)
        ));

    int purchasedTickets = ticketRepository.countByTicketTypeId(ticketType.getId());
    Integer totalAvailable = ticketType.getTotalAvailable();

    if(purchasedTickets + 1 > totalAvailable) {
      throw new TicketsSoldOutException();
    }

    Ticket ticket = new Ticket();
    ticket.setStatus(TicketStatusEnum.PURCHASED);
    ticket.setTicketType(ticketType);
    ticket.setPurchaser(user);

    Ticket savedTicket = ticketRepository.save(ticket);
    qrCodeService.generateQrCode(savedTicket);

    return ticketRepository.save(savedTicket);
  }
}
