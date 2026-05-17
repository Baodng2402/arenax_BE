package com.bk.arenax.domain.account;

import com.bk.arenax.domain.common.BaseEntity;
import com.bk.arenax.domain.subscription.Subscription;
import com.bk.arenax.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {
    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountType type = AccountType.PERSONAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountStatus status = AccountStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    private Subscription subscription;

    public Account(String name, AccountType type, AccountStatus status, User owner) {
        this.name = name;
        this.type = type;
        this.status = status;
        this.owner = owner;
    }
}
