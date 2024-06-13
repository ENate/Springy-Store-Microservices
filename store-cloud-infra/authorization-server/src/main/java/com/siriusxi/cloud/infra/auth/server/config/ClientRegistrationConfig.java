package com.siriusxi.cloud.infra.auth.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class ClientRegistrationConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientRegistrationConfig.class);
	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
		.clientId("store")
		.clientSecret("{noop}secret")
		.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
		.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
		.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
		.redirectUri("http://127.0.0.1:8080/login/oauth2/code/oidc-client")
		.postLogoutRedirectUri("http://127.0.0.1:8080/")
		.scope(OidcScopes.OPENID)
		.scope(OidcScopes.PROFILE)
		.scope("SCOPE_product.write")
		.scope("SCOPE_product.read")
		.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
		.build();

		return new InMemoryRegisteredClientRepository(oidcClient);
	}
}
