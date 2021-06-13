package org.galatea.starter.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import lombok.Builder;
import lombok.Data;
import org.galatea.starter.domain.AVTimeSeries;

@Data
public class PriceFinderStock {

  public PriceFinderStock(String metadata, List<DateAndPrice> prices) {
    this.metadata = metadata;
    this.prices = prices;
  }


  @JsonProperty
  private String metadata;

  @JsonProperty
  private  List<DateAndPrice> prices;

}