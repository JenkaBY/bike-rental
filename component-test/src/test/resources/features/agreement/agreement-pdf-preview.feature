Feature: Agreement PDF Preview
  As an administrator editing agreement templates
  I want to preview any (possibly unsaved) agreement text as the exact PDF the customer will sign
  So that I can verify layout, wording and Cyrillic rendering before activating a template

  Background:
    Given the request header "Content-Type" is "application/json"

  Scenario: Preview an unsaved Cyrillic text renders a valid PDF with the fixture data block
    Given the agreement pdf preview request is
      | title          | content                                                  |
      | Договор аренды | Настоящим вы соглашаетесь вернуть оборудование вовремя. |
    When a POST request for "application/pdf" content has been made to "/api/agreements/preview" endpoint
    Then the response status is 200
    And the response headers contain
      | name         | value           |
      | Content-Type | application/pdf |
    And the PDF body is a valid document containing text "соглашаетесь вернуть оборудование"
    And the PDF body is a valid document containing text "Иванов"
    And the PDF body is a valid document containing text "1. Горный велосипед(BIKE-001) — 25.00 BYN"
    And the PDF body is a valid document containing text "2. Шлем защитный(HELM-014) — 5.00 BYN"
    And the PDF body is a valid document containing text "02:00 h"
    And the PDF body is a valid document containing text "Total: 30.00 BYN"
    And the PDF body is a valid document containing text "Template Hash content_SHA256"
    And the PDF body is a valid document matching pattern "Договор аренды dated \d{2}\.\d{2}\.\d{4}"

  Scenario: Template placeholders are substituted with fixture customer and rental data
    Given the agreement pdf preview request is
      | title          | content                                                                                                        |
      | Договор аренды | Уважаемый {{customer.firstName}} {{customer.lastName}}, аренда №{{rental.number}} на сумму {{rental.total}}. |
    When a POST request for "application/pdf" content has been made to "/api/agreements/preview" endpoint
    Then the response status is 200
    And the PDF body is a valid document containing text "Уважаемый Иван Иванов, аренда №0 на сумму 30.00 BYN."
    And the PDF body is a valid document not containing text "{{customer.firstName}}"

  Scenario: Long agreement content paginates to more than one page
    Given the agreement pdf preview request is
      | title          | content                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
      | Договор аренды | Пункт первый. Арендатор обязуется бережно относиться к оборудованию и возвращать его в исправном состоянии в согласованный срок, а также нести ответственность за любые повреждения, возникшие по его вине в период пользования оборудованием.\nПункт второй. Арендодатель предоставляет оборудование в исправном состоянии и гарантирует его пригодность для заявленного использования на весь срок аренды.\nПункт третий. Стороны согласовали размер залога и порядок его возврата после завершения аренды при отсутствии повреждений оборудования.\nПункт четвёртый. Настоящий договор вступает в силу с момента подписания и действует до полного исполнения обязательств обеими сторонами.\nПункт пятый. Все споры, возникающие из настоящего договора, разрешаются путём переговоров, а при недостижении согласия — в установленном законом порядке.\nПункт шестой. Арендатор подтверждает, что ознакомлен с правилами эксплуатации оборудования и обязуется их соблюдать на протяжении всего срока аренды.\nПункт седьмой. Любые изменения и дополнения к настоящему договору оформляются в письменном виде и подписываются обеими сторонами.\nПункт восьмой. Арендатор не вправе передавать оборудование третьим лицам без письменного согласия арендодателя.\nПункт девятый. Арендатор обязан незамедлительно уведомить арендодателя о любой неисправности, поломке или утрате оборудования, возникшей в период аренды.\nПункт десятый. При возврате оборудования стороны совместно осматривают его состояние и фиксируют выявленные недостатки в акте возврата.\nПункт одиннадцатый. Арендодатель не несёт ответственности за вред, причинённый арендатором третьим лицам в период пользования оборудованием.\nПункт двенадцатый. Стоимость аренды рассчитывается согласно действующим тарифам арендодателя, зафиксированным в учётной системе на момент начала аренды.\nПункт тринадцатый. При просрочке возврата оборудования арендатор оплачивает дополнительное время пользования по тарифам арендодателя.\nПункт четырнадцатый. Персональные данные арендатора обрабатываются арендодателем исключительно в целях исполнения настоящего договора.\nПункт пятнадцатый. Настоящий договор составлен в электронной форме и подписан собственноручной подписью арендатора, воспроизведённой средствами электронного планшета.\nПункт шестнадцатый. Во всём остальном, что не предусмотрено настоящим договором, стороны руководствуются действующим законодательством. |
    When a POST request for "application/pdf" content has been made to "/api/agreements/preview" endpoint
    Then the response status is 200
    And the PDF body has more than 1 page
