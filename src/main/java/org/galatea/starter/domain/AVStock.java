package org.galatea.starter.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;

//reference: https://stackoverflow.com/questions/47263236/how-to-parse-json-object-array-with-jackson-into-dto-in-springboot


@JsonIgnoreProperties(ignoreUnknown = true)
public class AVStock {



  @JsonProperty("Time Series (Daily)")
  private HashMap<String, AVDay> AVTimeSeries;

  public HashMap<String, AVDay> getAVTimeSeries() {
    return AVTimeSeries;
  }

  public void setAVTimeSeries(HashMap<String, AVDay> AVTimeSeries) {
    this.AVTimeSeries = AVTimeSeries;
  }
}