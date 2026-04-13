package com.train.repository;

import com.train.entity.Friendship;
import com.train.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    @Query("SELECT f FROM Friendship f WHERE (f.requester = :user OR f.receiver = :user) AND f.status = 'ACCEPTED' ORDER BY f.createdAt DESC")
    List<Friendship> findFriends(@Param("user") User user);
    
    @Query("SELECT f FROM Friendship f WHERE f.requester = :user AND f.status = 'PENDING'")
    List<Friendship> findSentFriendRequests(@Param("user") User user);
    
    @Query("SELECT f FROM Friendship f WHERE f.receiver = :user AND f.status = 'PENDING'")
    List<Friendship> findReceivedFriendRequests(@Param("user") User user);
    
    @Query("SELECT COUNT(f) FROM Friendship f WHERE (f.requester = :user OR f.receiver = :user) AND f.status = 'ACCEPTED'")
    long countFriends(@Param("user") User user);
    
    @Query("SELECT f FROM Friendship f WHERE (f.requester = :userA AND f.receiver = :userB) OR (f.requester = :userB AND f.receiver = :userA)")
    Optional<Friendship> findFriendshipBetween(@Param("userA") User userA, @Param("userB") User userB);
    
    @Query("SELECT f FROM Friendship f WHERE f.receiver = :user AND f.status = 'BLOCKED'")
    List<Friendship> findBlockedUsers(@Param("user") User user);
    
    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.receiver = :user AND f.status = 'PENDING'")
    long countPendingFriendRequests(@Param("user") User user);
}