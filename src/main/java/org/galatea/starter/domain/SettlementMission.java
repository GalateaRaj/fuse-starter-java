package org.galatea.starter.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.DecimalMin;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.galatea.starter.utils.validation.StringEnumeration;

/* For builder since we explicitly want to make the all args ctor private */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE) // For spring and jackson
@Builder
@Data
@Entity
@XmlRootElement(name = "settlementMission")
public class SettlementMission {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  protected Long id;

  @NonNull
  protected String instrument;

  @NonNull
  protected String externalParty;

  @NonNull
  protected String depot;

  @StringEnumeration(enumClass = Direction.class, message = "Direction must be valid")
  @NonNull
  protected String direction;

  @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
  @NonNull
  protected Double qty;

  @Version
  @NonNull
  protected Long version;
}
