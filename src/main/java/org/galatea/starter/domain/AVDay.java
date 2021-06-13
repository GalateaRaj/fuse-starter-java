package org.galatea.starter.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

//reference: https://stackoverflow.com/questions/47263236/how-to-parse-json-object-array-with-jackson-into-dto-in-springboot
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AVDay{
  @JsonProperty("4. close")
  private double price;
}