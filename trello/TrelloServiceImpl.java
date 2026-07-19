package com.sharkdom.service.trello;

import com.sharkdom.client.trello.TrelloClient;
import com.sharkdom.entity.trello.*;
import com.sharkdom.mapper.trello.TrelloMapper;
import com.sharkdom.model.trello.TrelloCardDto;
import com.sharkdom.model.trello.client.TrelloBoardResponse;
import com.sharkdom.model.trello.client.TrelloCardResponse;
import com.sharkdom.model.trello.client.TrelloListResponse;
import com.sharkdom.model.trello.client.TrelloMemberResponse;
import com.sharkdom.repository.trello.TrelloBoardRepository;
import com.sharkdom.repository.trello.TrelloCardRepository;
import com.sharkdom.repository.trello.TrelloListRepository;
import com.sharkdom.repository.trello.TrelloMemberRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrelloServiceImpl implements TrelloService {

    @Resource
    private TrelloServiceImpl trelloService;

    private final TrelloClient trelloClient;
    private final TrelloBoardRepository boardRepository;
    private final TrelloListRepository listRepository;
    private final TrelloCardRepository cardRepository;
    private final TrelloMemberRepository memberRepository;
    private final TrelloMapper trelloMapper;

    @Override
    @Transactional
    public List<TrelloBoard> getAllBoard(String memberId) {
        try {
            List<TrelloBoardResponse> trelloBoardResponses = trelloClient.fetchAllBoard(memberId);
            List<TrelloBoard> trelloBoardList = new ArrayList<>();
            for (TrelloBoardResponse trelloBoardResponse : trelloBoardResponses) {
                TrelloBoard trelloBoard = trelloMapper.mapToTrelloBoardEntity(trelloBoardResponse);
                trelloBoard.setLastSync(LocalDateTime.now());
                trelloBoard.setMemberships(trelloBoard.getMemberships());
                trelloBoard.setMembers(processMembersForBoard(trelloBoardResponse));
                TrelloBoard savedTrelloBoard = boardRepository.findById(trelloBoard.getBoardId()).orElse(boardRepository.save(trelloBoard));
                trelloBoardList.add(savedTrelloBoard);
            }
            log.info("Successfully updated, created or fetched Trello Boards");
            return trelloBoardList;
        } catch (Exception e) {
            log.error("An unexpected error occurred while getting all board {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public TrelloMember getMember(String memberId) {
        try {
            TrelloMemberResponse trelloMemberResponse = trelloClient.fetchMember(memberId);
            TrelloMember trelloMember = trelloMapper.mapToTrelloMemberEntity(trelloMemberResponse);
//            trelloMember.setBoards(trelloService.getAllBoard(memberId));
            trelloMember.setLastSync(LocalDateTime.now());
            TrelloMember savedTrelloMember = memberRepository.findById(trelloMember.getMemberId()).orElse(memberRepository.save(trelloMember));
            log.info("Successfully updated, created or fetched Trello member: {}", savedTrelloMember);
            return savedTrelloMember;
        } catch (Exception e) {
            log.error("An unexpected error occurred while getting member {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public List<TrelloList> getListForBoard(String boardId) {
        try {
            List<TrelloList> returnTrelloList = new ArrayList<>();
            if (ObjectUtils.isEmpty(boardId)) {
                List<TrelloBoard> allBoard = trelloService.getAllBoard(null);
                for (TrelloBoard trelloBoard : allBoard) {
                    extractedTrelloList(trelloBoard.getBoardId(), returnTrelloList);
                }
            } else {
                extractedTrelloList(boardId, returnTrelloList);
            }
            log.info("Successfully updated, created or fetched Trello List");
            return returnTrelloList;
        } catch (Exception e) {
            log.error("An unexpected error occurred while getting List for board {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public List<TrelloCardDto> getCardForList(String listId) {
        try {
            List<TrelloCard> returnTrelloCard = new ArrayList<>();
            if (ObjectUtils.isEmpty(listId)) {
                List<TrelloList> allList = trelloService.getListForBoard(null);
                for (TrelloList trelloList : allList) {
                    extractedTrelloCard(trelloList.getListId(), returnTrelloCard);
                }
            } else {
                extractedTrelloCard(listId, returnTrelloCard);
            }
            log.info("Successfully updated, created or fetched Trello Card");
            return trelloMapper.toDtoList(returnTrelloCard);
        } catch (Exception e) {
            log.error("An unexpected error occurred while getting Card for list {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public TrelloCardDto createCardForList(String idList, String name, String description) {
        TrelloCardResponse cardForList = trelloClient.createCardForList(idList, name, description);
        TrelloCard savedAndMappedTrelloCard = getSavedAndMappedTrelloCard(cardForList);
        return trelloMapper.toDto(savedAndMappedTrelloCard);
    }

    private void extractedTrelloList(String trelloBoardId, List<TrelloList> returnTrelloList) {
        for (TrelloListResponse trelloListResponse : trelloClient.fetchListForBoard(trelloBoardId)) {
            TrelloList trelloList = trelloMapper.mapToTrelloListEntity(trelloListResponse);
            TrelloBoard foundTrelloBoard = boardRepository.findById(trelloListResponse.idBoard()).orElse(null);
            trelloList.setBoard(foundTrelloBoard);
            trelloList.setLastSync(LocalDateTime.now());
            TrelloList savedList = listRepository.findById(trelloList.getListId()).orElse(listRepository.save(trelloList));
            returnTrelloList.add(savedList);
        }
    }

    private void extractedTrelloCard(String listId, List<TrelloCard> returnTrelloCard) {
        for (TrelloCardResponse trelloCardResponse : trelloClient.fetchCardForList(listId)) {
            TrelloCard savedTrelloCard = getSavedAndMappedTrelloCard(trelloCardResponse);
            returnTrelloCard.add(savedTrelloCard);
        }
    }

    @NotNull
    private TrelloCard getSavedAndMappedTrelloCard(TrelloCardResponse trelloCardResponse) {
        TrelloCard trelloCard = trelloMapper.mapToTrelloCardEntity(trelloCardResponse);
        trelloCard.setLastSync(LocalDateTime.now());
        trelloCard.setLastActivity(parseDate(trelloCardResponse.dateLastActivity()));
        TrelloList foundTrelloList = listRepository.findById(trelloCardResponse.idList()).orElse(null);
        TrelloBoard foundTrelloBoard = boardRepository.findById(trelloCardResponse.idBoard()).orElse(null);
        trelloCard.setList(foundTrelloList);
        trelloCard.setBoard(foundTrelloBoard);
        trelloCard.setMembers(processMembersForCard(trelloCardResponse));
        return cardRepository.findById(trelloCard.getCardId()).orElse(cardRepository.save(trelloCard));
    }

    private Set<TrelloMember> processMembersForCard(TrelloCardResponse response) {
        if (ObjectUtils.isEmpty(response.idMembers())) {
            return new HashSet<>();
        }
        return response.idMembers().stream()
                .map(memberId -> memberRepository.findById(memberId)
                        .orElseGet(() -> trelloService.getMember(memberId)))
                .collect(Collectors.toSet());
    }

    private Set<TrelloMember> processMembersForBoard(TrelloBoardResponse response) {
        return response.memberships().stream()
                .map(membership -> memberRepository.findById(membership.idMember())
                        .orElseGet(() -> trelloService.getMember(membership.idMember())))
//                {
//                    Optional<TrelloMember> existing = memberRepository.findById(membership.idMember());
//                    if (existing.isPresent()) {
//                        return existing.get();
//                    } else {
//                        log.warn("Member {} not found, creating new member record", membership.idMember());
//                        TrelloMember newMember = new TrelloMember();
//                        newMember.setMemberId(membership.idMember());
//                        // Set other required fields from membership data
//                        return memberRepository.save(newMember);
//                    }
//                })
                .collect(Collectors.toSet());
    }

    private LocalDateTime parseDate(String dateString) {
        if (dateString == null) return null;
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
    }

}
