package io.hhplus.tdd;

import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointServiceConcurrencyTest {


    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("2개 아이디 여러건 충전 - 동시성 테스트")
    void chargingPointBy2IdConcurrencyTest() throws InterruptedException {

        int count = 500; // 충전횟수
        long amount = 10L; // 1회당 충전금액
        int user1Id = 1; // 첫 번째 사용자 ID
        int user2Id = 2; // 두 번째 사용자 ID

        ExecutorService executorService = Executors.newFixedThreadPool(count); // 고정된 크기 Thread Pool 생성
        CountDownLatch countDownLatch = new CountDownLatch(count); // 특정 작업이 완료될때까지 기다림
        for(int i =0 ; i < count; i++) {

            int userId = (i % 2 == 0) ? user1Id : user2Id; // 사용자 ID를 번갈아 가며 할당 (1번과 2번 ID 번갈아 가며 충전)

            executorService.execute(() -> {
                try {
                    pointService.chargePoints(userId, amount); // 포인트 충전
                }catch(Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    countDownLatch.countDown(); // (예외 발생해도) 작업 후 카운트 감소
                }

            });
        }

        countDownLatch.await(); // 0 이 될때까지 현재 스레드를 대기시킴
        executorService.shutdown(); // 스레드 풀 종료

        long expectAmount = (count / 2) * amount; // 기대값
        UserPoint userPointByUserId1 = pointService.getUserPoint(1); // 결과값
        UserPoint userPointByUserId2 = pointService.getUserPoint(2); // 결과값

        assertThat(expectAmount).isEqualTo(userPointByUserId1.point());
        assertThat(expectAmount).isEqualTo(userPointByUserId2.point());

    }

    @Test
    @DisplayName("동일한 아이디 여러건 사용 - 동시성 테스트")
    void usingPointBy1IdConcurrencyTest() throws InterruptedException {

        //given
        UserPoint chargePointInfo  = pointService.chargePoints(1, 500L); // 포인트 충전

        int count = 100;
        long amount = 5L;

        ExecutorService executorService = Executors.newFixedThreadPool(count); // 고정된 크기 Thread Pool 생성
        CountDownLatch countDownLatch = new CountDownLatch(count); // 특정 작업이 완료될때까지 기다림
        for(int i =0 ; i < count; i++) {
            executorService.execute(() -> {
                try {
                    pointService.usePoints(1, amount); // 포인트 사용
                }catch(Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    countDownLatch.countDown(); // (예외 발생해도) 작업 후 카운트 감소
                }

            });
        }

        countDownLatch.await(); // 0 이 될때까지 현재 스레드를 대기시킴
        executorService.shutdown(); // 스레드 풀 종료

        long expectAmount = chargePointInfo.point() - (amount * count); // 기대값
        UserPoint userPoint = pointService.getUserPoint(1); // 결과값
        assertThat(expectAmount).isEqualTo(userPoint.point());

    }

}
