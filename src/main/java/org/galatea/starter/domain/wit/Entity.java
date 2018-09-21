package org.galatea.starter.domain.wit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
public class Entity {
  private Double confidence;
  private String value;
  private String type;
}
