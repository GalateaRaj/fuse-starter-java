package org.galatea.starter.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.galatea.starter.domain.AVTimeSeries;

@Data
public class PriceFinderResponse {

  public PriceFinderResponse (PriceFinderStock stock) {
    this.stock = stock;
  }
 @JsonProperty
  private PriceFinderStock stock;

}
