package org.galatea.starter.entrypoint;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.sf.aspect4log.Log;
import net.sf.aspect4log.Log.Level;
import org.galatea.starter.domain.SettlementMission;
import org.galatea.starter.domain.TradeAgreement;
import org.galatea.starter.service.SettlementService;
import org.galatea.starter.utils.Tracer;

/**
 * Implements a base class for settlement rest controllers to avoid duplicating
 * the logic of calling the settlement service.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
@Slf4j
@Log(enterLevel = Level.INFO, exitLevel = Level.INFO)
public abstract class BaseSettlementRestController {

  @NonNull
  SettlementService settlementService;

  /**
   * Adds the specified requestId to the context for this request (if not null).
   */
  protected void processRequestId(String requestId) {
    if (requestId != null) {
      log.info("Request received with id: {}", requestId);
      Tracer.setExternalRequestId(requestId);
    }
  }

  /**
   * Invokes the settlement service to spawn missions for the specified trade agreements.
   */
  protected Set<Long> settleAgreementInternal(List<TradeAgreement> agreements) {
    return settlementService.spawnMissions(agreements);
  }

  /**
   * Retrieves settlement missions from the settlement service.
   */
  protected Optional<SettlementMission> getMissionInternal(final Long id) {
    return settlementService.findMission(id);
  }
}
