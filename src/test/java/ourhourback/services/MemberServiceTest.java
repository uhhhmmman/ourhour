package ourhourback.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ourhourback.entities.Member;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        // 인덱스를 삭제하는 부분 제거
        // IndexManager.dropIndexes();
    }

    @Test
    void getFriends() {

        // 워밍업을 위한 이메일 목록 생성
        String[] warmupEmails = IntStream.range(1, 20)
                .mapToObj(i -> "user" + i + "@example.com")
                .toArray(String[]::new);

        // 실제 테스트를 위한 이메일 목록 생성
        String[] testEmails = IntStream.range(20, 40)
                .mapToObj(i -> "user" + i + "@example.com")
                .toArray(String[]::new);

        // 워밍업 쿼리 실행
        for (String email : warmupEmails) {
            memberService.getFriendsOfFriends(email);
        }

        System.out.println("인덱스 없는상태의 Warming up ends.");

        // 인덱스 없는 상태에서의 성능 측정
        for (String email : testEmails) {
            long startTime = System.currentTimeMillis();
            List<Member> friendsWithoutIndex = memberService.getFriendsOfFriends(email);
            long endTime = System.currentTimeMillis();
            long timeWithoutIndex = endTime - startTime;
            System.out.println("getFriendsOfFriends - 인덱스 없이 걸린시간: " + timeWithoutIndex + "ms");
            System.out.println("검색결과의 사이즈: "+friendsWithoutIndex.size());
//            System.out.println(friendsWithoutIndex.toString());
        }

        System.out.println("인덱스 없는 상태에서의 성능 측정 종료");
        // 인덱스 생성
        IndexManager.createIndexes();

        try {
            Thread.sleep(5000); // 5초 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 워밍업 쿼리 실행
        for (String email : warmupEmails) {
            memberService.getFriendsOfFriends(email);
        }
        System.out.println("인덱스 있는 상태의 Warming up ends.");

        // 인덱스 있는 상태에서의 성능 측정
        for (String email : testEmails) {
            long startTime = System.currentTimeMillis();
            List<Member> friendsWithIndex = memberService.getFriendsOfFriends(email);
            long endTime = System.currentTimeMillis();
            long timeWithIndex = endTime - startTime;
            System.out.println("getFriendsOfFriends - 인덱스 생성 후 걸린시간: " + timeWithIndex + "ms");
            System.out.println("검색결과의 사이즈: "+friendsWithIndex.size());
//            System.out.println(friendsWithIndex.toString());
        }
        System.out.println("인덱스 있는 상태에서의 성능 측정 종료");
    }
}
