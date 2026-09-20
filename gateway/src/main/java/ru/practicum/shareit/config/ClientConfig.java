package ru.practicum.shareit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.user.UserClient;

@Configuration
public class ClientConfig {

    @Bean
    public BookingClient bookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        return new BookingClient(buildRestTemplate(builder, serverUrl + BookingClient.API_PREFIX));
    }

    @Bean
    public UserClient userClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        return new UserClient(buildRestTemplate(builder, serverUrl + UserClient.API_PREFIX));
    }

    @Bean
    public ItemClient itemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        return new ItemClient(buildRestTemplate(builder, serverUrl + ItemClient.API_PREFIX));
    }

    @Bean
    public ItemRequestClient itemRequestClient(@Value("${shareit-server.url}") String serverUrl,
                                               RestTemplateBuilder builder) {
        return new ItemRequestClient(buildRestTemplate(builder, serverUrl + ItemRequestClient.API_PREFIX));
    }

    private RestTemplate buildRestTemplate(RestTemplateBuilder builder, String baseUrl) {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(baseUrl))
                .requestFactory(() -> new JdkClientHttpRequestFactory())
                .build();
    }
}
