package org.galatea.starter.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.galatea.starter.domain.StockData;

public class StockDataSerializer extends StdSerializer<StockData> {

  /**
   * Blank default constructor.
   */
  public StockDataSerializer() {
    this(null);
  }

  /**
   * Constructor for custom serializer for MongoDocument into JSON.
   * @param md MongoDocument
   */
  public StockDataSerializer(final Class<StockData> md) {
    super(md);
  }

  @Override
  public void serialize(final StockData value, final JsonGenerator gen,
      final SerializerProvider provider) throws IOException {
    gen.writeStartObject();
    gen.writeStringField("date",
        LocalDate.ofInstant(value.getDate(), ZoneId.systemDefault())
            .toString());
    gen.writeNumberField("open", value.getOpen());
    gen.writeNumberField("high", value.getHigh());
    gen.writeNumberField("low", value.getLow());
    gen.writeNumberField("close", value.getClose());
    gen.writeEndObject();
  }
}
