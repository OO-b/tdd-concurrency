package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {

    private final PointHistoryTable pointHistoryTable; // 사용자 포인트 이력 테이블
    private final UserPointTable userPointTable; // 사용자 포인트 테이블
    private final Lock lock = new ReentrantLock(); // Lock 객체 생성
    private final ConcurrentHashMap<Long, Lock> locks = new ConcurrentHashMap<>();

    @Autowired
    public PointService(PointHistoryTable pointHistoryTable, UserPointTable userPointTable) {
        this.pointHistoryTable = pointHistoryTable;
        this.userPointTable = userPointTable;
    }

    /**
     * 사용자 포인트 충전
     * - id, amount 에 대한 유효성 검증은 controller에서 했다고 가정
     * @param id 사용자 아이디
     * @param amount 충전 포인트 금액
     * */
    public UserPoint chargePoints(long id, long amount) {

        final long maxRechargeLimit = 5000L; // 충전가능 최대 금액

        Lock userLock = locks.computeIfAbsent(id, key -> new ReentrantLock());

        userLock.lock(); // Lock

        try {
            UserPoint userPointInfo = userPointTable.selectById(id);
            long savePoint = (userPointInfo == null)? 0 : userPointInfo.point(); //저장된 포인트

            // 충전가능 금액 초과 시
            if(savePoint + amount > maxRechargeLimit || amount > maxRechargeLimit ) throw new IllegalArgumentException("충전가능 금액을 초과했습니다.");

            // 사용자 포인트 충전
            long totalAmount = savePoint + amount;
            UserPoint userPoint = userPointTable.insertOrUpdate(id, totalAmount);

            // 포인트 충전이력 저장
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, userPoint.updateMillis());

            return userPoint;
        } finally {
            userLock.unlock(); // Lock 해제
        }
    }

    /**
     * 사용자 포인트 사용
     * - id, amount 에 대한 유효성 검증은 controller에서 했다고 가정
     * @param id 사용자 아이디
     * @param amount 사용 포인트 금액
     * */
    public UserPoint usePoints(long id, long amount) {

        lock.lock(); // Lock

        try {
            // 현재 포인트 확인
            UserPoint userPointInfo = userPointTable.selectById(id);

            // 충전된 포인트가 없는 사용자의 경우
            if (userPointInfo == null) throw new IllegalArgumentException("충전된 포인트가 없습니다.");

            // 잔고에 남은 포인트보다 사용하려는 금액이 더 많은 경우
            if (userPointInfo.point() < amount) throw new RuntimeException("잔고가 부족합니다.");

            // 사용자 포인트 사용
            long savePoint = userPointInfo.point() - amount;
            UserPoint userPoint = userPointTable.insertOrUpdate(id, savePoint);

            // 포인트 충전이력 저장
            pointHistoryTable.insert(id, amount, TransactionType.USE, userPoint.updateMillis());

            return userPoint;

        } finally {
            lock.unlock(); // Lock 해제
        }
    }

    /**
     * 사용자 포인트 조회
     * - id, amount 에 대한 유효성 검증은 controller에서 했다고 가정
     * @param id 사용자 아이디
     * @return 사용자 포인트 정보
     * */
    public UserPoint getUserPoint(long id) {

        UserPoint userPointInfo = userPointTable.selectById(id);

        //  존재하지 않는 사용자인 경우
        if(userPointInfo == null) throw new IllegalArgumentException("존재하지 않는 사용자입니다.");

        return userPointTable.selectById(id);
    }

    /**
     * 사용자 포인트 이력 조회
     * - id, amount 에 대한 유효성 검증은 controller에서 했다고 가정
     * @param id 사용자 아이디
     * @return 사용자 포인트 정보
     * */
    public List<PointHistory> getUserPointHist(long id) {

        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(id);

        //  존재하지 않는 사용자인 경우
        if(pointHistories.size() == 0) throw new RuntimeException("포인트 이력이 존재하지 않습니다.");

        return pointHistories;

    }
}
