package ourhourback.services;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ourhourback.dtos.UserDTO;
import ourhourback.entities.Friendship;
import ourhourback.entities.Member;
import ourhourback.repositories.FriendshipRepository;
import ourhourback.repositories.MemberRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Transactional
    public void addFriend(String senderEmail, String receiverEmail) {
        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("보낸사람을 찾을 수 없습니다."));
        Member receiver = memberRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new IllegalArgumentException("받는 사람을 찾을 수 없습니다."));

        // 친구관계 중복체크(양방향 체크)
        if (friendshipRepository.existsByFromUserAndToUser(sender, receiver)) {
            throw new IllegalArgumentException("이미 " + receiverEmail + "에게 친구 요청을 보냈습니다.");
        }
        if (friendshipRepository.existsByFromUserAndToUser(receiver, sender)) {
            throw new IllegalArgumentException(receiverEmail + "가 이미 " + senderEmail + "에게 친구 요청을 보냈습니다.");
        }
        // 친구관계 생성
        Friendship friendship = Friendship.builder()
                .fromUser(sender)
                .toUser(receiver)
                .status(Friendship.Status.PENDING)
                .build();

        friendshipRepository.save(friendship);

    }

    @Transactional
    public void acceptFriendRequest(String senderEmail, String receiverEmail) {
        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("보낸사람을 찾을 수 없습니다."));
        Member receiver = memberRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new IllegalArgumentException("받는 사람을 찾을 수 없습니다."));

        Friendship friendship = friendshipRepository.findByFromUserAndToUser(sender, receiver)
                .orElseThrow(() -> new IllegalArgumentException("친구요청이 없습니다."));

        if (friendship.getStatus() != Friendship.Status.PENDING) {
            throw new IllegalArgumentException("요청상태가 PENDING 이 아닙니다.");
        }

        friendship.setStatus(Friendship.Status.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void rejectFriendRequest(String senderEmail, String receiverEmail) {
        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("보낸사람을 찾을 수 없습니다."));
        Member receiver = memberRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new IllegalArgumentException("받는 사람을 찾을 수 없습니다."));

        Friendship friendship = friendshipRepository.findByFromUserAndToUser(sender, receiver)
                .orElseThrow(() -> new IllegalArgumentException("친구요청이 없습니다."));

        if (friendship.getStatus() != Friendship.Status.PENDING) {
            throw new IllegalArgumentException("요청상태가 PENDING 이 아닙니다.");
        }

        friendship.setStatus(Friendship.Status.REJECTED);
        friendshipRepository.save(friendship);
    }



    public List<Member> getFriends(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Friendship> fromUserFriends = friendshipRepository.findAllByFromUserAndStatus(member, Friendship.Status.ACCEPTED);
        List<Friendship> toUserFriends = friendshipRepository.findAllByToUserAndStatus(member, Friendship.Status.ACCEPTED);

        Set<Member> allFriends = new HashSet<>();
        for (Friendship friendship : fromUserFriends) {
            allFriends.add(friendship.getToUser());
        }
        for (Friendship friendship : toUserFriends) {
            allFriends.add(friendship.getFromUser());
        }

        return new ArrayList<>(allFriends);
    }

    public List<Friendship> getSentRequests(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return friendshipRepository.findAllByFromUserAndStatus(member, Friendship.Status.PENDING);
    }

    public List<Friendship> getReceivedRequests(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return friendshipRepository.findAllByToUserAndStatus(member, Friendship.Status.PENDING);
    }

    public void deleteFriend(String userEmail, String friendEmail) {
        Member user = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Member friend = memberRepository.findByEmail(friendEmail)
                .orElseThrow(() -> new IllegalArgumentException("Friend not found"));

        // Check both directions
        Friendship friendship = friendshipRepository.findByFromUserAndToUser(user, friend)
                .orElseGet(() -> friendshipRepository.findByFromUserAndToUser(friend, user)
                        .orElseThrow(() -> new IllegalArgumentException("Friendship not found")));

        friendshipRepository.delete(friendship);
    }

    //인덱스 성능향상을 위한 복잡한 쿼리, 친구의 친구 찾기
    public List<Member> getFriendsOfFriends(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

//        // 직접 친구
//        Set<Friendship> directFriends = new HashSet<>(friendshipRepository.findAllByFromUserAndStatus(member, Friendship.Status.ACCEPTED));
//        directFriends.addAll(friendshipRepository.findAllByToUserAndStatus(member, Friendship.Status.ACCEPTED));

        // 직접 친구
        Set<Member> directFriends = getFriends(member);

        // 친구의 친구
        Set<Member> friendsOfFriends = new HashSet<>();
        for (Member friend : directFriends) {
            Set<Member> secondLevelFriends = getFriends(friend);
            for (Member secondLevelFriend : secondLevelFriends) {
                if (!secondLevelFriend.equals(member) && !directFriends.contains(secondLevelFriend)) {
                    friendsOfFriends.add(secondLevelFriend);
                }
            }
        }
//        // 친구의 친구
//        Set<Member> friendsOfFriends = new HashSet<>();
//        for (Friendship friendship : directFriends) {
//            Member friend = friendship.getFromUser().equals(member) ? friendship.getToUser() : friendship.getFromUser();
//            Set<Friendship> secondLevelFriends = new HashSet<>(friendshipRepository.findAllByFromUserAndStatus(friend, Friendship.Status.ACCEPTED));
//            secondLevelFriends.addAll(friendshipRepository.findAllByToUserAndStatus(friend, Friendship.Status.ACCEPTED));
//
//            for (Friendship secondLevelFriendship : secondLevelFriends) {
//                Member secondLevelFriend = secondLevelFriendship.getFromUser().equals(friend) ? secondLevelFriendship.getToUser() : secondLevelFriendship.getFromUser();
//                if (!secondLevelFriend.equals(member)) {
//                    friendsOfFriends.add(secondLevelFriend);
//                }
//            }
//        }

        return new ArrayList<>(friendsOfFriends);
    }

    // 중복제거해서 친구찾기, 친구의 친구 찾기에서 사용함
    private Set<Member> getFriends(Member member) {
        Set<Friendship> friendships = new HashSet<>(friendshipRepository.findAllByFromUserAndStatus(member, Friendship.Status.ACCEPTED));
        friendships.addAll(friendshipRepository.findAllByToUserAndStatus(member, Friendship.Status.ACCEPTED));

        Set<Member> friends = new HashSet<>();
        for (Friendship friendship : friendships) {
            Member friend = friendship.getFromUser().equals(member) ? friendship.getToUser() : friendship.getFromUser();
            friends.add(friend);
        }
        return friends;
    }

    //공통된 친구 찾기
    public List<UserDTO> findCommonFriendsByEmails(String email1, String email2) {
        Optional<Member> member1 = memberRepository.findByEmail(email1);
        Optional<Member> member2 = memberRepository.findByEmail(email2);

        if (member1.isEmpty() || member2.isEmpty()) {
            throw new IllegalArgumentException("One or both emails are invalid.");
        }

        List<Member> friendsOfUser1 = friendshipRepository.findFriendsByUserId(member1.get().getId());
        List<Member> friendsOfUser2 = friendshipRepository.findFriendsByUserId(member2.get().getId());

        List<Member> commonFriends = friendsOfUser1.stream()
                .filter(friendsOfUser2::contains)
                .toList();

        return commonFriends.stream()
                .map(friend -> new UserDTO(friend.getEmail(), friend.getNickname(), friend.getLanguage()))
                .collect(Collectors.toList());
    }

}
