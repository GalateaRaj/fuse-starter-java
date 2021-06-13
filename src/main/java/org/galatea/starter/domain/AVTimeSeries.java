package org.galatea.starter.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.galatea.starter.domain.AVDay;
import java.util.*;

//reference: https://stackoverflow.com/questions/47263236/how-to-parse-json-object-array-with-jackson-into-dto-in-springboot

@JsonIgnoreProperties(ignoreUnknown = true)
public class AVTimeSeries {

  private HashMap<String, AVDay> AVDays;

  public HashMap<String, AVDay> getDays() {
    return AVDays;
  }

  public void setDays(HashMap<String, AVDay> AVDays) {
    this.AVDays = AVDays;
  }

}