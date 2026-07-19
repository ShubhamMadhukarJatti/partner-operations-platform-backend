package com.sharkdom.repository.organizationcollaboration;

import com.sharkdom.constants.Flag;
import com.sharkdom.entity.organizationcollaboration.ChannelFlag;
import com.sharkdom.entity.organizationcollaboration.OrganizationMessages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository

public interface OrganizationMessagesRepository extends JpaRepository<OrganizationMessages, Long> {
    List<OrganizationMessages> findAllByChatRoomIdOrderByCreationTimestampAsc(Long chatRoomId);

    Page<OrganizationMessages> findAllByChatRoomIdOrderByCreationTimestampDesc(Long chatRoomId, Pageable pageable);

    List<OrganizationMessages> findFirstByChatRoomIdOrderByCreationTimestampDesc(Long chatRoomId);

    long countByChatRoomId(Long chatRoomId);

    @Query("SELECT m.chatRoomId, COUNT(m) " + "FROM OrganizationMessages m " + "WHERE (m.senderId = :orgId OR m.receiverId = :orgId) " + "AND m.isRead = false " + "AND m.chatRoomId IS NOT NULL " + "GROUP BY m.chatRoomId")
    List<Object[]> countUnreadMessagesForOrg(@Param("orgId") Long orgId);

    @Query("SELECT m FROM OrganizationMessages m " + "WHERE (m.senderId = :orgId OR m.receiverId = :orgId) " + "AND m.chatRoomId IS NOT NULL " + "ORDER BY m.creationTimestamp DESC")
    List<OrganizationMessages> findLastMessagesForOrg(@Param("orgId") Long orgId);


    @Query("SELECT count(*) FROM OrganizationMessages m WHERE  m.senderId = :orgId AND m.chatRoomId in (:collaborationsIds) AND m.creationTimestamp >= :lastWeek AND m.creationTimestamp <= :now")
    Long countMessageInLastWeek(@Param("orgId") Long orgId, List<Long> collaborationsIds, @Param("lastWeek") LocalDateTime lastWeek,
                                @Param("now") LocalDateTime now);

    Page<OrganizationMessages> findAllByChatRoomIdAndChannelFlagOrderByCreationTimestampDesc(Long chatRoomId, ChannelFlag channelType, Pageable pageable);

    Page<OrganizationMessages> findAllByChatRoomIdAndFlagOrderByCreationTimestampDesc(Long chatRoomId, Flag flag, Pageable pageable);

    Page<OrganizationMessages> findAllByChatRoomIdAndChannelFlagAndFlagOrderByCreationTimestampDesc(Long chatRoomId, ChannelFlag channelType, Flag flag, Pageable pageable);

    boolean existsByChatRoomId(Long chatRoomId);
}
