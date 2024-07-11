package ourhourback.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ourhourback.entities.Friendship;
import ourhourback.entities.Member;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship,Long> {

    boolean existsByFromUserAndToUser(Member fromUser, Member toUser);

    @Query("SELECT f FROM Friendship f JOIN FETCH f.fromUser JOIN FETCH " +
            "f.toUser WHERE f.fromUser = :fromUser AND f.status = :status")
    List<Friendship> findAllByFromUserAndStatus(Member fromUser, Friendship.Status status);

    @Query("SELECT f FROM Friendship f JOIN FETCH f.fromUser JOIN FETCH " +
            "f.toUser WHERE f.toUser = :toUser AND f.status = :status")
    List<Friendship> findAllByToUserAndStatus(Member toUser, Friendship.Status status);

    @Query("SELECT f FROM Friendship f JOIN FETCH f.fromUser JOIN FETCH " +
            "f.toUser WHERE f.fromUser = :fromUser AND f.toUser = :toUser")
    Optional<Friendship> findByFromUserAndToUser(Member fromUser, Member toUser);

    @Query("SELECT f.toUser FROM Friendship f WHERE " +
            "f.fromUser.id = :userId AND f.status = 'ACCEPTED'")
    List<Member> findFriendsByUserId(Long userId);
}
