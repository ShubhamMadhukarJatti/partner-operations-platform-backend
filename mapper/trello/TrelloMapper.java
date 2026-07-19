package com.sharkdom.mapper.trello;

import com.sharkdom.entity.trello.TrelloBoard;
import com.sharkdom.entity.trello.TrelloCard;
import com.sharkdom.entity.trello.TrelloList;
import com.sharkdom.entity.trello.TrelloMember;
import com.sharkdom.model.trello.TrelloCardDto;
import com.sharkdom.model.trello.client.TrelloBoardResponse;
import com.sharkdom.model.trello.client.TrelloCardResponse;
import com.sharkdom.model.trello.client.TrelloListResponse;
import com.sharkdom.model.trello.client.TrelloMemberResponse;
import java.util.Collections;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrelloMapper {

    @Mapping(source = "id", target = "boardId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "desc", target = "description")
    @Mapping(source = "url", target = "url")
    @Mapping(source = "closed", target = "isClosed")
    @Mapping(source = "pinned", target = "isPinned")
    @Mapping(source = "memberships", target = "memberships")
    TrelloBoard mapToTrelloBoardEntity(TrelloBoardResponse trelloBoardResponse);

    @Mapping(source = "id", target = "listId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "closed", target = "isClosed")
    @Mapping(source = "pos", target = "position")
    @Mapping(source = "subscribed", target = "isSubscribed")
    @Mapping(source = "datasource.filter", target = "datasource.isFilter")
    TrelloList mapToTrelloListEntity(TrelloListResponse trelloListResponse);

    @Mapping(source = "id", target = "cardId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "desc", target = "description")
    @Mapping(source = "url", target = "url")
    @Mapping(source = "due", target = "dueDate")
    @Mapping(source = "closed", target = "isClosed")
    @Mapping(source = "pos", target = "position")
    TrelloCard mapToTrelloCardEntity(TrelloCardResponse trelloCardResponse);

    @Mapping(source = "id", target = "memberId")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "url", target = "url")
    TrelloMember mapToTrelloMemberEntity(TrelloMemberResponse trelloMemberResponse);

    @Mapping(target = "listId", source = "list.listId")
    @Mapping(target = "boardId", source = "board.boardId")
    @Mapping(target = "members", source = "members", qualifiedByName = "mapMembersToIds")
    TrelloCardDto toDto(TrelloCard trelloCard);

    List<TrelloCardDto> toDtoList(List<TrelloCard> trelloCards);

    @Named("mapMembersToIds")
    default Set<String> mapMembersToIds(Set<TrelloMember> members) {
        if (members == null) {
            return Collections.emptySet();
        }
        return members.stream()
                .map(TrelloMember::getMemberId)
                .collect(Collectors.toSet());
    }

}
