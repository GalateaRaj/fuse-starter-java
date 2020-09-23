package org.galatea.starter.domain.rpsy;

import junitparams.JUnitParamsRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.ASpringTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

@RequiredArgsConstructor
@Slf4j
@DataMongoTest
@RunWith(JUnitParamsRunner.class)
public class StockPriceRepositoryTest extends ASpringTest {
  // Test for pulling from MongoDB

  // Test putting to Mongo and then pulling the same
  @DisplayName("given object to save when save object using MongoDB template then object is saved")
  @Test
  public void test(@Autowired MongoTemplate mongoTemplate) {

//    // when
//    mongoTemplate.save(objectToSave);
//
//    // then
//    assertThat(mongoTemplate.findAll(DBObject.class, "collection")).extracting("key").containes
  }

  // Test
}
