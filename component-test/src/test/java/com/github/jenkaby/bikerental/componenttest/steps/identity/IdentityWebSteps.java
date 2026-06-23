package com.github.jenkaby.bikerental.componenttest.steps.identity;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.identity.web.dto.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.SoftAssertions;

@RequiredArgsConstructor
public class IdentityWebSteps {

    private final ScenarioContext scenarioContext;

    @Given("a create user request with the following data")
    public void aCreateUserRequestWithTheFollowingData(CreateUserRequest request) {
        scenarioContext.setRequestBody(request);
    }

    @Given("an update user request with the following data")
    public void anUpdateUserRequestWithTheFollowingData(UpdateUserRequest request) {
        scenarioContext.setRequestBody(request);
    }

    @Given("a change password request with the following data")
    public void aChangePasswordRequestWithTheFollowingData(ChangePasswordRequest request) {
        scenarioContext.setRequestBody(request);
    }

    @Then("the response matches the user")
    public void theResponseMatchesTheUser(UserResponse expected) {
        assertUser(scenarioContext.getResponseBody(UserResponse.class), expected);
    }

    @Then("the created account matches")
    public void theCreatedAccountMatches(UserResponse expected) {
        var creation = scenarioContext.getResponseBody(UserCreationResponse.class);
        assertUser(creation.user(), expected);
        SoftAssertions.assertSoftly(softly -> softly.assertThat(creation.temporaryPassword())
                .as("temporary password").isNotBlank());
    }

    private void assertUser(UserResponse actual, UserResponse expected) {
        var softly = new SoftAssertions();
        if (expected.id() == null) {
            softly.assertThat(actual.id()).as("user id").isNotNull();
        } else {
            softly.assertThat(actual.id()).as("user id").isEqualTo(expected.id());
        }
        softly.assertThat(actual.username()).as("username").isEqualTo(expected.username());
        softly.assertThat(actual.email()).as("email").isEqualTo(expected.email());
        softly.assertThat(actual.status()).as("status").isEqualTo(expected.status());
        softly.assertThat(actual.mustChangePassword()).as("mustChangePassword").isEqualTo(expected.mustChangePassword());
        softly.assertThat(actual.roles()).as("roles").containsExactlyInAnyOrderElementsOf(expected.roles());
        softly.assertAll();
    }
}
