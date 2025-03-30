package com.rephelper.infrastructure.adapter.firebase;

import com.rephelper.application.dto.FirebaseUserInfo;
import org.springframework.stereotype.Component;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.rephelper.domain.exception.AuthenticationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FirebaseAuthAdapter {

    private final FirebaseAuth firebaseAuth;

    public String verifyToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                log.error("Token Firebase nulo ou vazio");
                throw new AuthenticationException("Token Firebase inválido: não pode ser nulo ou vazio");
            }
            
            log.debug("Verificando token Firebase: {}", token.substring(0, Math.min(10, token.length())) + "...");
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            log.debug("Token verificado com sucesso para o usuário: {}", decodedToken.getUid());
            return decodedToken.getUid();
        } catch (FirebaseAuthException e) {
            log.error("Falha na verificação do token Firebase", e);
            throw new AuthenticationException("Token Firebase inválido ou expirado: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao verificar token Firebase", e);
            throw new AuthenticationException("Erro ao verificar autenticação: " + e.getMessage());
        }
    }

    public FirebaseUserInfo getUserInfo(String uuid) {
        try {
            if (uuid == null || uuid.isEmpty()) {
                log.error("UUID nulo ou vazio");
                throw new AuthenticationException("UUID inválido: não pode ser nulo ou vazio");
            }
            
            log.debug("Buscando informações do usuário Firebase: {}", uuid);
            UserRecord userRecord = firebaseAuth.getUser(uuid);
            log.debug("Informações do usuário obtidas com sucesso: {}", userRecord.getUid());

            // Safely handle provider data
            String provider = "unknown";
            if (userRecord.getProviderData() != null && !(userRecord.getProviderData().length == 0)) {
                provider = userRecord.getProviderData()[0].getProviderId();
            }

            return FirebaseUserInfo.builder()
                    .uuid(userRecord.getUid())
                    .email(userRecord.getEmail())
                    .displayName(userRecord.getDisplayName())
                    .photoUrl(userRecord.getPhotoUrl())
                    .phoneNumber(userRecord.getPhoneNumber())
                    .emailVerified(userRecord.isEmailVerified())
                    .provider(provider)
                    .build();
        } catch (FirebaseAuthException e) {
            log.error("Erro ao buscar informações do usuário no Firebase", e);
            throw new AuthenticationException("Erro ao buscar informações do usuário no Firebase: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar informações do usuário no Firebase", e);
            throw new AuthenticationException("Erro ao buscar informações do usuário: " + e.getMessage());
        }
    }
}