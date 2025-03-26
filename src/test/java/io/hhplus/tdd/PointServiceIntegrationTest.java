package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test
 * */
@SpringBootTest
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Autowired
    private UserPointTable userPointTable;

    // 포인트 조회
    @Test
    @DisplayName("포인트 조회 - 성공")
    void WhenCheckPoint_thenSuccess() {
        long id = 1;

        //given
        userPointTable.insertOrUpdate(id, 3000);

        //when
        UserPoint userPoint = pointService.getUserPoint(id);

        //then (포인트가 동일한지 확인)
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isEqualTo(3000);
    }

    //포인트 충전
    @Test
    @DisplayName("포인트 충전 - 성공")
    void whenChargePoint_thenSuccess() {
        long id = 1;

        //when
        UserPoint userPoint = pointService.chargePoints(id, 1000);

        //then (포인트가 동일한지 확인)
        UserPoint savePoint = userPointTable.selectById(id);

        assertThat(userPoint).isNotNull();
        assertThat(savePoint.point()).isEqualTo(1000);
    }

    //포인트 사용
    @Test
    @DisplayName("포인트 사용 - 성공")
    void whenUsePoint_thenSuccess() {
        long id = 1;

        //given
        userPointTable.insertOrUpdate(id, 3000);

        //when
        UserPoint userPoint = pointService.usePoints(id, 1000);

        //then (포인트가 동일한지 확인)
        assertThat(userPoint).isNotNull();
        assertThat(2000).isEqualTo(userPoint.point());
    }

    //사용자 포인트 이력조회
    @Test
    @DisplayName("포인트 이력조회 - 성공")
    void whenCheckUserPointHist_thenSuccess() {
        long id = 1;

        //given
        pointHistoryTable.insert(id, 3000, TransactionType.CHARGE, System.currentTimeMillis());

        //when
        List<PointHistory> pointHistories = pointService.getUserPointHist(id);

        //then
        assertThat(pointHistories).hasSize(1);
        assertThat(pointHistories.get(0).amount()).isEqualTo(3000);
    }
}
