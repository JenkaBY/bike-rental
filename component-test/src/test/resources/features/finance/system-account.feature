Feature: System Account Setup
  As a system
  I want to ensure that the necessary system accounts and sub-ledgers are created when a customer is registered
  So that the financial transactions can be properly recorded and tracked

  Scenario: System account and sub-ledgers with 0 balance are created upon system startup
    Given the following account record was persisted in db
      | id    | accountType | customerId |
      | ACC_S | SYSTEM      |            |
    And the following sub-ledger records were persisted in db
      | id       | accountId | ledgerType    | balance |
      | L_S_CASH | ACC_S     | CASH          | 0.00    |
      | L_S_CARD | ACC_S     | CARD_TERMINAL | 0.00    |
      | L_S_TRAN | ACC_S     | BANK_TRANSFER | 0.00    |
      | L_S_REV  | ACC_S     | REVENUE       | 0.00    |
      | L_S_ADJ  | ACC_S     | ADJUSTMENT    | 0.00    |

