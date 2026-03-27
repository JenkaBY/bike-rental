package com.github.jenkaby.bikerental.componenttest.context;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.SubLedgerJpaEntity;
import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@ScenarioScope
@Component
public class FinanceScenarioContext {

    private List<SubLedgerJpaEntity> subLedgers;

}
