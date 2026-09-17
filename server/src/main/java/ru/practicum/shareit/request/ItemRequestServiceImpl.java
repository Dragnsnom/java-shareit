package ru.practicum.shareit.request;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository, ItemRepository itemRepository,
                                   UserRepository userRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, NewItemRequestDto newItemRequestDto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(newItemRequestDto, requestor);
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest), List.of());
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        Map<Long, List<ItemForRequestDto>> itemsByRequest = findItemsByRequests(requests);

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(request,
                        itemsByRequest.getOrDefault(request.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);
        Map<Long, List<ItemForRequestDto>> itemsByRequest = findItemsByRequests(requests);

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(request,
                        itemsByRequest.getOrDefault(request.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        List<ItemForRequestDto> items = toItemForRequestDtos(itemRepository.findAllByRequestId(requestId));
        return ItemRequestMapper.toItemRequestDto(itemRequest, items);
    }

    private Map<Long, List<ItemForRequestDto>> findItemsByRequests(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        return itemRepository.findAllByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId(),
                        Collectors.mapping(this::toItemForRequestDto, Collectors.toList())));
    }

    private List<ItemForRequestDto> toItemForRequestDtos(List<Item> items) {
        return items.stream().map(this::toItemForRequestDto).collect(Collectors.toList());
    }

    private ItemForRequestDto toItemForRequestDto(Item item) {
        return new ItemForRequestDto(item.getId(), item.getName(), item.getOwner().getId());
    }
}
