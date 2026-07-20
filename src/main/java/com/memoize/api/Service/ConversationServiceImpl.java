package com.memoize.api.Service;

import com.memoize.api.Dto.ConversationDto;
import com.memoize.api.Entity.Conversation;
import com.memoize.api.Entity.User;
import com.memoize.api.Repository.ConversationRepository;
import com.memoize.api.Repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final VectorStore chatMemoryVectorStore;

    public ConversationServiceImpl(
        ConversationRepository conversationRepository,
        UserRepository userRepository,
        EntityManager entityManager,
        @Qualifier("chat-memory-vector-store") VectorStore chatMemoryVectorStore
    ) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.chatMemoryVectorStore = chatMemoryVectorStore;
    }

    @Override
    @Transactional
    public List<ConversationDto> getConversationsOfUser(UUID userId) {
        try {
            createNewConversationForUser(userId);
        } catch (EntityExistsException ex) {
            log.info("New conversation already exists");
        }
        return conversationRepository.fetchConversationsOfUser(userId);
    }

    @Override
    @Transactional
    public ConversationDto createNewConversationForUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
        if (conversationRepository.newConversationExistsForUser(userId)) {
            throw new EntityExistsException("A new conversation already exists");
        }
        User user = entityManager.getReference(User.class, userId);
        Conversation newConversation = Conversation.builder().name("New Chat").user(user).isNew(true).isProperName(false).build();
        conversationRepository.save(newConversation);
        return ConversationDto.fromEntity(newConversation);
    }

    @Override
    public ConversationDto getConversationByIdForUser(UUID conversationId, UUID userId) {
        var conversation = conversationRepository.findByIdAndUserId(conversationId, userId).orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        return ConversationDto.fromEntity(conversation);
    }

    @Override
    @Transactional
    public int deleteConversationByIdAndUser(UUID conversationId, UUID userId) {
        if (!conversationRepository.existsByIdAndUserId(conversationId, userId)) {
            throw new EntityNotFoundException("Conversation doesn't exist");
        }
        var conversationFilter = new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key("conversationId"), new Filter.Value(conversationId));
        chatMemoryVectorStore.delete(conversationFilter);
        return conversationRepository.deleteByIdAndUserId(conversationId, userId);
    }
}
