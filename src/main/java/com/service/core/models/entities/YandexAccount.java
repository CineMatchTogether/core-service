package com.service.core.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "yandex_accounts")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YandexAccount {

    @Id
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "yandex_id")
    private Long yandexId;

    @Column(name = "kinopoisk_id")
    private Long kinopoiskId;

    private String login;

    private String email;

    @Column(name = "real_name")
    private String realName;
}
