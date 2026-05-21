package com.memoize.api.Service;

import com.memoize.api.Dto.ConversationDto;
import com.memoize.api.Entity.Conversation;
import com.memoize.api.Entity.User;
import com.memoize.api.Repository.ConversationRepository;
import com.memoize.api.Repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public List<ConversationDto> getConversationsOfUser(UUID userId) {
        List<ConversationDto> list = conversationRepository.fetchConversationsOfUser(userId);
        if (!list.isEmpty()) return list;
        ConversationDto newConversation = createConversationForUser(userId);
        return List.of(newConversation);
    }

    @Override
    @Transactional
    public ConversationDto createConversationForUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
        User user = entityManager.getReference(User.class, userId);
        Conversation newConversation = Conversation.builder().name("New Chat").user(user).summary("").isProperName(false).build();
        conversationRepository.save(newConversation);
        return ConversationDto.fromEntity(newConversation);
    }

    @Override
    public ConversationDto getConversationByIdForUser(UUID conversationId, UUID userId) {
        var conversation = conversationRepository.findByIdAndUserId(conversationId, userId).orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        return ConversationDto.fromEntity(conversation);
    }
}
