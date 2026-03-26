package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Account {

    private final UUID id;
    private final AccountType accountType;
    @Nullable
    private final CustomerRef customerRef;
    private final List<SubLedger> subLedgers;
}
