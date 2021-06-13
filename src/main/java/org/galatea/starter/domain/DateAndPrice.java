package org.galatea.starter.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DateAndPrice {

  public DateAndPrice (String date, double price) {
    this.date = date;
    this.price = price;
  }

  @JsonProperty
  private String date;

  @JsonProperty
  private double price;

}