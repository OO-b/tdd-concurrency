package io.hhplus.tdd;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit Test
 * */
@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @InjectMocks
    private PointService pointService;


    @Test
    @DisplayName("포인트 조회 - 존재하지 않는 사용자인 경우 실패")
    void testGetUserPoint_userNotFound() {
        // Given
        long userId = 999; // 존재하지 않는 사용자
        when(userPointTable.selectById(userId)).thenReturn(null);

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                pointService.getUserPoint(userId)
        );

        assertEquals("존재하지 않는 사용자입니다.", thrown.getMessage());  // 예외 메시지 확인
    }

    @Test
    @DisplayName("포인트 충전 - 충전 최대금액을 초과해서 충전하는 경우 실패")
    void whenRechargeMaximumIsExceed_thenFailure() {
        long userId = 1;

        //given
        UserPoint userPointInfo = new UserPoint(userId, 4000L, System.currentTimeMillis()); // 저장포인트 4000

        //when
        when(userPointTable.selectById(userId)).thenReturn(userPointInfo);
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                pointService.chargePoints(userId, 2000L) // 저장 가능금액 초과로 충전
        );

        //then
        assertEquals("충전가능 금액을 초과했습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("포인트 사용 - 충전된 포인트가 없는 사용자가 포인트를 사용하려고 하는 경우 실패")
    void whenNoChargePoints_thenFailure() {

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                pointService.usePoints(1, 2000L)
        );

        // then
        assertEquals("충전된 포인트가 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사용자 포인트 이력조회 - 충전이력이 없는 사용자가 이력조회를 하는 경우 실패")
    void whenCheckChargingHistWithoutChargingHist_thenFailure() {

        //when
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                pointService.getUserPointHist(1)
        );

        // then
        assertEquals("포인트 이력이 존재하지 않습니다.", exception.getMessage());
    }













}
