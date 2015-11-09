package org.mobicents.servlet.restcomm.identity;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.servlet.restcomm.endpoints.Outcome;
import org.mobicents.servlet.restcomm.identity.RestcommIdentityApi.RestcommIdentityApiException;
import org.mobicents.servlet.restcomm.identity.RestcommIdentityApi.UserEntity;

public class RestcommIdentityApiTest {

    static String authServerBaseUrl = "http://192.168.1.3:8080"; //"https://identity.restcomm.com";
    static String username = "otsakir@gmail.com";  //"test_user";
    static String password = "password";
    static String realm = "restcomm";

    static RestcommIdentityApi api;

    public RestcommIdentityApiTest() {
        // TODO Auto-generated constructor stub
    }

    @BeforeClass
    public static void createInstance() throws RestcommIdentityApiException {
        api = new RestcommIdentityApi(authServerBaseUrl, username, password, realm);
        String instanceId = api.createInstance("http://localhost", "my-secret").instanceId;
        api.bindInstance(instanceId);
    }

    @Test
    public void testCreateAndInviteUser() {
        UserEntity user = new UserEntity("test_invited_user",null, "Test Invited User", null, "invited_password");
        assertEquals(api.createUser(user), Outcome.OK);
        assertTrue(api.inviteUser("test_invited_user"));
        // TODO remove user
    }

}