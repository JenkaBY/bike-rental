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