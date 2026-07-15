package org.kanishk.backend.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kanishk.backend.domain.entities.TicketStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTicketResponseDto {
  private UUID id;
  private TicketStatusEnum status;
  private Double price;
  private String description;
  private String eventName;
  private String eventVenue;
  private LocalDateTime eventStart;
  private LocalDateTime eventEnd;
}
