package org.galatea.starter.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.time.OffsetTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.galatea.starter.utils.StockDataSerializer;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonSerialize(using = StockDataSerializer.class)
@NoArgsConstructor
@Document
@Data public class StockData {
  //including extended hours
  public static OffsetTime NYSE_CLOSE_TIME_OFFSET = OffsetTime.parse("20:00:00-04:00");

  @Id
  private String id;
  private String ticker;
  private Instant date;
  @JsonProperty(value = "1. open") private Double open;
  @JsonProperty(value = "2. high") private Double high;
  @JsonProperty(value = "3. low") private Double low;
  @JsonProperty(value = "4. close") private Double close;
}
