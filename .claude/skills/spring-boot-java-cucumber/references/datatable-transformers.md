# Main rule

- If an object being transformed consists of nested objects, create a separate transformer for each of them.
  Use main object identifier (`String groupSlug, String hierarchySlug` in the provided example) to be able to combine it
  into final object in steps.
  An example is:

```java
public record HierarchyGroup(Membership membership, String groupSlug, String hierarchySlug, String parentRef,
                             String displayName) {
}

public record Membership(String personRef) {
}
```

```gherkin
    Given application is started
#  Datatable transformers allow converting datatables directly into domain objects and avoid using JSON in steps:
    And the following hierarchy groups are
      | groupSlug      | hierarchySlug |  parentRef      | displayName              |
      | h-group-slug-1 | h-slug        |  null           | Parent 1                 |
      | h-group-slug-0 | h-slug        |  null           | Parent 0                 |
    And the following memberships are
      | groupSlug      | hierarchySlug | personRef         |
      | h-group-slug-1 | h-slug        | P.ref10@email.com |
      | h-group-slug-0 | h-slug        | P.ref21@email.com |
      | h-group-slug-0 | h-slug        | P.ref22@email.com |
#  instead of:
#    And the hierarchy-group records are committed
#    """
#    [
#      {"group": [{"personRef": "P.ref10@email.com"}],
#        "hierarchySlug":"h-slug", "slug": "h-group-slug-1", "parentRef":null,"displayName":"Parent 1"
#      },
#      {"group": [{ "personRef": "P.ref21@email.com"}, { "slug": "h-group-slug-0". "personRef": "P.ref22@email.com"}],
#        "hierarchySlug":"h-slug", "slug": "h-group-slug-0", "parentRef":null,"displayName":"Parent 0"
#     }
#    ]
#    """
    
```

```java

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TransformerHelper {

    public static HierarchyGroupKey convertToHierarchyGroupKey(Map<String, String> table) {
        return new HierarchyGroupKey(table.get("groupSlug"), table.get("hierarchySlug"));
    }
}

public class HierarchyGroupTransformer {

    @DataTableType
    public HierarchyGroupHolder transform(Map<String, String> table) {
        return new HierarchyGroupHolder(
                TransformerHelper.convertToHierarchyGroupKey(table),
                HierarchyGroup.builder()
                        // populate fields
                        .slug(table.get("groupSlug"))
                        .hierarchyReference(table.get("hierarchySlug"))
                        .displayName(table.get("displayName"))
                        .parentReference(getParentRef(table))
                        .memberships(new ArrayList<>())// Should be populated later as needed
                        .build()
        );
    }

    private static String getParentRef(Map<String, String> table) {
        String parentRef = table.get("parentRef");
        return "null".equals(parentRef) ? null : parentRef;
    }
}

public class MembershipTransformer {

    @DataTableType
    public MembershipHolder transform(Map<String, String> table) {
        return new MembershipHolder(
                TransformerHelper.convertToHierarchyGroupKey(table),
                new Membership(table.get("personRef"))
        );
    }
}

class HierarchyGroupDbSteps {

    private final HierarchyGroupScenarioContext hgScenarioContext;
    private final MongoTemplate mongoTemplate;

    @Given("the following hierarchy group(s) is/are")
    public void theFollowingHierarchyGroupsAre(List<HierarchyGroupHolder> hierarchyGroupHolders) {
        for (HierarchyGroupHolder hierarchyGroupHolder : hierarchyGroupHolders) {
            hgScenarioContext.addHierarchyGroup(hierarchyGroupHolder.key(), hierarchyGroupHolder.hierarchyGroup());
        }
    }

    @Given("the following membership(s) is/are")
    public void theFollowingMembershipsAre(List<MembershipHolder> membershipHolders) {
        for (MembershipHolder membershipHolder : membershipHolders) {
            hgScenarioContext.addMembershipToGroup(membershipHolder.key(), membershipHolder.membership());
        }
    }

    @Given("the hierarchy-group records are committed")
    public void theHierarchyGroupsRecordsAreCommitted() {
        for (var group : hgScenarioContext.getHierarchyGroups().values()) {
            log.info("Persisting into collection `{}`: {}", COLLECTION_NAME, group);
            mongoTemplate.save(group, COLLECTION_NAME);
        }
    }
}
```

# Builder + ScenarioContext (request with a nested list of sub-objects)

When the endpoint request itself carries a nested collection (a list of sub-objects), prepare it across **two steps**
and keep all conversion in transformers — never assemble the request or parse a DataTable inside the step:

1. **Sub-part step** — one `@DataTableType` entry transformer maps each row to one sub-object; the step parameter is a
   `List<Sub>`, and the step stores it in `ScenarioContext` (add a field; the context is `@Getter @Setter`).
2. **`…request is prepared with the following data` step** — a second `@DataTableType` maps the single globals row to a
   `{Request}Builder` record (in `model/`); the step calls `builder.toRequest(storedSubParts)` and sets the request
   body.

This mirrors the tariff pattern (`the pricing params for tariff request are` + `the tariff v2 request is prepared with
the following data`, `TariffV2RequestBuilder.toRequest(pricingParams)`).

```gherkin
Given the equipment items for cost calculation request are
  | equipmentId | equipmentType | returnAt         |
  | 10          | SCOOTER       | 2026-06-01T11:00 |
  | 11          | SCOOTER       | 2026-06-01T14:00 |
And the rental cost calculation request is prepared with the following data
  | startAt          | plannedDurationMinutes |
  | 2026-06-01T09:00 | 180                    |
```

```java
public class CostCalculationV2RequestTransformer {

    @DataTableType
    public CostCalculationV2RequestBuilder transform(Map<String, String> entry) { /* globals → builder */ }

    @DataTableType
    public CostCalculationV2Request.EquipmentItemRequest equipmentItem(Map<String, String> entry) { /* one item */ }
}

public record CostCalculationV2RequestBuilder(Instant startAt, Integer plannedDurationMinutes, /* ... */) {
    public CostCalculationV2Request toRequest(List<CostCalculationV2Request.EquipmentItemRequest> equipments) {
        return new CostCalculationV2Request(equipments, startAt, plannedDurationMinutes, /* ... */);
    }
}

public class RentalCostCalculationV2WebSteps {

    private final ScenarioContext scenarioContext;

    @Given("the equipment items for cost calculation request are")
    public void items(List<CostCalculationV2Request.EquipmentItemRequest> items) {
        scenarioContext.setEquipmentItems(items);
    }

    @Given("the rental cost calculation request is prepared with the following data")
    public void prepare(CostCalculationV2RequestBuilder builder) {
        scenarioContext.setRequestBody(builder.toRequest(scenarioContext.getEquipmentItems()));
    }
}
```