<task_file_template>

# Task 016: Create the `agreement-pdf-preview.feature` component test

> **Applied Skill:** `.claude/rules/component-tests.md` / `spring-boot-java-cucumber` — happy paths only (validation
> negatives live in the WebMvc test, Task 013); features under `features/agreement/`; run via `RunComponentTests`. Uses
> the binary steps from Task 015 and the shared `a prepared payload is` step.

## 1. Objective

Cover the two acceptance scenarios that require a real rendered PDF: (1) a Cyrillic preview returns 200 +
`application/pdf` and the extracted text contains the submitted Cyrillic phrase AND the fixture customer name
("Иванов"); (2) long content paginates to more than one page. Depends on Task 015 (steps) and Task 011 (endpoint).

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/resources/features/agreement/agreement-pdf-preview.feature`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```gherkin
Feature: Agreement PDF Preview
  As an administrator editing agreement templates
  I want to preview any (possibly unsaved) agreement text as the exact PDF the customer will sign
  So that I can verify layout, wording and Cyrillic rendering before activating a template

  Scenario: Preview an unsaved Cyrillic text renders a valid PDF with the fixture data block
    Given a prepared payload is
    """
    {"title": "Договор аренды", "content": "Настоящим вы соглашаетесь вернуть оборудование вовремя."}
    """
    When a POST request for a PDF has been made to "/api/agreements/preview-pdf" endpoint
    Then the PDF response status is 200
    And the PDF response content type is "application/pdf"
    And the PDF body is a valid document containing text "соглашаетесь вернуть оборудование"
    And the PDF body is a valid document containing text "Иванов"

  Scenario: Long agreement content paginates to more than one page
    Given a prepared payload is
    """
    {"title": "Договор аренды", "content": "Пункт первый. Арендатор обязуется бережно относиться к оборудованию и возвращать его в исправном состоянии в согласованный срок, а также нести ответственность за любые повреждения, возникшие по его вине в период пользования оборудованием.\nПункт второй. Арендодатель предоставляет оборудование в исправном состоянии и гарантирует его пригодность для заявленного использования на весь срок аренды.\nПункт третий. Стороны согласовали размер залога и порядок его возврата после завершения аренды при отсутствии повреждений оборудования.\nПункт четвёртый. Настоящий договор вступает в силу с момента подписания и действует до полного исполнения обязательств обеими сторонами.\nПункт пятый. Все споры, возникающие из настоящего договора, разрешаются путём переговоров, а при недостижении согласия — в установленном законом порядке.\nПункт шестой. Арендатор подтверждает, что ознакомлен с правилами эксплуатации оборудования и обязуется их соблюдать на протяжении всего срока аренды.\nПункт седьмой. Любые изменения и дополнения к настоящему договору оформляются в письменном виде и подписываются обеими сторонами.\nПункт восьмой. Арендатор не вправе передавать оборудование третьим лицам без письменного согласия арендодателя."}
    """
    When a POST request for a PDF has been made to "/api/agreements/preview-pdf" endpoint
    Then the PDF response status is 200
    And the PDF body has more than 1 page
```

> The first scenario asserts BOTH the submitted Cyrillic phrase and the fixture customer surname "Иванов" (proving the
> fixture data block is rendered). Do NOT add validation-failure scenarios here — those belong in the WebMvc test
> (Task 013). If the long-content scenario does not exceed one page in practice, lengthen the `content` string further
> (more paragraphs) rather than lowering the page threshold.

## 4. Validation Steps

Execute the following command (assumes the DB is already up). Do NOT run the full application server.

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

</task_file_template>
