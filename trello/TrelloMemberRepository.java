package com.sharkdom.repository.trello;

import com.sharkdom.entity.trello.TrelloMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrelloMemberRepository extends JpaRepository<TrelloMember, String> {
}
