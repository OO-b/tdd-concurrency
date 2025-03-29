# 동시성 제어방식
## 동시성 이슈
멀티스레드 환경에서 발생하는 문제로서, 여러 스레드가 동시에 공유자원에 접근할때 발생 할 수 있는 문제.
이로인해 데이터의 일관성이 깨지고, 시스템 안정성을 저하시킬 수 있음.

* 동기화  
여러 스레드가 동시에 고유자원에 접근할때, 자원이 일관성있고 안전하게 사용될 수 있도록 제어하는 방식 

## 동시성 제어방식
## 1. synchronized
암시적 Lock 
한번에 하나의 스레드만 접근 가능함. 여러 스레드가 동시에 접근할 수 없게 만들어 동시성 이슈를 막음.  
synchronized 키워드 사용하여 메서드나 블록 전체를 동기화 하는 방식  
#### 장점  
간단하게 사용가능. JVM 지원  
#### 단점
블럭/메소드 전체를 Lock 해서, 여러 스레드 접근시 데드락 발생 (자원낭비)

```
public class SynchronizedExample {
    private int a = 0;
    public synchronized void increment() {
        a++;
    }
    public synchronized void decrement() {
        a--;
    }
}
```

## 2. ReentrantLock
명시적 Lock, java.util.concurrent.locks 패키지에서 제공하는 Lock 
직접적으로 Lock 객체를 생성해서 범위를 지정하여 사용하는 방식. ( lock() - unlock() )
#### 장점
Lock을 획득, 해제하는 타이밍 제어가능(세밀한 동기화)
#### 단점
Lock 해제를 직접 해야함. (해제하지 않으면 데드락 발생)
코드 복잡도 증가.

```
Lock lock = new ReentrantLock();
lock.lock();
try {
    // 공유 자원에 접근하는 코드
} finally {
    lock.unlock();  // 락 해제
}
```

## 3. Atomic 클래스
java.util.concurrent.atomic 패키지  
Atomic variable(AtomicInteger / AtomicLong ..) 등은 Lock 없이도 여러 스레드가 안전하게 값을 변경할 수 있게 함  
CAS(Compare-And-Set) 알고리즘 기반으로 원자성을 보장하며 Lock 없이 연산이 가능함  

#### 장점
Lock을 사용하지 않아 성능이 뛰어나고, 간단한 변수조작에 효율적  
Lock을 사용하지 않아서 데드락 위험이 없음.
#### 단점
복잡한 연산, 여러자원을 동시에 처리해야하는 경우엔 적합하지 않음  
주로 기본 자료형에 대해서만 효율적으로 동작

```
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicExample {
    private AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet();
    }
}
```